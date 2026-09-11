package com.lmt.global.base.presenter.home

import androidx.lifecycle.viewModelScope
import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.Recipient
import com.lmt.global.base.model.Transaction
import com.lmt.global.base.model.WalletActionResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModel(
    private val walletRepository: WalletRepository,
    private val sessionManager: SessionManager
) : IViewModel<HomeAction>() {

    val uiState = sessionManager.currentAccountId.flatMapLatest { accountId ->
        if (accountId == null) flowOf(HomeUiState()) else combine(
            walletRepository.observeBalance(accountId),
            walletRepository.observeRecentRecipients(accountId),
            walletRepository.observeLatestTransactions(accountId)
        ) { balance, recipients, transactions ->
            HomeUiState(
                balance = balance,
                recentRecipients = recipients,
                latestTransactions = transactions
            )
        }
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = HomeUiState()
    )

    private val actionResultChannel = Channel<WalletActionResult>(Channel.BUFFERED)
    val actionResults = actionResultChannel.receiveAsFlow()

    override fun onState(state: HomeAction) {
        when (state) {
            is HomeAction.AddBalance -> {
                launchWithMain {
                    val accountId = sessionManager.currentAccountId.first()
                    val result = if (accountId == null) WalletActionResult.NoActiveAccount
                    else walletRepository.addBalance(accountId, state.amountMinor)
                    actionResultChannel.send(result)
                }
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

data class HomeUiState(
    val balance: Long = 0L,
    val recentRecipients: List<Recipient> = emptyList(),
    val latestTransactions: List<Transaction> = emptyList()
)

sealed interface HomeAction : IViewModel.IState {
    data class AddBalance(val amountMinor: Long) : HomeAction
}
