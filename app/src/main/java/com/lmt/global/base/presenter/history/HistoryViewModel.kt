package com.lmt.global.base.presenter.history

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.Transaction
import com.lmt.global.base.presenter.wallet.HistoryTransactionFilter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class HistoryViewModel(
    walletRepository: WalletRepository,
    sessionManager: SessionManager
) : IViewModel<HistoryAction>() {

    private val selectedFilters = MutableStateFlow(emptySet<HistoryTransactionFilter>())

    private val transactions = sessionManager.currentAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(emptyList())
            else walletRepository.observeHistory(accountId)
        }

    val uiState = combine(transactions, selectedFilters, ::HistoryUiState)
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = HistoryUiState()
        )

    override fun onState(state: HistoryAction) {
        when (state) {
            is HistoryAction.ApplyFilters -> selectedFilters.value = state.filters
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

data class HistoryUiState(
    val transactions: List<Transaction> = emptyList(),
    val selectedFilters: Set<HistoryTransactionFilter> = emptySet()
)

sealed interface HistoryAction : IViewModel.IState {
    data class ApplyFilters(
        val filters: Set<HistoryTransactionFilter>
    ) : HistoryAction
}
