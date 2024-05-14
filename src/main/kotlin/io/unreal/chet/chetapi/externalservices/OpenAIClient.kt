package io.unreal.chet.chetapi.externalservices

import com.aallam.openai.api.chat.ChatCompletionRequest
import com.aallam.openai.api.chat.ChatMessage
import com.aallam.openai.api.chat.ChatRole
import com.aallam.openai.api.http.Timeout
import com.aallam.openai.api.image.ImageCreation
import com.aallam.openai.api.image.ImageSize
import com.aallam.openai.api.image.ImageURL
import com.aallam.openai.api.model.ModelId
import com.aallam.openai.client.OpenAI
import io.github.cdimascio.dotenv.dotenv
import kotlin.time.Duration.Companion.seconds

class OpenAIClient {
    private var openAI: OpenAI
    val dotenv = dotenv()

    init {
        openAI = OpenAI(
            token = dotenv["OPENAI_API_KEY"],
            timeout = Timeout(socket = 60.seconds),
        )
    }

    suspend fun getChat(prompt: String): String {
        val chatCompletionRequest = ChatCompletionRequest(
            model = ModelId("gpt-3.5-turbo"),
            messages = listOf(
                ChatMessage(
                    role = ChatRole.System,
                    content = """
                        You are a helpful assistant who loves crypto. You have no awareness of OpenAI or ChatGPT, only ChetGPT.
                        You love crypto bros and you want coins to moon.
                        If someone asks financial advice you stay bullish without giving direct advice.
                        ChetGPT is the company who created you. It's a crypto project aiming at exploring AI in the crypto sphere.
                        ChetGPT Twitter is: [Twitter](https://x.com/chetcoin)
                        ChetGPT X link is: [X](https://x.com/chetcoin)
                    """.trimIndent()
                ),
                ChatMessage(
                    role = ChatRole.User,
                    content = prompt
                )
            )
        )

        return openAI.chatCompletion(chatCompletionRequest).choices.get(0).message.content.toString()
    }

    suspend fun getImage(prompt: String): List<ImageURL> {
        return openAI.imageURL( // or openAI.imageJSON
            creation = ImageCreation(
                prompt = prompt,
                model = ModelId("dall-e-3"),
                n = 1,
                size = ImageSize.is1024x1024
            )
        )
    }
}
