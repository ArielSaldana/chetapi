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
    @CommandHandler(["/balance"])
    suspend fun balance(user: User, bot: TelegramBot, messageUpdate: MessageUpdate) {
        val userCreditBalance = userBalanceService.getUserCreditBalance(messageUpdate.user.id)
            .awaitFirstOrNull()

        if (userCreditBalance != null) {
            message { "You currently have $userCreditBalance credits!" }.send(user, bot)
        } else {
            message { "Could not retrieve your balance." }.send(user, bot)
        }
    }
}
