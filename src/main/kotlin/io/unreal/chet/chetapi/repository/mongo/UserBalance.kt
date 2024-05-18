package io.unreal.chet.chetapi.repository.mongo

import org.springframework.beans.factory.annotation.Autowired
import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.index.Indexed
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.core.query.Criteria
import org.springframework.data.mongodb.core.query.Query
import org.springframework.data.mongodb.core.query.Update
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Document(collection = "user_balance")
data class UserBalance(
    @Id
    val id: UUID = UUID.randomUUID(), // MongoDB ObjectId

    @Indexed(unique = true)
    @Field("uid")
    val uid: UUID, // User ID

    @Field("credit_balance")
    var creditBalance: Int,

    @Field("modified")
    var modified: Date = Date()
)

interface CustomUserBalanceRepository {
    fun updateUserBalanceByUid(uid: UUID, creditBalance: Int): Mono<UserBalance>
}

@Repository
class CustomUserBalanceRepositoryImpl @Autowired constructor(
    private val mongoTemplate: ReactiveMongoTemplate
) : CustomUserBalanceRepository {

    override fun updateUserBalanceByUid(uid: UUID, creditBalance: Int): Mono<UserBalance> {
        val query = Query(Criteria.where("uid").`is`(uid))
        val update = Update().set("creditBalance", creditBalance).set("modified", Date())

        return mongoTemplate.findAndModify(query, update, UserBalance::class.java)
    }
}

@Repository
interface UserBalanceRepository : ReactiveMongoRepository<UserBalance, UUID>, CustomUserBalanceRepository {
    fun findByUid(uid: UUID): Mono<UserBalance>
}
