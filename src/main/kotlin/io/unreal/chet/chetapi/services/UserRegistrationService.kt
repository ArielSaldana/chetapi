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
    fun registerUserWithTelegramId(telegramId: Long): Mono<UUID> {
        return userService.createUserWithTelegramId(telegramId).flatMap {
            userBalanceService.createUserBalanceDBEntryWithTelegramId(telegramId, balance = 0).flatMap { userBalance ->
                Mono.just(userBalance.uid)
            }
        }
    }
}
