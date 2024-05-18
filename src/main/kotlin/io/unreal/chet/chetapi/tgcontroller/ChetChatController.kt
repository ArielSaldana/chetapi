package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.RegexCommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.MessageUpdate
import io.unreal.chet.chetapi.objects.HttpResponse
import io.unreal.chet.chetapi.objects.PromptRequest
import io.unreal.chet.chetapi.objects.QueryCostId
import io.unreal.chet.chetapi.objects.SimpleStringResponseEntity
import io.unreal.chet.chetapi.services.CreditService
import io.unreal.chet.chetapi.services.PromptService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class ChetChatController(val promptService: PromptService, private val creditService: CreditService) {

    companion object {
        const val CHET_COMMAND = "/chet"
        const val CHARGE = "charge"
        const val CHARGE_FOR_CHAT35_PROMPT = "charge for chat35 prompt"
        const val PROMPT_PROCESSED_SUCCESSFULLY = "Prompt processed successfully"
        const val UNEXPECTED_ERROR_OCCURRED = "An unexpected error occurred"
    }

    @RegexCommandHandler("^$CHET_COMMAND .*")
    suspend fun chetChat(bot: TelegramBot, messageUpdate: MessageUpdate) {
        val prompt = messageUpdate.text.replace(CHET_COMMAND, "").trim()
        val request = PromptRequest(telegramId = messageUpdate.user.id, prompt = prompt, isImage = false)

        val responseEntity = mono {
            try {
                val result = promptService.processPrompt(request).awaitSingleOrNull()
                ResponseEntity.ok(
                    HttpResponse(
                        error = null,
                        success = SimpleStringResponseEntity(result ?: PROMPT_PROCESSED_SUCCESSFULLY)
                    )
                )
            } catch (error: Exception) {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    HttpResponse(error = error.localizedMessage, success = null)
                )
            }
        }.awaitSingleOrNull()

        val messageContent = responseEntity?.body?.success?.message ?: responseEntity?.body?.error ?: UNEXPECTED_ERROR_OCCURRED
        val escapedString = escapeMarkdownV2(messageContent)

        val messageFooter =
            "\uD83D\uDCAC [Telegram](https://t.me/chetverify) \uD83D\uDCC8 [Dexscreener](https://dexscreener.com/solana/hdkb6ksckptssrutdnddtuqkx1pg2teocr2v67qm9gqt)"

        sendMessage(bot, messageUpdate, escapedString, messageFooter)

        creditService.queryTransaction(
            messageUpdate.user.id,
            queryCostId = QueryCostId.CHAT35,
            CHARGE,
            CHARGE_FOR_CHAT35_PROMPT
        ).awaitSingleOrNull()
    }

    private suspend fun sendMessage(bot: TelegramBot, messageUpdate: MessageUpdate, messageContent: String, messageFooter: String) {
        message {
            String.format("%s \r\n\r\n%s", messageContent, messageFooter)
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
