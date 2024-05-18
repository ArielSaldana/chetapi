package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import io.unreal.chet.chetapi.error.UserBalanceNotFoundError
import io.unreal.chet.chetapi.error.UserExistsError
import io.unreal.chet.chetapi.objects.HttpResponse
import io.unreal.chet.chetapi.services.UserBalanceService
import io.unreal.chet.chetapi.services.UserService
import kotlinx.coroutines.reactor.awaitSingle
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component
import reactor.core.publisher.Mono

@Component // This annotation is necessary for the correct functioning of the spring DI
class HelloController(private val userService: UserService, private val userBalanceService: UserBalanceService) {
    @CommandHandler(["/start"])
    suspend fun start(user: User, bot: TelegramBot) {

        try {
            val userBalance = userBalanceService.createUserBalanceDBEntryWithTelegramId(user.id, 10).awaitSingle()
            println("User balance entry created successfully: $userBalance")
            // Continue with other operations
        } catch (ex: Exception) {
            println("Failed to create user balance entry: ${ex.message}")
            // Handle the error accordingly
        }


        try {
            val userBalance = userBalanceService.createUserBalanceDBEntryWithTelegramId(user.id, 10).awaitSingle()
            println("User balance entry created successfully: $userBalance")
            // Continue with other operations
        } catch (ex: Exception) {
            println("Failed to create user balance entry: ${ex.message}")
            // Handle the error accordingly
        }


//        val msg = userService.getUserUidWithTelegramId(user.id).flatMap {
//            uid ->
//            userBalanceService.getUserCreditBalance(telegramId = user.id).onErrorResume {
//                ex ->
//                if (ex is UserBalanceService) {
//                    userBalanceService.
//                }
//            }
//
////            userBalanceService.updateUserBalance(uid, 10)
//        }.onErrorResume { ex ->
//            val errorResponseEntity = when (ex) {
//                is UserBalanceNotFoundError -> "UserBalanaceNotFound"
//                is IllegalArgumentException -> "Illegal"
//                else -> "wow"
//            }
//            Mono.just(0)
//        }

//        println(String.format("WHATS HAPPENING %s", msg.toString()))
        message { "Hello!" }.send(user, bot)
    }
}
