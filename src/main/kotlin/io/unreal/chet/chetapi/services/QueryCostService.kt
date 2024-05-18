package io.unreal.chet.chetapi.services

import io.unreal.chet.chetapi.error.QueryCostNotFoundError
import io.unreal.chet.chetapi.repository.mongo.QueryCost
import io.unreal.chet.chetapi.repository.mongo.QueryCostRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class QueryCostService(private val queryCostRepository: QueryCostRepository) {
    fun addQueryCostIfNotExist(queryCost: QueryCost): Mono<QueryCost> {
        return queryCostRepository.findById(queryCost.queryId)
            .switchIfEmpty(queryCostRepository.save(queryCost))
    }

    fun getQueryCost(queryId: Int): Mono<Int> {
        return queryCostRepository.findById(queryId).map { queryCost -> queryCost.creditCost }
            .switchIfEmpty(Mono.error(QueryCostNotFoundError()))
    }

    // assuming 1000 = $10 worth of credits
    // openai img cost 4 cents per image
    // if we wanted to make a profit we could charge 10 cents per image
    // if they top up $10 they get 1000 credits
    // if they used it only for image generations at 10 cents per image they would get 100 images
    fun initializeStaticData() {
        val staticMembers = listOf(
            QueryCost(
                queryId = 0,
                queryName = "chat-3.5",
                queryDescription = "OpenAI chat API version 3.5",
                creditCost = -1
            ),
            QueryCost(
                queryId = 1,
                queryName = "chat-4.0",
                queryDescription = "OpenAI chat API version 4.0",
                creditCost = -5
            ),
            QueryCost(
                queryId = 2,
                queryName = "dall-e-3",
                queryDescription = "OpenAI DALL-E version 3",
                creditCost = -10
            ),
            QueryCost(
                queryId = 3,
                queryName = "promotional-100-signup-credit",
                queryDescription = "Offering users a one time sign up credit",
                creditCost = 100
            ),
        )

        staticMembers.forEach { creditCost ->
            addQueryCostIfNotExist(creditCost).subscribe()
        }
    }
}
