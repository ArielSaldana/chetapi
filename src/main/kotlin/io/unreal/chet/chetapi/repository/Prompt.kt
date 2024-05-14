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
import java.util.Date
import java.util.UUID

@Table("prompt")
data class Prompt (
    @PrimaryKeyColumn(name = "id", ordinal = 0, type = PrimaryKeyType.PARTITIONED)
    val id: UUID,
    @PrimaryKeyColumn(name = "userid", ordinal = 0, type = PrimaryKeyType.CLUSTERED)
    val userid: UUID,
    @Column("prompt")
    val prompt: String,
    @Column("isimage")
    val isImage: Boolean,
    @Column("created")
    val created: Date,
    @Column("modified")
    val modified: Date
)

@Repository
interface PromptRepository : ReactiveCassandraRepository<Prompt, UUID> {
    @Query("SELECT COUNT(*) FROM prompt WHERE userid = :userid AND created > :date ALLOW FILTERING")
    fun getNumberOfPromptsLast24hByUserId(
        @Param("userid") userid: UUID,
        @Param("date") date: Date
    ): Mono<Int>

    @Query("INSERT INTO prompt (id, userid, prompt, isimage, created, modified) VALUES (uuid(), :userid, :prompt, :isimage, toTimestamp(now()), toTimestamp(now()))")
    fun insertPrompt(
        @Param("userid") userid: UUID,
        @Param("prompt") prompt: String,
        @Param("isimage") isImage: Boolean
    ): Mono<Void>

}
