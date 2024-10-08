package io.unreal.chet.chetapi.services

import io.unreal.chet.chetapi.error.UserExceededCreditError
import io.unreal.chet.chetapi.error.UserNotFoundError
import io.unreal.chet.chetapi.objects.QueryCostId
import kotlinx.coroutines.reactor.awaitSingleOrNull
import org.springframework.stereotype.Service
import reactor.core.publisher.Mono
import java.util.*

/*
 * This service exists to create entries into the transaction table,
 * and maintain synchronization with user_balance table.
 */
@Service
class CreditService(
    private val queryCostService: QueryCostService,
    private val userBalanceService: UserBalanceService,
    private val creditTransactionService: CreditTransactionService,
    private val userService: UserService
) {
    /**
     * Process a query transaction.
     * This involves checking the cost of the query, checking the user's credit balance,
     * creating a transaction entry, and updating the user's balance.
     * @param telegramId The Telegram ID of the user.
     * @param queryCostId The ID of the query cost.
     * @param transactionType The type of the transaction.
     * @param transactionDescription The description of the transaction.
     * @return The UUID of the transaction.
     */
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
                if (e is UserExceededCreditError) {
                    Mono.error(e) // Rethrow UserExceededCreditError
                } else {
                    Mono.error(RuntimeException("Error occurred while processing query transaction: ${e.message}", e))
                }
            }
    }

    fun checkIfUserHasEnoughCredit(telegramId: Long, queryCostId: QueryCostId): Mono<Void> {
        return queryCostService.getQueryCost(queryCostId.id)
            .flatMap { cost ->
                userBalanceService.getUserCreditBalance(telegramId)
                    .flatMap { userCredit ->
                        if (userCredit + cost >= 0) {
                            Mono.empty<Void>()
                        } else {
                            Mono.error(UserExceededCreditError())
                        }
                    }
            }
    }

    suspend fun transferCredits(
        senderTelegramId: Long,
        recipientTelegramId: Long,
        amount: Int
    ): Mono<Boolean> {
        val senderUid = userService.getUserUidWithTelegramId(senderTelegramId).awaitSingleOrNull()
            ?: return Mono.error(UserNotFoundError())
        val recipientUid = userService.getUserUidWithTelegramId(recipientTelegramId).awaitSingleOrNull()
            ?: return Mono.error(UserNotFoundError("Recipient ID not found"))

        return userBalanceService.getUserCreditBalance(senderTelegramId)
            .flatMap { senderBalance ->
                if (senderBalance - amount < 0) {
                    Mono.error(UserExceededCreditError())
                } else {
                    // Update the user_balance relative to their current value
                    userBalanceService.updateUserBalance(senderUid, -amount)
                        // update the recipient's balance
                        .then(userBalanceService.updateUserBalance(recipientUid, amount))
                        // add entries into both users' transaction tables
                        .then(
                            creditTransactionService.createUserCreditTransactionItem(
                                senderTelegramId,
                                -amount,
                                "TRANSFER",
                                "Transfer to $recipientTelegramId"
                            ).then(
                                creditTransactionService.createUserCreditTransactionItem(
                                    recipientTelegramId,
                                    amount,
                                    "TRANSFER",
                                    "Transfer from $senderTelegramId"
                                )
                            )
                        )
                        .then(Mono.just(true))
                }
            }
    }


    fun processTransactionWithoutChecking(
        telegramId: Long,
        cost: Int,
        transactionType: String,
        transactionDescription: String
    ): Mono<UUID> {
        return creditTransactionService.createUserCreditTransactionItem(
            telegramId,
            cost,
            transactionType,
            transactionDescription
        ).flatMap { transaction ->
            userBalanceService.updateUserBalance(transaction.uid, cost)
                .thenReturn(transaction.id)
        }
            .onErrorResume { e ->
                Mono.error(RuntimeException("Error occurred while processing transaction: ${e.message}", e))
            }
    }
}
