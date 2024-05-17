package io.unreal.chet.chetapi.services

import io.unreal.chet.chetapi.error.UserExistsError
import io.unreal.chet.chetapi.objects.CreateUserRequest
import io.unreal.chet.chetapi.repository.mongo.User
import io.unreal.chet.chetapi.repository.mongo.UserRepository
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
) {
    fun getAllUsers(): Flux<User?> {
        return userRepository.findAll()
    }

    fun getUserWithUid(id: UUID): Mono<User?> {
        return userRepository.findById(id)
    }

    fun getUserUidWithTelegramId(telegramId: Long): Mono<UUID> {
        return userRepository.findByTelegramId(telegramId)
            .map { it.id }
    }

    private fun checkUserExists(telegramId: Long): Mono<Void> {
        return userRepository.findByTelegramId(telegramId)
            .flatMap {
                Mono.error<Void>(UserExistsError("User already exists"))
            }
            .switchIfEmpty(Mono.empty()) // Completes successfully if no user is found
    }

    private fun saveUser(createUserRequest: CreateUserRequest): Mono<Void> {
        val uuid = UUID.randomUUID()
        val currentDate = Date()

        val user = User(
            telegramId = createUserRequest.telegramId,
            solanaWalletAddress = "",
            created = currentDate,
            modified = currentDate
        )

        return userRepository.save(user)
            .onErrorResume { ex ->
                if (ex is DataIntegrityViolationException) {
                    Mono.error(UserExistsError("User with given key already exists"))
                } else {
                    Mono.error(ex)
                }
            }
            .then()
    }

    @Transactional
    fun createUser(createUserRequest: CreateUserRequest): Mono<Void> {
        return checkUserExists(createUserRequest.telegramId)
            .then(saveUser(createUserRequest))
    }
}
