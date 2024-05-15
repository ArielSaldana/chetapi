package io.unreal.chet.chetapi

import io.unreal.chet.chetapi.services.QueryCostService
import org.springframework.boot.ApplicationRunner
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration

@Configuration
class AppConfig(
    private val queryCostService: QueryCostService
) {

    @Bean
    fun initializeDatabase(): ApplicationRunner {
        return ApplicationRunner {
            queryCostService.initializeStaticData()
        }
    }
}
