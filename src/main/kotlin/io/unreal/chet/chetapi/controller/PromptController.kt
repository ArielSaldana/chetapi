package io.unreal.chet.chetapi.controller

import io.unreal.chet.chetapi.repository.mongo.UserRepository
import io.unreal.chet.chetapi.services.PromptService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/prompt")
class PromptController(
    val userRepository: UserRepository,
//    val userByTelegramIdRepository: UserByTelegramIdRepository,
    private val promptService: PromptService
) {

//    @PostMapping("/text")
//    suspend fun getChatPrompt(@RequestBody request: PromptRequest): Mono<ResponseEntity<HttpResponse>> {
//        return promptService.processPrompt(request, 5)
//            .map { result ->
//                val response = HttpResponse(
//                    error = null, success = SimpleStringResponseEntity(result)
//                )
//                ResponseEntity.ok(response)
//            }
//            .onErrorResume { error ->
//                Mono.just(ResponseEntity.status(400).body(HttpResponse(error = error.message, success = null)))
//            }
//    }
}
