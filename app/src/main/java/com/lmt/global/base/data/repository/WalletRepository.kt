package com.lmt.global.base.data.repository

import com.lmt.global.base.model.Card
import com.lmt.global.base.model.Recipient
import com.lmt.global.base.model.Transaction
import com.lmt.global.base.model.WalletActionResult
import kotlinx.coroutines.flow.Flow

interface WalletRepository {
    fun observeBalance(accountId: Long): Flow<Long>
    fun observeCards(accountId: Long): Flow<List<Card>>
    fun observeCard(accountId: Long, cardId: String): Flow<Card?>
    fun observeRecentRecipients(accountId: Long): Flow<List<Recipient>>
    fun observeLatestTransactions(accountId: Long, limit: Int = LATEST_TRANSACTION_LIMIT): Flow<List<Transaction>>
    fun observeHistory(accountId: Long): Flow<List<Transaction>>

    suspend fun addBalance(accountId: Long, amountMinor: Long): WalletActionResult
    suspend fun addCard(accountId: Long, card: Card): WalletActionResult
    suspend fun transfer(
        accountId: Long,
        recipientName: String,
        avatarKey: String,
        amountMinor: Long
    ): WalletActionResult

    suspend fun payBill(
        accountId: Long,
        billerName: String,
        iconKey: String,
        amountMinor: Long
    ): WalletActionResult

    companion object {
        const val LATEST_TRANSACTION_LIMIT = 5
    }
}
