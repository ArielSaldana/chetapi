package io.unreal.chet.chetapi.repository.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Document(collection = "user_balance")
data class UserBalance(
    @Id
    val id: UUID,

    @Field("credit_balance")
    val creditBalance: Long,

    @Field("modified")
    var modified: Date
)

@Repository
interface UserBalanceRepository : ReactiveMongoRepository<UserBalance, UUID>
