package io.unreal.chet.chetapi.controller

import io.unreal.chet.chetapi.repository.mongo.UserRepository
import io.unreal.chet.chetapi.services.PromptService
import org.springframework.web.bind.annotation.RequestMapping
import org.springframework.web.bind.annotation.RestController

@RestController
@RequestMapping("/prompt")
class PromptController(
    val userRepository: UserRepository,
    private val promptService: PromptService
) {
}
