package io.unreal.chet.chetapi.objects

data class PromptRequest(
    val prompt: String,
    val telegramId: Long,
    val isImage: Boolean
)
