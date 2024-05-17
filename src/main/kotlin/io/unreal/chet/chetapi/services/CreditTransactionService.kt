package io.unreal.chet.chetapi.services

import io.unreal.chet.chetapi.repository.mongo.User
import io.unreal.chet.chetapi.repository.mongo.CreditTransactionsRepository
import org.springframework.stereotype.Service

@Service
class CreditTransactionService(
    queryCostService: QueryCostService,
    creditTransactionsRepository: CreditTransactionsRepository
) {
    // lets say a user makes a call to the img api, we query the query_cost table to see the cost of the query
    // we then query the user_balance table to see if the user has enough credits to make the query
    // if they don't return a UserInsufficientCreditsException
    // if they have enough credits, attempt to process the query and charge the user only on success
    fun chargeUserForquery(user: User) {

    }
}
