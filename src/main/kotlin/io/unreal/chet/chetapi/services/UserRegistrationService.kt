package io.unreal.chet.chetapi.services

import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

/*
 * When a new user signs up, we create an entry into the user table, and user balance table.
 */
@Service
class UserRegistrationService(
    private val userService: UserService,
    private val userBalanceService: UserBalanceService
) {
    /**
     * Register a new user with a specific Telegram ID.
     * This involves creating a new user in the user table and a corresponding entry in the user balance table.
     * @param telegramId The Telegram ID of the new user.
     * @return The UUID of the new user.
     */
    fun registerUserWithTelegramId(telegramId: Long): Mono<UUID> {
        return userService.createUserWithTelegramId(telegramId).flatMap { userId ->
            userBalanceService.createUserBalanceDBEntryWithTelegramId(telegramId, balance = 0).map { userId }
        }.onErrorResume { ex ->
            Mono.error(RuntimeException("Error occurred while registering user: ${ex.message}", ex))
        }
    }
}
