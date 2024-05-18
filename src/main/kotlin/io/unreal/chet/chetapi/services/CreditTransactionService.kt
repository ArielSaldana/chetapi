package io.unreal.chet.chetapi.services

import io.unreal.chet.chetapi.error.TransactionEntryFailureError
import io.unreal.chet.chetapi.repository.mongo.CreditTransactions
import io.unreal.chet.chetapi.repository.mongo.CreditTransactionsRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CreditTransactionService(
    val userService: UserService,
    val creditTransactionsRepository: CreditTransactionsRepository
) {
    // Let's say a user makes a call to the img api, we query the query_cost table to see the cost of the query
    // we then query the user_balance table to see if the user has enough credits to make the query
    // if they don't return a UserInsufficientCreditsException
    // if they have enough credits, attempt to process the query and charge the user only on success
    fun createUserTransactionEntry(transaction: CreditTransactions): Mono<CreditTransactions> {
        return creditTransactionsRepository.save(transaction).map { creditTransaction ->
            creditTransaction
        }.switchIfEmpty(Mono.error(TransactionEntryFailureError()))
    }

    fun createUserCreditTransactionItem(
        telegramId: Long,
        amount: Int,
        transactionType: String,
        transactionDescription: String
    ): Mono<CreditTransactions> {
        return userService.getUserUidWithTelegramId(telegramId)
            .flatMap { userUid ->
                createUserTransactionEntry(
                    CreditTransactions(
                        uid = userUid,
                        amount = amount,
                        transactionType = transactionType,
                        description = transactionDescription
                    )
                )
            }
    }
}
