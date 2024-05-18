package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.RegexCommandHandler
import eu.vendeli.tgbot.api.media.sendPhoto
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.MessageUpdate
import io.unreal.chet.chetapi.error.UserExceededCreditError
import io.unreal.chet.chetapi.objects.PromptRequest
import io.unreal.chet.chetapi.objects.QueryCostId
import io.unreal.chet.chetapi.services.CreditService
import io.unreal.chet.chetapi.services.PromptService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Component

@Component
class ChetImgController(
    val promptService: PromptService,
    val creditService: CreditService
) {

    companion object {
        const val CHET_IMG_COMMAND = "/chetimg"
        const val CHARGE = "charge"
        const val CHARGE_FOR_DALL3_PROMPT = "charge for dall3 prompt"
        const val PROMPT_PROCESSED_SUCCESSFULLY = "Prompt processed successfully"
        const val UNEXPECTED_ERROR_OCCURRED_OPENAI = "We're having trouble generating the image right now.."
        const val CREDIT_LIMIT_EXCEEDED = "You have exceeded your credit limit."
    }

    @RegexCommandHandler("^$CHET_IMG_COMMAND .*")
    suspend fun chetChat(bot: TelegramBot, messageUpdate: MessageUpdate) {
        val prompt = messageUpdate.text.replace(CHET_IMG_COMMAND, "").trim()
        val request = PromptRequest(telegramId = messageUpdate.user.id, prompt = prompt, isImage = true)

        try {
            // Perform transaction check first
            creditService.checkIfUserHasEnoughCredit(messageUpdate.user.id, QueryCostId.DALLE3).awaitSingleOrNull()

            message { "Generating Image..." }
                .options { replyParameters(messageId = messageUpdate.message.messageId) }
                .sendAsync(messageUpdate.message.chat.id, bot).await()

            // Process the prompt only if the transaction check succeeds
            val result = promptService.processPrompt(request).awaitSingleOrNull()
            val messageContent = result ?: PROMPT_PROCESSED_SUCCESSFULLY
            sendMessage(bot, messageUpdate, messageContent, request.isImage)

            creditService.queryTransaction(
                messageUpdate.message.chat.id,
                queryCostId = QueryCostId.DALLE3,
                CHARGE,
                CHARGE_FOR_DALL3_PROMPT
            ).awaitSingleOrNull()

        } catch (error: UserExceededCreditError) {
            println("User exceeded credit: ${error.localizedMessage}")
            sendMessage(bot, messageUpdate, CREDIT_LIMIT_EXCEEDED, false)
        } catch (error: Exception) {
            println("Error occurred while processing prompt: ${error.localizedMessage}")
            sendMessage(bot, messageUpdate, UNEXPECTED_ERROR_OCCURRED_OPENAI, false)
        }
    }

    private suspend fun sendMessage(
        bot: TelegramBot,
        messageUpdate: MessageUpdate,
        messageContent: String,
        isImage: Boolean
    ) {
        if (isImage) {
            sendPhoto { messageContent }
                .options {
                    parseMode = ParseMode.MarkdownV2
                    replyParameters(messageId = messageUpdate.message.messageId)
                }
                .sendAsync(to = messageUpdate.message.chat.id, bot).await()
        } else {
            val escapedString = escapeMarkdownV2(messageContent)
            sendMarkdownMessage(bot, messageUpdate, escapedString)
        }
    }

    private suspend fun sendMarkdownMessage(bot: TelegramBot, messageUpdate: MessageUpdate, messageContent: String) {
        message {
            String.format("%s", messageContent) // Corrected format string
        }
            .options {
                parseMode = ParseMode.MarkdownV2
                linkPreviewOptions { isDisabled = true }
                replyParameters(messageId = messageUpdate.message.messageId)
            }
            .send(to = messageUpdate.message.chat.id, bot)
    }

    private fun escapeMarkdownV2(text: String): String {
        return text
            .replace("_", "\\_")
            .replace("*", "\\*")
            .replace("~", "\\~")
            .replace("`", "\\`")
            .replace(">", "\\>")
            .replace("+", "\\+")
            .replace("=", "\\=")
            .replace("{", "\\{")
            .replace("}", "\\}")
            .replace(".", "\\.")
            .replace("#", "\\#")
            .replace("!", "\\!")
            .replace("-", "\\-")
            .replace("|", "\\|")
            .replace("(", "\\(")
            .replace(")", "\\)")
    }
}
