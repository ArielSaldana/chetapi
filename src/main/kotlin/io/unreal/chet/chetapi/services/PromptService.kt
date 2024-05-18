package io.unreal.chet.chetapi.services

import io.unreal.chet.chetapi.error.UserNotFoundError
import io.unreal.chet.chetapi.externalservices.OpenAIClient
import io.unreal.chet.chetapi.objects.PromptRequest
import io.unreal.chet.chetapi.repository.mongo.Prompt
import io.unreal.chet.chetapi.repository.mongo.PromptRepository
import io.unreal.chet.chetapi.repository.mongo.QueryCostID
import io.unreal.chet.chetapi.repository.mongo.UserRepository
import kotlinx.coroutines.reactor.awaitSingle
import kotlinx.coroutines.reactor.mono
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Service
class PromptService(
    private val userService: UserService,
    private val userRepository: UserRepository,
    private val promptRepository: PromptRepository,
) {

    // Check if a user should have access based on the number of requests
    fun shouldUserHaveAccess(promptRequest: PromptRequest, numerOfRequests: Int): Mono<Boolean> {
        return userService.getUserUidWithTelegramId(promptRequest.telegramId)
            .switchIfEmpty(Mono.error(UserNotFoundError()))
            .flatMap { user ->
                val date24HoursAgo = Date.from(Instant.now().minusSeconds(24 * 60 * 60))
                promptRepository.getNumberOfPromptsLast24hByUserId(user, date24HoursAgo)
                    .flatMap { numberOfPrompts ->
                        if (numberOfPrompts.toInt() >= numerOfRequests) {
                            Mono.just(false)
                        } else {
                            Mono.just(true)
                        }
                    }
            }
    }

    // Save a prompt request
    fun savePrompt(promptRequest: PromptRequest, isImage: Boolean): Mono<Boolean> {
        return userRepository.findByTelegramId(promptRequest.telegramId)
            .switchIfEmpty(Mono.error(UserNotFoundError()))
            .flatMap { user ->
                val prompt = Prompt(
                    userId = user.id,
                    prompt = promptRequest.prompt,
                    isImage = isImage
                )
                promptRepository.save(prompt).thenReturn(true)
            }
    }

    // Process a prompt request
    suspend fun processPrompt(promptRequest: PromptRequest): Mono<String> {
        return mono {
            val client = OpenAIClient()
            try {
                val userUUID = userService.getUserUidWithTelegramId(promptRequest.telegramId).awaitSingle()
//                creditTransactionService.chargeUserForQuery(userUUID, determineQueryCostId(promptRequest)).awaitSingle()

                val response = if (promptRequest.isImage) {
                    val chatResponse = client.getImage(promptRequest.prompt)[0].url
                    savePrompt(promptRequest, promptRequest.isImage).awaitSingle()
                    chatResponse
                } else {
                    val chatResponse = client.getChat(promptRequest.prompt)
                    savePrompt(promptRequest, promptRequest.isImage).awaitSingle()
                    chatResponse
                }
                response
            } catch (e: Exception) {
                println(e)
                savePrompt(promptRequest, promptRequest.isImage).awaitSingle()
                throw RuntimeException("Failed generating prompt", e)
            }
        }
    }

    // Determine the QueryCostID based on the PromptRequest
    fun determineQueryCostId(promptRequest: PromptRequest): QueryCostID {
        return if (promptRequest.isImage) {
            QueryCostID.DALLE3
        } else {
            QueryCostID.CHAT35
        }
    }
}
