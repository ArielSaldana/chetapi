package io.unreal.chet.chetapi.services

import io.unreal.chet.chetapi.error.UserExistsError
import io.unreal.chet.chetapi.error.UserNotFoundError
import io.unreal.chet.chetapi.externalservices.OpenAIClient
import io.unreal.chet.chetapi.objects.PromptRequest
import io.unreal.chet.chetapi.repository.Prompt
import io.unreal.chet.chetapi.repository.PromptRepository
import io.unreal.chet.chetapi.repository.UserByTelegramIdRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.dao.DataIntegrityViolationException
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service
class PromptService(
    private val userService: UserService,
    private val userByTelegramIdRepository: UserByTelegramIdRepository,
    private val promptRepository: PromptRepository
) {

    fun shouldUserHaveAccess(promptRequest: PromptRequest): Mono<Boolean> {
        return userService.getUserUidWithTelegramId(promptRequest.telegramId)
            .switchIfEmpty(Mono.error(UserNotFoundError("User does not exist")))
            .flatMap { user ->
                val date24HoursAgo = Date.from(Instant.now().minusSeconds(24 * 60 * 60))
                promptRepository.getNumberOfPromptsLast24hByUserId(user, date24HoursAgo)
                    .flatMap { numberOfPrompts ->
                        if (numberOfPrompts.toInt() >= 5) {
                            Mono.just(false)
                        } else {
                            Mono.just(true)
                        }
                    }
            }
    }

//    fun savePrompt(promptRequest: PromptRequest, isImage: Boolean): Mono<Boolean> {
//        return userService.getUserUidWithTelegramId(promptRequest.telegramId)
//            .switchIfEmpty(Mono.error(UserNotFoundError("User does not exist")))
//            .flatMap { user ->
//                val currentDate = Date()
//                val prompt = Prompt(
//                    UUID.randomUUID(),
//                    user,
//                    promptRequest.prompt,
//                    isImage,
//                    currentDate,
//                    currentDate
//                )
//                promptRepository.save(prompt)
//                    .flatMap { savedPrompt ->
//                        performAdditionalWork()
//                    }
//            }
//    }

//    fun savePrompt(promptRequest: PromptRequest, isImage: Boolean): Mono<Boolean> {
//        return userService.getUserUidWithTelegramId(promptRequest.telegramId)
//            .switchIfEmpty(Mono.error(UserNotFoundError("User does not exist")))
//            .flatMap { user ->
//                val currentDate = Date()
//                val prompt = Prompt(
//                    UUID.randomUUID(),
//                    user,
//                    promptRequest.prompt,
//                    isImage,
//                    currentDate,
//                    currentDate
//                )
//                promptRepository.save(prompt)
//                    .onErrorResume { ex ->
//                        if (ex is DataIntegrityViolationException) {
//                            println("ERROR happENED")
//                            Mono.error(UserExistsError("User with given key already exists"))
//                        } else {
//                            println("ERROR happENED")
//                            Mono.error(ex)
//                        }
//                    }
//                    .thenReturn(true)
//            }
//    }

    fun savePrompt(promptRequest: PromptRequest, isImage: Boolean): Mono<Boolean> {
        return userService.getUserUidWithTelegramId(promptRequest.telegramId)
            .switchIfEmpty(Mono.error(UserNotFoundError("User does not exist")))
            .flatMap { user ->
                promptRepository.insertPrompt(user, promptRequest.prompt, isImage)
                    .thenReturn(true)
            }
    }

    suspend fun processPrompt(promptRequest: PromptRequest): Mono<String> {
        return shouldUserHaveAccess(promptRequest)
            .flatMap { shouldHaveAccess ->
                if (shouldHaveAccess) {
                    mono {
                        val client = OpenAIClient()
                        try {
                            val chatResponse = client.getChat(promptRequest.prompt)
                            savePrompt(promptRequest, false).awaitSingle()
                            chatResponse
                        } catch (e: Exception) {
                            savePrompt(promptRequest, false).awaitSingle()
                            throw e
                        }
                    }
                } else {
                    Mono.error<String>(RuntimeException("User does not have access"))
                }
            }
    }

    private fun performAdditionalWork(): Mono<Boolean> {
        return Mono.just(true) // Replace with your actual result
    }
}
