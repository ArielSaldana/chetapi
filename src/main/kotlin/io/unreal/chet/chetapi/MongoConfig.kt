package io.unreal.chet.chetapi

import com.mongodb.reactivestreams.client.MongoClient
import org.springframework.beans.factory.annotation.Value
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.data.mongodb.ReactiveMongoDatabaseFactory
import org.springframework.data.mongodb.ReactiveMongoTransactionManager
import org.springframework.data.mongodb.core.ReactiveMongoTemplate
import org.springframework.data.mongodb.core.SimpleReactiveMongoDatabaseFactory
import org.springframework.transaction.ReactiveTransactionManager
import org.springframework.transaction.annotation.EnableTransactionManagement
import org.springframework.transaction.reactive.TransactionalOperator

@Configuration
@EnableTransactionManagement
class MongoConfig {

    @Value("\${spring.data.mongodb.database}")
    lateinit var databaseName: String

    @Bean
    fun reactiveMongoDatabaseFactory(mongoClient: MongoClient): ReactiveMongoDatabaseFactory {
        return SimpleReactiveMongoDatabaseFactory(mongoClient, databaseName)
    }

    @Bean
    fun reactiveTransactionManager(dbFactory: ReactiveMongoDatabaseFactory): ReactiveTransactionManager {
        return ReactiveMongoTransactionManager(dbFactory)
    }

    @Bean
    fun transactionalOperator(rtm: ReactiveTransactionManager): TransactionalOperator {
        return TransactionalOperator.create(rtm)
    }
}
