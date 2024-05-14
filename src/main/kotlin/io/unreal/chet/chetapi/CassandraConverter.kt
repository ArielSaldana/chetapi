package io.unreal.chet.chetapi

import org.springframework.data.cassandra.core.convert.MappingCassandraConverter
import org.springframework.data.cassandra.core.mapping.CassandraMappingContext
import org.springframework.data.cassandra.core.mapping.SnakeCaseNamingStrategy
import org.springframework.data.mapping.model.SimpleTypeHolder

class CassandraConverter(mappingContext: CassandraMappingContext) : MappingCassandraConverter(mappingContext) {
    override fun getMappingContext(): CassandraMappingContext {
        val context = super.getMappingContext()
        context.setSimpleTypeHolder(SimpleTypeHolder.DEFAULT)
        context.setNamingStrategy(SnakeCaseNamingStrategy())
        return context
    }
}
