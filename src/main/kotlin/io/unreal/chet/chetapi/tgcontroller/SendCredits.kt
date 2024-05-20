package io.unreal.chet.chetapi.tgcontroller

import kotlinx.coroutines.reactor.awaitSingle

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.RegexCommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.MessageUpdate
import io.unreal.chet.chetapi.error.UserExceededCreditError
import io.unreal.chet.chetapi.error.UserInsufficientCreditsError
import io.unreal.chet.chetapi.error.UserNotFoundError
import io.unreal.chet.chetapi.services.CreditService
import org.springframework.stereotype.Component

@Component
class SendCredits(
    private val creditService: CreditService,
) {
    companion object {
        const val SEND_CREDITS_COMMAND = "/send"
        const val CREDIT_TRANSFER_SUCCESS = "Credits transferred successfully!"
        const val CREDIT_TRANSFER_FAILURE = "Could not transfer credits."
        const val INVALID_SEND_COMMAND_FORMAT = "Oh no, the send command not formatted correctly!"
    }

    fun isValidSendCommand(command: String): Pair<Boolean, List<String>?> {
        val sendCommandPattern = Regex("^/send\\s+(\\d+)\\s+(\\d+)\$")
        val matchResult = sendCommandPattern.matchEntire(command.trim())
        return if (matchResult != null) {
            Pair(true, matchResult.groupValues.drop(1))
        } else {
            Pair(false, null)
        }
    }

    @RegexCommandHandler("^$SEND_CREDITS_COMMAND .*")
    suspend fun register(bot: TelegramBot, up: MessageUpdate, user: User) {
        val fullMessageContent = up.message.text

        if (fullMessageContent == null) {
            message { INVALID_SEND_COMMAND_FORMAT }.send(up.message.chat.id, bot)
            return
        }

        val (isValid, groups) = isValidSendCommand(fullMessageContent)
        if (isValid && groups != null) {
            val recipient = groups[0].toLong()
            val amount = groups[1].toInt()

            try {
                val success = creditService.transferCredits(user.id, recipient, amount).awaitSingle()
                if (success) {
                    message { CREDIT_TRANSFER_SUCCESS }
                        .options { replyParameters(messageId = up.message.messageId) }
                        .send(up.message.chat.id, bot)
                } else {
                    message { CREDIT_TRANSFER_FAILURE }
                        .options { replyParameters(messageId = up.message.messageId) }
                        .send(up.message.chat.id, bot)
                }
            } catch (ex: Exception) {
                if (ex is UserExceededCreditError || ex is UserInsufficientCreditsError || ex is UserNotFoundError) {
                    message { ex.localizedMessage }
                        .options { replyParameters(messageId = up.message.messageId) }
                        .send(up.message.chat.id, bot)
                } else {
                    message { CREDIT_TRANSFER_FAILURE }
                        .options { replyParameters(messageId = up.message.messageId) }
                        .send(up.message.chat.id, bot)
                }
            }
        } else {
            message { INVALID_SEND_COMMAND_FORMAT }.
            options { replyParameters(messageId = up.message.messageId) }
                .send(up.message.chat.id, bot)
        }
    }
}
