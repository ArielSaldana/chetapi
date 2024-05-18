package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.MessageUpdate
import io.unreal.chet.chetapi.services.UserBalanceService
import kotlinx.coroutines.reactive.awaitFirstOrNull
import org.springframework.stereotype.Component

@Component // This annotation is necessary for the correct functioning of the spring DI
class BalanceController(private val userBalanceService: UserBalanceService) {

    companion object {
        const val BALANCE_RETRIEVAL_SUCCESS = "You currently have %d credits!"
        const val BALANCE_RETRIEVAL_FAILURE = "Could not retrieve your balance."
    }

    @CommandHandler(["/balance"])
    suspend fun balance(user: User, bot: TelegramBot, messageUpdate: MessageUpdate) {
        val userCreditBalance = userBalanceService.getUserCreditBalance(messageUpdate.user.id)
            .awaitFirstOrNull()

        val messageContent = userCreditBalance?.let { String.format(BALANCE_RETRIEVAL_SUCCESS, it) } ?: BALANCE_RETRIEVAL_FAILURE
        sendMessage(messageUpdate.message.chat.id, messageUpdate.message.messageId, bot, messageContent)
    }

    private suspend fun sendMessage(telegramChatId: Long, messageId:Long,  bot: TelegramBot, messageContent: String) {
        message { messageContent }
            .options { replyParameters(messageId = messageId) }
            .send(telegramChatId, bot)
    }
}
