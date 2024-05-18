package io.unreal.chet.chetapi.repository.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.Query
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.Date
import java.util.UUID

@Document(collection = "prompt")
data class Prompt(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Field("user_id")
    val userId: UUID,

    @Field("prompt")
    val prompt: String,

    @Field("is_image")
    val isImage: Boolean,

    @Field("created")
    val created: Date = Date(),

    @Field("modified")
    val modified: Date = Date()
)

@Repository
interface PromptRepository : ReactiveMongoRepository<Prompt, UUID> {

    @Query("{ 'userid': ?0, 'created': { \$gt: ?1 } }")
    fun getNumberOfPromptsLast24hByUserId(userid: UUID, date: Date): Mono<Int>

    fun save(prompt: Prompt): Mono<Prompt>
}
