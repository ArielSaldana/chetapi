package io.unreal.chet.chetapi.controller

import io.unreal.chet.chetapi.error.UserExistsError
import io.unreal.chet.chetapi.objects.CreateUserRequest
import io.unreal.chet.chetapi.objects.HttpResponse
import io.unreal.chet.chetapi.objects.SimpleStringResponseEntity
import io.unreal.chet.chetapi.services.UserService
import org.springframework.http.HttpStatus
import org.springframework.http.ResponseEntity
import org.springframework.web.bind.annotation.*
import reactor.core.publisher.Mono

@RestController
@RequestMapping("/user")
class UserController(val userService: UserService) {

    @PostMapping("/create")
    fun createUser(@RequestBody request: CreateUserRequest): Mono<ResponseEntity<HttpResponse>> {
        return userService.createUserInBothTables(request)
            .then(Mono.fromCallable {
                val response = HttpResponse(error = null, success = SimpleStringResponseEntity("User created"))
                ResponseEntity.ok(response)
            })
            .onErrorResume { ex ->
                val errorResponseEntity = when (ex) {
                    is UserExistsError -> HttpResponse(error = ex.message, success = null)
                    is IllegalArgumentException -> HttpResponse(error = ex.message, success = null)
                    else -> HttpResponse(error = "An unexpected error occurred", success = null)
                }
                Mono.just(ResponseEntity.status(HttpStatus.BAD_REQUEST).body(errorResponseEntity))
            }
    }
}
