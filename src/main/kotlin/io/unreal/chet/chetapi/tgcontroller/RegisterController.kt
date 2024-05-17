package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.MessageUpdate
import eu.vendeli.tgbot.utils.setChain
import io.unreal.chet.chetapi.objects.CreateUserRequest
import io.unreal.chet.chetapi.services.UserService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Component

@Component
class RegisterController(private val userService: UserService, private val conversation: TermsOfServiceChain.Name) {

    @CommandHandler(["/register"])
    suspend fun register(bot: TelegramBot, up: MessageUpdate, user: User) {
        val request = CreateUserRequest(user.id, null)

        val telegramUser = userService.getUserUidWithTelegramId(user.id).awaitSingleOrNull()
        if (telegramUser != null) {
            message { "You are already registered." }.send(up.message.chat.id, bot)
            return
        }

        message { "Do you agree to our TOS? https://chetai.xyz/tos" }.inlineKeyboardMarkup {
            callbackData("Agree") { TermsOfServiceChain.TOS_YES}
            callbackData("Deny") { TermsOfServiceChain.TOS_NO }
        }.send(user, bot)

        bot.inputListener.setChain(up.user, conversation)
        bot.userData[up.user, "deletingInChat"] = up.message.chat.id
        bot.userData[up.user, "name"] = up.message.chat.id
    }
}
