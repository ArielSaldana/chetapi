package io.unreal.chet.chetapi.repository

import org.springframework.data.cassandra.core.ReactiveCassandraTemplate
import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKey
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.util.*

@Table(value = "query_cost")
data class QueryCost(
    @PrimaryKey
    val queryId: Int,

    @Column(value = "queryname")
    var queryName: String,

    @Column(value = "querydescription")
    var queryDescription: String,

    @Column("cost")
    var cost: Int
)

@Repository
interface QueryCostRepository : ReactiveCassandraRepository<QueryCost, Int>

//interface CustomQueryCostRepository {
//    fun upsertQueryCost(queryCost: QueryCost): Mono<QueryCost>
//}
//
//class CustomQueryCostRepositoryImpl(
//    private val cassandraTemplate: ReactiveCassandraTemplate
//) : CustomQueryCostRepository {
//
//    override fun upsertQueryCost(queryCost: QueryCost): Mono<QueryCost> {
//        return cassandraTemplate.selectOneById(queryCost.queryId, QueryCost::class.java)
//            .flatMap { existingCreditCost ->
//                existingCreditCost.queryName = queryCost.queryName
//                existingCreditCost.queryDescription = queryCost.queryDescription
//                existingCreditCost.cost = queryCost.cost
//                cassandraTemplate.update(existingCreditCost)
//            }
//            .switchIfEmpty(cassandraTemplate.insert(queryCost))
//    }
//}
