package io.unreal.chet.chetapi.error

class UserBalanceNotFoundError(message: String = "User balance entry was not found") : RuntimeException(message)
