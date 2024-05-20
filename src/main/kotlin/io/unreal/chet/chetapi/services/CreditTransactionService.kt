package io.unreal.chet.chetapi.services

import io.unreal.chet.chetapi.error.TransactionEntryFailureError
import io.unreal.chet.chetapi.repository.mongo.CreditTransactions
import io.unreal.chet.chetapi.repository.mongo.CreditTransactionsRepository
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono

@Service
class CreditTransactionService(
    private val userService: UserService,
    private val creditTransactionsRepository: CreditTransactionsRepository
) {

    // Create a new user transaction entry in the database
    fun createUserTransactionEntry(transaction: CreditTransactions): Mono<CreditTransactions> {
        return creditTransactionsRepository.insert(transaction)
            .switchIfEmpty(Mono.error(TransactionEntryFailureError("Failed to save transaction entry")))
            .onErrorResume { ex ->
                Mono.error(TransactionEntryFailureError("An error occurred while saving the transaction: ${ex.message}"))
            }
    }

    // Create a new user credit transaction item in the database
    fun createUserCreditTransactionItem(
        telegramId: Long,
        amount: Int,
        transactionType: String,
        transactionDescription: String
    ): Mono<CreditTransactions> {
        return userService.getUserUidWithTelegramId(telegramId)
            .flatMap { userUid ->
                val creditTransaction = CreditTransactions(
                    uid = userUid,
                    amount = amount,
                    transactionType = transactionType,
                    description = transactionDescription
                )
                createUserTransactionEntry(creditTransaction)
            }
            .onErrorResume { ex ->
                Mono.error(RuntimeException("Error occurred while creating user credit transaction item: ${ex.message}", ex))
            }
    }
}
