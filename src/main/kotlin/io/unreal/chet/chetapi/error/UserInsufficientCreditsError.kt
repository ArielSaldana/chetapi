package io.unreal.chet.chetapi.error

class UserInsufficientCreditsError(message: String= "oops! You don't have enough credits for this query!"): RuntimeException(message)


