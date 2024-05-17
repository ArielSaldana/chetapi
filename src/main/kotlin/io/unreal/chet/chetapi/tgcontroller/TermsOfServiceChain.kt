package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.InputChain
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.BreakCondition
import eu.vendeli.tgbot.types.internal.ChainLink
import eu.vendeli.tgbot.types.internal.ProcessedUpdate
import io.unreal.chet.chetapi.objects.CreateUserRequest
import io.unreal.chet.chetapi.repository.mongo.UserRepository
import io.unreal.chet.chetapi.services.UserService
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
@InputChain
class TermsOfServiceChain(
    val userRepository: UserRepository,
    val userService: UserService
) {
    companion object {
        const val TOS_YES = "YES"
        const val TOS_NO = "NO"

        lateinit var userService: UserService
    }

    init {
        Companion.userService = userService
    }

    @Component
    object Name : ChainLink() {
        override val breakCondition = BreakCondition { _, update, _ -> update.text != TOS_YES }

        override suspend fun action(user: User, update: ProcessedUpdate, bot: TelegramBot) {
            val to = bot.userData.get<Long>(user.id, "deletingInChat") ?: return

            val createUserRequest = CreateUserRequest(
                telegramId = user.id.toLong(),
                solanaAddress = null
            )

            CoroutineScope(Dispatchers.Default).launch {
                try {
                    userService.createUser(createUserRequest).awaitSingleOrNull()
                    message { "Congratulations! Your account has been created" }
                        .replyKeyboardRemove(true)
                        .send(to, bot)
                } catch (ex: Exception) {
                    message { "Error creating user: ${ex.message}" }
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
