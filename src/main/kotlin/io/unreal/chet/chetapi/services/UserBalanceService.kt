package io.unreal.chet.chetapi.services

import com.mongodb.DuplicateKeyException
import io.unreal.chet.chetapi.error.UserBalanceAlreadyExistsError
import io.unreal.chet.chetapi.error.UserBalanceNotFoundError
import io.unreal.chet.chetapi.repository.mongo.UserBalance
import io.unreal.chet.chetapi.repository.mongo.UserBalanceRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

@Service
class UserBalanceService(
    private val userService: UserService,
    private val userBalanceRepository: UserBalanceRepository) {

    fun createUserBalanceDBEntry(uid: UUID, balance: Int): Mono<UserBalance> {
        return userBalanceRepository.save(UserBalance(uid = uid, creditBalance = balance))
            .onErrorResume { ex: Throwable ->
                when (ex) {
                    is DuplicateKeyException -> {
                        // Handle duplicate key exception
                        println("Duplicate key error: ${ex.message}")
                        Mono.error(UserBalanceAlreadyExistsError("User balance entry already exists for user ID: $uid"))
                    }
                    else -> {
                        // Handle other exceptions
                        println("Error occurred while creating user balance entry: ${ex.message}")
                        Mono.error(ex)
                    }
                }
            }
            .doOnError { ex ->
                // Log the error
                println("Error occurred while creating user balance entry: ${ex.message}")
            }
    }

    fun createUserBalanceDBEntryWithTelegramId(telegramId: Long, balance: Int): Mono<UserBalance> {
        return userService.getUserUidWithTelegramId(telegramId)
            .flatMap { userUid ->
                createUserBalanceDBEntry(userUid, balance)
            }
    }

    fun getUserCreditBalance(telegramId: Long): Mono<Int> {
        return userService.getUserUidWithTelegramId(telegramId)
            .flatMap { userUid ->
                userBalanceRepository.findByUid(userUid)
                    .map { userBalance ->
                        userBalance.creditBalance
                    }
            }.switchIfEmpty(Mono.error(UserBalanceNotFoundError()))
    }

    fun updateUserBalance(uid: UUID, amount: Int): Mono<UserBalance> {
        return userBalanceRepository.findByUid(uid)
            .flatMap { userBalance ->
                val newBalance = userBalance.creditBalance + amount
                userBalance.creditBalance = newBalance
                userBalanceRepository.save(userBalance)
            }
            .switchIfEmpty(Mono.error(UserBalanceNotFoundError("Could not update user balance")))
    }
}
