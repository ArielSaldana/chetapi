package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.annotations.RegexCommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import eu.vendeli.tgbot.types.internal.MessageUpdate
import org.springframework.stereotype.Component

@Component
class MyId() {
    companion object {
        const val MY_ID_COMMAND = "/id"
        const val FIND_ID_COMMAND = "/fid"
        const val FAILED_TO_FIND_USERID_ERROR_MESSAGE = "Failed to get user id, user might have telegram privacy settings enabled"
        const val NULL_STRING = "null"
    }

    @CommandHandler([MY_ID_COMMAND])
    suspend fun myId(bot: TelegramBot, up: MessageUpdate, user: User) {
        message { user.id.toString() }
            .options { replyParameters(messageId = up.message.messageId) }
            .send(up.message.chat.id, bot)
    }

    @RegexCommandHandler("^$FIND_ID_COMMAND .*")
    suspend fun findId(bot: TelegramBot, up: MessageUpdate, user: User) {
        var failedToGetUserId = true
        val userId = up.message.entities!![1].user?.id.toString()

        if (userId.isNotEmpty() && userId != NULL_STRING) {
            message { userId }
                .options { replyParameters(messageId = up.message.messageId) }
                .send(up.message.chat.id, bot)
            failedToGetUserId = false
        }

        if (failedToGetUserId) {
            message { FAILED_TO_FIND_USERID_ERROR_MESSAGE  }
                .options { replyParameters(messageId = up.message.messageId) }
                .send(up.message.chat.id, bot)
        }
    }
}
