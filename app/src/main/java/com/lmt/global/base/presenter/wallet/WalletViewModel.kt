package com.lmt.global.base.presenter.wallet

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.lmt.global.base.data.WalletActionResult
import com.lmt.global.base.data.WalletRepository
import com.lmt.global.base.data.entity.RecipientEntity
import com.lmt.global.base.data.entity.TransactionEntity
import com.lmt.global.base.data.entity.CardEntity
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn

class WalletViewModel(private val repository: WalletRepository) : ViewModel() {
    val balance: StateFlow<Long?> = repository.balance.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    val recentRecipients: StateFlow<List<RecipientEntity>> = repository.recentRecipients.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val latestTransactions: StateFlow<List<TransactionEntity>> =
        repository.latestTransactions.stateIn(
            viewModelScope,
            SharingStarted.WhileSubscribed(5_000),
            emptyList()
        )

    val history: StateFlow<List<TransactionEntity>> = repository.history.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    val cards: StateFlow<List<CardEntity>> = repository.cards.stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        emptyList()
    )

    fun card(cardId: String): StateFlow<CardEntity?> = repository.card(cardId).stateIn(
        viewModelScope,
        SharingStarted.WhileSubscribed(5_000),
        null
    )

    suspend fun addBalance(amountMinor: Long) = repository.addBalance(amountMinor)

    suspend fun addCard(
        cardId: String,
        cardName: String,
        cardNumber: String,
        balanceMinor: Long
    ) = repository.addCard(cardId, cardName, cardNumber, balanceMinor)

    suspend fun transfer(name: String, avatarKey: String, amountMinor: Long): WalletActionResult =
        repository.transfer(name, avatarKey, amountMinor)

    suspend fun payBill(name: String, iconKey: String, amountMinor: Long): WalletActionResult =
        repository.payBill(name, iconKey, amountMinor)
}
