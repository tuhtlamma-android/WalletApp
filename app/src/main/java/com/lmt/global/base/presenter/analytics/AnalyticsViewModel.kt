package com.lmt.global.base.presenter.analytics

import androidx.lifecycle.viewModelScope
import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.Transaction
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.stateIn
import org.threeten.bp.ZoneId

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsViewModel internal constructor(
    walletRepository: WalletRepository,
    sessionManager: SessionManager,
    private val nowMillis: () -> Long,
    private val zoneId: ZoneId
) : IViewModel<AnalyticsAction>() {

    constructor(
        walletRepository: WalletRepository,
        sessionManager: SessionManager
    ) : this(
        walletRepository = walletRepository,
        sessionManager = sessionManager,
        nowMillis = System::currentTimeMillis,
        zoneId = ZoneId.systemDefault()
    )

    private val selectedPeriod = MutableStateFlow(AnalyticsPeriod.LAST_7_DAYS)

    private val source = sessionManager.currentAccountId.flatMapLatest { accountId ->
        if (accountId == null) {
            flowOf(AnalyticsSource(hasActiveAccount = false, transactions = emptyList()))
        } else {
            walletRepository.observeHistory(accountId).map { transactions ->
                AnalyticsSource(hasActiveAccount = true, transactions = transactions)
            }
        }
    }

    val uiState = combine(source, selectedPeriod) { source, period ->
        AnalyticsCalculator.calculate(
            transactions = source.transactions,
            period = period,
            nowMillis = nowMillis(),
            zoneId = zoneId,
            hasActiveAccount = source.hasActiveAccount
        )
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = AnalyticsUiState()
    )

    override fun onState(state: AnalyticsAction) {
        when (state) {
            is AnalyticsAction.SelectPeriod -> selectedPeriod.value = state.period
        }
    }

    private data class AnalyticsSource(
        val hasActiveAccount: Boolean,
        val transactions: List<Transaction>
    )

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

sealed interface AnalyticsAction : IViewModel.IState {
    data class SelectPeriod(val period: AnalyticsPeriod) : AnalyticsAction
}
