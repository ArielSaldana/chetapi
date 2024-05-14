package io.unreal.chet.chetapi.repository

import org.springframework.data.cassandra.core.cql.PrimaryKeyType
import org.springframework.data.cassandra.core.mapping.Column
import org.springframework.data.cassandra.core.mapping.PrimaryKeyColumn
import org.springframework.data.cassandra.core.mapping.Table
import org.springframework.data.cassandra.repository.Query
import org.springframework.data.cassandra.repository.ReactiveCassandraRepository
import org.springframework.data.repository.query.Param
import org.springframework.stereotype.Repository
import reactor.core.publisher.Mono
import java.time.Instant
import java.util.*

@Table(value = "user_by_telegram_id")
data class UserByTelegramId(
    @PrimaryKeyColumn(name = "telegramid", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val telegramId: Long,

    @Column("uid")
    val uid: UUID,
)

@Repository
interface UserByTelegramIdRepository : ReactiveCassandraRepository<UserByTelegramId, Long> {
    @Query("SELECT * FROM user_by_telegram_id WHERE telegramid = :telegramid")
    fun findUserByTelegramId(
        @Param("telegramid") telegramId: Long
    ): Mono<UserByTelegramId>

    @Query("SELECT * FROM user_by_telegram_id WHERE telegramid = :telegramid")
    fun getUserUidByTelegramId(
        @Param("telegramid") telegramId: Long
    ): Mono<UserByTelegramId>
}

