package io.unreal.chet.chetapi.repository

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository
import java.util.*

@Table(value = "credit_transactions")
data class CreditTransactions(
    @PrimaryKeyColumn(name = "transactionid", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val transactionId: UUID,

    @PrimaryKeyColumn(name = "uid", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val uid: UUID,

    @Column("amount")
    var amount: Int,

    @Column(value = "transactiontype")
    val transactiontype: String,

    @Column(value = "description")
    val description: String,

    @Column("created")
    val created: Date
)

@Repository
interface CreditTransactionsRepository : ReactiveCassandraRepository<CreditTransactions, UUID>
