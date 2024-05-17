package io.unreal.chet.chetapi.repository.mongo

import org.springframework.data.annotation.Id
import org.springframework.data.mongodb.core.mapping.Document
import org.springframework.data.mongodb.core.mapping.Field
import org.springframework.data.mongodb.repository.ReactiveMongoRepository
import org.springframework.stereotype.Repository

@Document(collection = "query_cost")
data class QueryCost(
    @Id
    val queryId: Int,

    @Field("query_name")
    var queryName: String,

    @Field("query_description")
    var queryDescription: String,

    @Field("credit_cost")
    var creditCost: Int
)

@Repository
interface QueryCostRepository : ReactiveMongoRepository<QueryCost, Int>
