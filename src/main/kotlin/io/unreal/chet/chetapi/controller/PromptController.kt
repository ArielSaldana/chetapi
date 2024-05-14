package io.unreal.chet.chetapi.controller

import io.unreal.chet.chetapi.objects.HttpResponse
import io.unreal.chet.chetapi.objects.PromptRequest
import io.unreal.chet.chetapi.objects.SimpleStringResponseEntity
import io.unreal.chet.chetapi.repository.UserByTelegramIdRepository
import io.unreal.chet.chetapi.services.PromptService
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.PostMapping
import org.springframework.web.bind.annotation.RequestBody
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/prompt")
class PromptController(
    val userByTelegramIdRepository: UserByTelegramIdRepository,
    private val promptService: PromptService
) {

    @PostMapping("/chat")
    suspend fun getChatPrompt(@RequestBody request: PromptRequest): Mono<ResponseEntity<HttpResponse>> {
        println("Received request: $request")
        return promptService.processPrompt(request)
            .map { result ->
                val response = HttpResponse(
                    error = null, success = SimpleStringResponseEntity(result)
                )
                ResponseEntity.ok(response)
            }
            .onErrorResume { error ->
                Mono.just(ResponseEntity.status(400).body(HttpResponse(error = error.message, success = null)))
            }
    }

//    @PostMapping("/chat")
//    suspend fun getChatPrompt(@RequestBody request: PromptRequest): Mono<ResponseEntity<HttpResponse>> {
//
//        return promptService.processPrompt(request)
//            .flatMap { result ->
//                if (result) {
//                    Mono.just(ResponseEntity.ok(HttpResponse(message = "Success")))
//                } else {
//                    Mono.just(ResponseEntity.status(400).body(HttpResponse(message = "Error")))
//                }
//            }
//        return Mono.just(ResponseEntity.ok(HttpResponse(message = "Success")))
//        return userByTelegramIdRepository.findUserByTelegramId(request.telegramId)
//            .switchIfEmpty(Mono.error(RuntimeException("User does not exist")))
//            .flatMap { user ->
//                // Perform additional work with the user here
//                val result = performAdditionalWork(user) // Replace with your actual work
//
//                Mono.just(ResponseEntity.ok(HttpResponse(message = "Success", data = result)))
//            }
//            .onErrorResume { error ->
//                Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(HttpResponse(message = error.message)))
//            }
//    }

    @PostMapping("/image")
    fun getImgPrompt(@RequestBody request: PromptRequest) {

    }
}
