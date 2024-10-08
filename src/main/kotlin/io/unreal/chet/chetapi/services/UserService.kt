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
import java.util.UUID

@Service
class UserService(
    private val userRepository: UserRepository,
) {
    // Fetch all users from the database
    fun getAllUsers(): Flux<User?> {
        return userRepository.findAll()
    }

    // Fetch a user with a specific UUID from the database
    fun getUserWithUid(id: UUID): Mono<User?> {
        return userRepository.findById(id)
    }

    // Fetch a user's UUID with a specific Telegram ID from the database
    fun getUserUidWithTelegramId(telegramId: Long): Mono<UUID> {
        return userRepository.findByTelegramId(telegramId)
            .map { it.id }
    }

    // Save a new user to the database
    private fun saveUser(createUserRequest: CreateUserRequest): Mono<User> {
        val user = User(
            id = UUID.randomUUID(),
            telegramId = createUserRequest.telegramId,
            solanaWalletAddress = "",
        )

        return userRepository.save(user)
            .onErrorResume { ex ->
                if (ex is DataIntegrityViolationException) {
                    Mono.error(UserExistsError("User with given key already exists"))
                } else {
                    Mono.error(ex)
                }
            }
    }

    // Create a new user with a specific Telegram ID
    @Transactional
    fun createUserWithTelegramId(telegramId: Long): Mono<UUID> {
        return createUser(CreateUserRequest(telegramId =  telegramId, solanaAddress = null))
    }

    // Create a new user
    @Transactional
    fun createUser(createUserRequest: CreateUserRequest): Mono<UUID> {
        return checkUserExists(createUserRequest.telegramId)
            .then(saveUser(createUserRequest))
            .map { user -> user.id } // Assuming `id` is the field for `userId`
    }

    // Check if a user with a specific Telegram ID exists
    fun checkUserExists(telegramId: Long): Mono<Void> {
        return userRepository.findByTelegramId(telegramId)
            .flatMap { user ->
                if (user != null) {
                    Mono.error<Void>(RuntimeException("User already exists"))
                } else {
                    Mono.empty<Void>()
                }
            }
    }
}
