package com.lmt.global.base.presenter.wallet

import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.Card
import com.lmt.global.base.model.Recipient
import com.lmt.global.base.model.Transaction
import com.lmt.global.base.model.WalletActionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow

class FakeSessionManager(accountId: Long? = null) : SessionManager {
    private val accountIdFlow = MutableStateFlow(accountId)
    override val currentAccountId: Flow<Long?> = accountIdFlow

    override suspend fun saveAccountId(id: Long) { accountIdFlow.value = id }
    override suspend fun clearSession() { accountIdFlow.value = null }
}

class FakeWalletRepository : WalletRepository {
    private val balances = mutableMapOf<Long, MutableStateFlow<Long>>()
    private val cards = mutableMapOf<Long, MutableStateFlow<List<Card>>>()
    private val recipients = mutableMapOf<Long, MutableStateFlow<List<Recipient>>>()
    private val transactions = mutableMapOf<Long, MutableStateFlow<List<Transaction>>>()

    var addBalanceResult: WalletActionResult = WalletActionResult.Success()
    var addCardResult: WalletActionResult = WalletActionResult.Success()
    var transferResult: WalletActionResult = WalletActionResult.Success(1L)
    var payBillResult: WalletActionResult = WalletActionResult.Success(1L)
    var lastAddedCard: Card? = null
    var lastAddBalanceAccountId: Long? = null
    var lastAddCardAccountId: Long? = null
    var lastTransferAccountId: Long? = null
    var lastPayBillAccountId: Long? = null

    fun balance(accountId: Long) = balances.getOrPut(accountId) { MutableStateFlow(0L) }
    fun cards(accountId: Long) = cards.getOrPut(accountId) { MutableStateFlow(emptyList()) }
    fun recipients(accountId: Long) = recipients.getOrPut(accountId) { MutableStateFlow(emptyList()) }
    fun transactions(accountId: Long) = transactions.getOrPut(accountId) { MutableStateFlow(emptyList()) }

    override fun observeBalance(accountId: Long): Flow<Long> = balance(accountId)
    override fun observeCards(accountId: Long): Flow<List<Card>> = cards(accountId)
    override fun observeCard(accountId: Long, cardId: String): Flow<Card?> = MutableStateFlow(
        cards(accountId).value.firstOrNull { it.id == cardId }
    )
    override fun observeRecentRecipients(accountId: Long): Flow<List<Recipient>> = recipients(accountId)
    override fun observeLatestTransactions(accountId: Long, limit: Int): Flow<List<Transaction>> =
        transactions(accountId)
    override fun observeHistory(accountId: Long): Flow<List<Transaction>> = transactions(accountId)

    override suspend fun addBalance(accountId: Long, amountMinor: Long): WalletActionResult {
        lastAddBalanceAccountId = accountId
        return addBalanceResult
    }

    override suspend fun addCard(accountId: Long, card: Card): WalletActionResult {
        lastAddCardAccountId = accountId
        lastAddedCard = card
        return addCardResult
    }

    override suspend fun transfer(
        accountId: Long,
        recipientName: String,
        avatarKey: String,
        amountMinor: Long
    ): WalletActionResult {
        lastTransferAccountId = accountId
        return transferResult
    }

    override suspend fun payBill(
        accountId: Long,
        billerName: String,
        iconKey: String,
        amountMinor: Long
    ): WalletActionResult {
        lastPayBillAccountId = accountId
        return payBillResult
    }
}
