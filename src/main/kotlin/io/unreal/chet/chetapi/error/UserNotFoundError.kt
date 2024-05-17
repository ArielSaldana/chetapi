package io.unreal.chet.chetapi.error

class UserNotFoundError(message: String = "\"Oops! It looks like you're not registered yet. To join our community, please type /register and follow the steps. If you need any assistance, just let us know!") : RuntimeException(message)
