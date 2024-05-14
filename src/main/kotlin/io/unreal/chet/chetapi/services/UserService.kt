package io.unreal.chet.chetapi.services

import io.unreal.chet.chetapi.error.UserExistsError
import io.unreal.chet.chetapi.objects.CreateUserRequest
import io.unreal.chet.chetapi.repository.User
import io.unreal.chet.chetapi.repository.UserByTelegramId
import io.unreal.chet.chetapi.repository.UserByTelegramIdRepository
import io.unreal.chet.chetapi.repository.UserRepository
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import org.springframework.transaction.annotation.Transactional
import reactor.core.publisher.Flux
import reactor.core.publisher.Mono
import java.util.Date
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
    private val userByTelegramIdRepository: UserByTelegramIdRepository
) {
    fun getAllUsers(): Flux<User?> {
        return userRepository.findAll()
    }

    fun getUserWithUid(id: UUID): Mono<User?> {
        return userRepository.findById(id)
    }

    fun getUserUidWithTelegramId(telegramId: Long): Mono<UUID> {
        return userByTelegramIdRepository.getUserUidByTelegramId(telegramId)
            .map { it.uid }
    }

    private fun checkUserExists(telegramId: Long): Mono<Void> {
        return userByTelegramIdRepository.findUserByTelegramId(telegramId)
            .flatMap {
                Mono.error<Void>(UserExistsError("User already exists"))
            }
            .switchIfEmpty(Mono.empty()) // Completes successfully if no user is found
    }

    private fun saveUserAndUserByTelegramId(createUserRequest: CreateUserRequest): Mono<Void> {
        val uuid = UUID.randomUUID()
        val currentDate = Date()

        val user = User(
            uuid,
            telegramId = createUserRequest.telegramId,
            solanaWalletAddress = "",
            modified = currentDate,
            created = currentDate
        )

        val userByTelegramId = UserByTelegramId(
            telegramId = createUserRequest.telegramId,
            uid = uuid,
        )

        return userRepository.save(user)
            .onErrorResume { ex ->
                if (ex is DataIntegrityViolationException) {
                    Mono.error(UserExistsError("User with given key already exists"))
                } else {
                    Mono.error(ex)
                }
            }
            .flatMap {
                userByTelegramIdRepository.save(userByTelegramId)
                    .onErrorResume { ex ->
                        if (ex is DataIntegrityViolationException) {
                            Mono.error(UserExistsError("User with given key already exists"))
                        } else {
                            Mono.error(ex)
                        }
                    }
            }
            .then()
    }

    @Transactional
    fun createUserInBothTables(createUserRequest: CreateUserRequest): Mono<Void> {
        return checkUserExists(createUserRequest.telegramId)
            .then(saveUserAndUserByTelegramId(createUserRequest))
    }
}
