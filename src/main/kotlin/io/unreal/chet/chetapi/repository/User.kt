package io.unreal.chet.chetapi.repository

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.*
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.stereotype.Repository
import java.util.Date
import java.util.UUID

@Table(value = "user")
data class User(
    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val id: UUID,

    @CassandraType(type = CassandraType.Name.BIGINT)
    @Column(value = "telegram_id", forceQuote = true)
    val telegramId: Long?,

    @Column("solana_wallet_address")
    var solanaWalletAddress: String?,

    @Column("created")
    val created: Date,

    @Column("modified")
    var modified: Date
)

@Repository
interface UserRepository : ReactiveCassandraRepository<User, UUID>
