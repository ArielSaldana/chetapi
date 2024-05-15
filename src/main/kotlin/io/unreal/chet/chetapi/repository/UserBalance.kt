package io.unreal.chet.chetapi.repository

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.CassandraType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository
import java.util.*

@Table(value = "user_balance")
data class UserBalance(
    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val id: UUID,

    @Column(value = "creditbalance", forceQuote = true)
    val creditBalance: Long,

    @Column("modified")
    var modified: Date
)

@Repository
interface UserBalanceRepository : ReactiveCassandraRepository<UserBalance, UUID>
