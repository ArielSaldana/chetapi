//package io.unreal.chet.chetapi
//
//import org.springframework.beans.factory.annotation.Value
//import org.springframework.boot.autoconfigure.cassandra.CassandraProperties
//import org.springframework.boot.autoconfigure.domain.EntityScan
//import org.springframework.context.annotation.Bean
//import org.springframework.context.annotation.Configuration
//import org.springframework.context.annotation.PropertySource
//import org.springframework.core.io.ClassPathResource
//import org.springframework.data.cassandra.config.AbstractCassandraConfiguration
//import org.springframework.data.cassandra.core.cql.keyspace.CreateKeyspaceSpecification
//import org.springframework.data.cassandra.core.cql.keyspace.DataCenterReplication
//import org.springframework.data.cassandra.core.cql.session.init.KeyspacePopulator
//import org.springframework.data.cassandra.core.cql.session.init.ResourceKeyspacePopulator
//import org.springframework.data.cassandra.core.mapping.CassandraMappingContext
//import org.springframework.data.cassandra.core.mapping.NamingStrategy
//import org.springframework.data.cassandra.core.mapping.SnakeCaseNamingStrategy
//import org.springframework.data.cassandra.repository.config.EnableCassandraRepositories
//
//@Configuration
//@EnableCassandraRepositories
//@EntityScan
//@PropertySource("classpath:application.properties")
//class SpringCassandraConfiguration : AbstractCassandraConfiguration() {
//
//    @Value("\${spring.cassandra.local-datacenter}")
//    private lateinit var datacenter: String
//
//    @Value("\${spring.cassandra.keyspace-name}")
//    private lateinit var keySpaceName: String
//
//    @Value("\${spring.cassandra.contact-points}")
//    private lateinit var contactPoints: String
//
//    @Value("\${spring.cassandra.port}")
//    private var port: Int = 9042
//
//    override fun getKeyspaceCreations(): MutableList<CreateKeyspaceSpecification> {
//        return mutableListOf(
//            CreateKeyspaceSpecification.createKeyspace(keyspaceName)
//                .ifNotExists()
//                .withNetworkReplication(DataCenterReplication.of(datacenter, 1))
//        )
//    }
//
//    override fun getKeyspaceName(): String {
//        return keySpaceName
//    }
//
//    override fun getContactPoints(): String {
//        return contactPoints
//    }
//
//    override fun getPort(): Int {
//        return port
//    }
//
////    override fun keyspacePopulator(): KeyspacePopulator {
////        return ResourceKeyspacePopulator(ClassPathResource("dbs.cql"))
////    }
//
////    @Bean
////    override fun cassandraConverter(): CassandraConverter {
////        super.cassandraConverter()
////        return CassandraConverter(CassandraMappingContext())
////    }
//
//    @Bean
//    fun schemaAction(cassandraProperties: CassandraProperties): String? {
//        return cassandraProperties.schemaAction
//    }
//
//    @Bean
//    @Override
//    fun cassandraMappingContext(): CassandraMappingContext {
//        val mappingContext = CassandraMappingContext()
//        mappingContext.setNamingStrategy(SnakeCaseNamingStrategy())
//        return mappingContext
//    }
//
//    override fun cassandraMapping(): CassandraMappingContext {
//        val context = super.cassandraMapping()
//        context.setNamingStrategy(NamingStrategy.SNAKE_CASE)
//        return context
//    }
//}
