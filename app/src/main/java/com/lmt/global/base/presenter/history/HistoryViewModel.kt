package com.lmt.global.base.presenter.history

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.Transaction
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    walletRepository: WalletRepository,
    sessionManager: SessionManager
) : IViewModel<HistoryAction>() {

    val uiState = sessionManager.currentAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(HistoryUiState())
            else walletRepository.observeHistory(accountId).map(::HistoryUiState)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HistoryUiState()
        )

    override fun onState(state: HistoryAction) = Unit

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

data class HistoryUiState(val transactions: List<Transaction> = emptyList())

sealed interface HistoryAction : IViewModel.IState
