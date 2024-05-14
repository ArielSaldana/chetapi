package io.unreal.chet.chetapi.objects

data class CreateUserRequest(
    val telegramId: Long,
    val solanaAddress: String?
)
