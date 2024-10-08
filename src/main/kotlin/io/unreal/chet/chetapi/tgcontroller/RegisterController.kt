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
class RegisterController(
    private val conversation: TermsOfServiceChain.Name,
    private val userService: UserService
) {
    companion object {
        const val ALREADY_REGISTERED_MESSAGE = "You are already registered."
        const val TOS_AGREEMENT_MESSAGE = "Do you agree to our TOS? https://chetai.xyz/tos"
    }

    @CommandHandler(["/register"])
    suspend fun register(bot: TelegramBot, up: MessageUpdate, user: User) {
        val request = CreateUserRequest(user.id, null)

        val telegramUser = userService.getUserUidWithTelegramId(user.id).awaitSingleOrNull()
        if (telegramUser != null) {
            message { ALREADY_REGISTERED_MESSAGE }.send(up.message.chat.id, bot)
            return
        }

        message { TOS_AGREEMENT_MESSAGE }.inlineKeyboardMarkup {
            callbackData("Agree") { TermsOfServiceChain.TOS_YES }
            callbackData("Deny") { TermsOfServiceChain.TOS_NO }
        }.send(up.message.chat.id, bot)

        bot.inputListener.setChain(up.user, conversation)
        bot.userData[up.user, "deletingInChat"] = up.message.chat.id
        bot.userData[up.user, "name"] = up.message.chat.id
    }
}
