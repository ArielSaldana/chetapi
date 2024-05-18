package io.unreal.chet.chetapi.error

class UserExceededCreditError(message: String = "oops, you don't have enough credits for this operation!"): RuntimeException(message)
