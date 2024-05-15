package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.RegexCommandHandler
import eu.vendeli.tgbot.api.media.sendPhoto
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
class ChetImgController(val promptService: PromptService) {

    @RegexCommandHandler("^/chetimg .*")
    suspend fun chetChat(bot: TelegramBot, messageUpdate: MessageUpdate) {
        val prompt = messageUpdate.text.replace("/chetimg", "").trim()
        val request = PromptRequest(telegramId = messageUpdate.user.id, prompt = prompt, isImage = true)

        val responseEntity = mono {
            try {
                val result = promptService.processPrompt(request, 10).awaitSingleOrNull()
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

        if (responseEntity?.body?.success != null) {
            sendPhoto { responseEntity.body?.success?.message ?: "Prompt processed successfully" }
                .options {
                    parseMode = ParseMode.MarkdownV2
                    replyParameters(messageId = messageUpdate.message.messageId)
                }
                .sendAsync(to = messageUpdate.message.chat.id, bot)
        } else {
            val messageContent = responseEntity?.body?.error ?: "An unexpected error occurred"
            message { messageContent }
                .options {
                    parseMode = ParseMode.MarkdownV2
                    replyParameters(messageId = messageUpdate.message.messageId)
                }
                .sendAsync(to = messageUpdate.message.chat.id, bot)
        }
    }
}
