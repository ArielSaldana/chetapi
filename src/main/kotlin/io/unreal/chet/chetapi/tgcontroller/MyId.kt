package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.MessageUpdate
import org.springframework.stereotype.Component

@Component
class MyId() {
    companion object {
        const val MY_ID_COMMAND = "/id"
    }

    @CommandHandler([MY_ID_COMMAND])
    suspend fun myId(bot: TelegramBot, up: MessageUpdate, user: User) {
        message { user.id.toString() }
            .options { replyParameters(messageId = up.message.messageId) }
            .send(up.message.chat.id, bot)
    }
}
