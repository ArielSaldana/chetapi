package io.unreal.chet.chetapi.tgcontroller

import eu.vendeli.tgbot.TelegramBot
import eu.vendeli.tgbot.annotations.CommandHandler
import eu.vendeli.tgbot.api.message.message
import eu.vendeli.tgbot.types.User
import io.unreal.chet.chetapi.error.UserExistsError
import io.unreal.chet.chetapi.objects.CreateUserRequest
import io.unreal.chet.chetapi.objects.HttpResponse
import io.unreal.chet.chetapi.objects.SimpleStringResponseEntity
import io.unreal.chet.chetapi.services.UserService
import kotlinx.coroutines.reactor.awaitSingleOrNull
import kotlinx.coroutines.reactor.mono
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.stereotype.Component

@Component
class RegisterController(val userService: UserService) {

    @CommandHandler(["/register"])
    suspend fun register(user: User, bot: TelegramBot) {
        val request = CreateUserRequest(user.id, null)
        val responseEntity = mono {
            try {
                userService.createUserInBothTables(request).awaitSingleOrNull()
                ResponseEntity.ok(
                    HttpResponse(
                        error = null,
                        success = SimpleStringResponseEntity("User created")
                    )
                )
            } catch (ex: Exception) {
                val errorResponseEntity = when (ex) {
                    is UserExistsError -> HttpResponse(error = ex.message, success = null)
                    is IllegalArgumentException -> HttpResponse(error = ex.message, success = null)
                    else -> HttpResponse(error = "An unexpected error occurred", success = null)
                }
                ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseEntity)
            }
        }.awaitSingleOrNull()

        val messageContent = when {
            responseEntity?.body?.success != null -> responseEntity.body?.success?.message ?: "User created"
            responseEntity?.body?.error != null -> responseEntity.body?.error ?: "An error occurred"
            else -> "An unexpected error occurred"
        }

        message { messageContent }.send(user, bot)
    }
}
