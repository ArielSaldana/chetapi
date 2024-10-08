package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.MessageUpdate
import io.unreal.chet.chetapi.error.UserBalanceNotFoundError
import io.unreal.chet.chetapi.services.UserBalanceService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component // This annotation is necessary for the correct functioning of the spring DI
class BalanceController(private val userBalanceService: UserBalanceService) {

    companion object {
        const val BALANCE_RETRIEVAL_SUCCESS = "You currently have %d credits! If you'd like more please contact @MadeEast"
        const val BALANCE_RETRIEVAL_FAILURE = "Could not retrieve your balance. are you registered?"
        const val USER_BALANCE_DOES_NOT_EXIST = "Oops! Are you registered?"
    }

    @CommandHandler(["/balance"])
    suspend fun balance(user: User, bot: TelegramBot, messageUpdate: MessageUpdate) {
        try {
            val userCreditBalance = userBalanceService.getUserCreditBalance(messageUpdate.user.id)
                .awaitFirstOrNull()
            val messageContent =
                userCreditBalance?.let { String.format(BALANCE_RETRIEVAL_SUCCESS, it) } ?: BALANCE_RETRIEVAL_FAILURE
            sendMessage(messageUpdate.message.chat.id, messageUpdate.message.messageId, bot, messageContent)
        } catch (e: Exception) {
            if (e is UserBalanceNotFoundError) {
                sendMessage(
                    messageUpdate.message.chat.id,
                    messageUpdate.message.messageId,
                    bot,
                    USER_BALANCE_DOES_NOT_EXIST
                )
                return
            }
            sendMessage(messageUpdate.message.chat.id, messageUpdate.message.messageId, bot, BALANCE_RETRIEVAL_FAILURE)
            return
        }
    }

    private suspend fun sendMessage(telegramChatId: Long, messageId: Long, bot: TelegramBot, messageContent: String) {
        message { messageContent }
            .options { replyParameters(messageId = messageId) }
            .send(telegramChatId, bot)
    }
}
