package io.unreal.chet.chetapi.objects

enum class TransactionType(val type: String) {
    CREDIT("credit"),
    SIGNUP_CREDIT("signup_credit"),
    SPEND("spend")
}
