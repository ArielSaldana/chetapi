package io.unreal.chet.chetapi.services

import io.unreal.chet.chetapi.error.UserExceededCreditError
import io.unreal.chet.chetapi.objects.QueryCostId
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

/*
 * this service exists to create entries into the transaction table,
 * and maintain synchronization with user_balance table
 */
@Service
class CreditService(
    private val queryCostService: QueryCostService,
    private val userBalanceService: UserBalanceService,
    private val creditTransactionService: CreditTransactionService
) {
    fun queryTransaction(
        telegramId: Long,
        queryCostId: QueryCostId,
        transactionType: String,
        transactionDescription: String
    ): Mono<UUID> {
        return queryCostService.getQueryCost(queryCostId.id)
            .flatMap { cost ->
                userBalanceService.getUserCreditBalance(telegramId)
                    .flatMap { userCredit ->
                        if (userCredit + cost < 0) {
                            Mono.error(UserExceededCreditError())
                        } else {
                            // Create an entry in credit_transactions
                            creditTransactionService.createUserCreditTransactionItem(
                                telegramId,
                                cost,
                                transactionType,
                                transactionDescription
                            ).flatMap { transaction ->
                                // Update the user_balance relative to their current value
                                userBalanceService.updateUserBalance(transaction.uid, cost)
                                    .thenReturn(transaction.id)
                            }
                        }
                    }
            }
            .onErrorResume { e ->
                // Handle errors appropriately
                Mono.error(e)
            }
    }
}
