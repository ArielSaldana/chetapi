package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.InputChain
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.BreakCondition
import eu.vendeli.tgbot.types.internal.ChainLink
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import io.unreal.chet.chetapi.objects.CreateUserRequest
import io.unreal.chet.chetapi.objects.QueryCostId
import io.unreal.chet.chetapi.objects.TransactionType
import io.unreal.chet.chetapi.repository.mongo.CreditTransactions
import io.unreal.chet.chetapi.services.CreditService
import io.unreal.chet.chetapi.services.CreditTransactionService
import io.unreal.chet.chetapi.services.UserRegistrationService
import io.unreal.chet.chetapi.services.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
@InputChain
class TermsOfServiceChain(
    private val userService: UserService,
    private val userRegistrationService: UserRegistrationService,
    private val creditService: CreditService
) {
    companion object {
        const val TOS_YES = "YES"
        const val TOS_NO = "NO"
    }

    @Component
    inner class Name : ChainLink() {
        override val breakCondition = BreakCondition { _, update, _ -> update.text != TOS_YES }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            val to = bot.userData.get<Long>(user.id, "deletingInChat") ?: return

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    val createdUserId = userRegistrationService.registerUserWithTelegramId(user.id).awaitSingleOrNull()

                    if (createdUserId != null) {
                        message { "Congratulations! Your account has been created" }
                            .replyKeyboardRemove(true)
                            .send(to, bot)

                        val creditService = creditService.queryTransaction(
                            user.id, QueryCostId.PROMOTION100CREDITS,
                            "credit_user",
                            "Offering users a one time sign up credit").awaitSingleOrNull()

                        if (creditService !== null) {
                            message { "You have received 100 credits for signing up!" }
                                .replyKeyboardRemove(true)
                                .send(to, bot)
                        }
                    } else {
                        message { "User creation failed for an unknown reason." }
                            .replyKeyboardRemove(true)
                            .send(to, bot)
                    }
                } catch (ex: Exception) {
                    message { "Error creating user $ex" }
                        .replyKeyboardRemove(true)
                        .send(to, bot)
                }
            }
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            message { "User registration aborted. You need to agree to the TOS to use our service." }
                .replyKeyboardRemove(true)
                .send(bot.userData.get<Long>(user.id, "deletingInChat") ?: return, bot)
        }
    }
}
