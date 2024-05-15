package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.RegexCommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.ParseMode
import eu.vendeli.tgbot.types.internal.MessageUpdate
import io.unreal.chet.chetapi.objects.HttpResponse
import io.unreal.chet.chetapi.objects.PromptRequest
import io.unreal.chet.chetapi.objects.SimpleStringResponseEntity
import io.unreal.chet.chetapi.services.PromptService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class ChetChatController(val promptService: PromptService) {

    @RegexCommandHandler("^/chet .*")
    suspend fun chetChat(bot: TelegramBot, messageUpdate: MessageUpdate) {
        val prompt = messageUpdate.text.replace("/chet", "").trim()
        val request = PromptRequest(telegramId = messageUpdate.user.id, prompt = prompt, isImage = false)

        val responseEntity = mono {
            try {
                val result = promptService.processPrompt(request, 20).awaitSingleOrNull()
                ResponseEntity.ok(
                    HttpResponse(
                        error = null,
                        success = SimpleStringResponseEntity(result ?: "Prompt processed successfully")
                    )
                )
            } catch (error: Exception) {
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(
                    HttpResponse(error = error.message, success = null)
                )
            }
        }.awaitSingleOrNull()

        val messageContent = when {
            responseEntity?.body?.success != null -> responseEntity.body?.success?.message ?: "Prompt processed successfully"
            responseEntity?.body?.error != null -> responseEntity.body?.error ?: "An error occurred"
            else -> "An unexpected error occurred"
        }

        val escapedString = messageContent
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

        val messageFooter =
            "\uD83D\uDCAC [Telegram](https://t.me/chetverify) \uD83D\uDCC8 [Dexscreener](https://dexscreener.com/solana/hdkb6ksckptssrutdnddtuqkx1pg2teocr2v67qm9gqt)"


        message {
            String.format("%s \r\n\r\n%s", escapedString, messageFooter)
        }
            .options {
                parseMode = ParseMode.MarkdownV2
                linkPreviewOptions { isDisabled = true }
                replyParameters(messageId = messageUpdate.message.messageId)
            }
            .send(to = messageUpdate.message.chat.id, bot)
    }
}
