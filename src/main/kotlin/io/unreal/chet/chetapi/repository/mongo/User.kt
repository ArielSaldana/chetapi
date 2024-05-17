package io.unreal.chet.chetapi.repository.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import java.util.Date
import java.util.UUID
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono

@Document(collection = "user")
data class User(
    @Id
    val id: UUID = UUID.randomUUID(),

    @Indexed(unique = true)
    @Field("telegram_id")
    val telegramId: Long?,

    @Field("solana_wallet_address")
    var solanaWalletAddress: String?,

    @Field("created")
    val created: Date,

    @Field("modified")
    var modified: Date
)

@Repository
interface UserRepository : ReactiveMongoRepository<User, UUID> {
    fun findByTelegramId(telegramId: Long): Mono<User>
}


