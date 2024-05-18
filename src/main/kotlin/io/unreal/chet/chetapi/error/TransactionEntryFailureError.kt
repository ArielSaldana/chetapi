package io.unreal.chet.chetapi.error

class TransactionEntryFailureError(message: String = "Failed to add user transaction"): RuntimeException(message)
