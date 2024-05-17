package io.unreal.chet.chetapi.repository.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository
import java.util.*

@Document(collection = "credit_transactions")
data class CreditTransactions(
    @Id
    val transactionId: UUID,

    @Field("uid")
    val uid: UUID,

    @Field("amount")
    var amount: Int,

    @Field("transaction_type")
    val transactionType: String,

    @Field("description")
    val description: String,

    @Field("created")
    val created: Date
)

@Repository
interface CreditTransactionsRepository : ReactiveMongoRepository<CreditTransactions, UUID>
