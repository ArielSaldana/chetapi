package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.InputChain
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.BreakCondition
import eu.vendeli.tgbot.types.internal.ChainLink
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import io.unreal.chet.chetapi.objects.QueryCostId
import io.unreal.chet.chetapi.services.CreditService
import io.unreal.chet.chetapi.services.UserRegistrationService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
@InputChain
class TermsOfServiceChain(
    private val userRegistrationService: UserRegistrationService,
    private val creditService: CreditService
) {
    companion object {
        const val TOS_YES = "YES"
        const val TOS_NO = "NO"
        const val ACCOUNT_CREATION_SUCCESS = "Congratulations! Your account has been created"
        const val ACCOUNT_CREATION_FAILURE = "User creation failed for an unknown reason."
        const val ERROR_CREATING_USER = "Error creating user"
        const val USER_REGISTRATION_ABORTED = "User registration aborted. You need to agree to the TOS to use our service."
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
                        sendMessage(bot, to, ACCOUNT_CREATION_SUCCESS)

                        val creditService = creditService.queryTransaction(
                            user.id, QueryCostId.PROMOTION100CREDITS,
                            "credit_user",
                            "Offering users a one time sign up credit").awaitSingleOrNull()

                        if (creditService !== null) {
                            sendMessage(bot, to, "You have received 100 credits for signing up!")
                        }
                    } else {
                        sendMessage(bot, to, ACCOUNT_CREATION_FAILURE)
                    }
                } catch (ex: Exception) {
                    sendMessage(bot, to, "$ERROR_CREATING_USER $ex")
                }
            }
        }

        override suspend fun breakAction(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            sendMessage(bot, bot.userData.get<Long>(user.id, "deletingInChat") ?: return, USER_REGISTRATION_ABORTED)
        }

        private suspend fun sendMessage(bot: TelegramBot, to: Long, messageContent: String) {
            message { messageContent }
                .replyKeyboardRemove(true)
                .send(to, bot)
        }
    }
}
