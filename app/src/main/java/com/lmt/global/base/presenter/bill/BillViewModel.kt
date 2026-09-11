package com.lmt.global.base.presenter.bill

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.WalletActionResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class BillViewModel(
    private val walletRepository: WalletRepository,
    private val sessionManager: SessionManager
) : IViewModel<BillAction>() {

    val balance = sessionManager.currentAccountId.flatMapLatest { accountId ->
        if (accountId == null) flowOf(0L) else walletRepository.observeBalance(accountId)
    }.stateIn(
        scope = viewModelScope,
        started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
        initialValue = 0L
    )

    private val resultChannel = Channel<BillPaymentResult>(Channel.BUFFERED)
    val results = resultChannel.receiveAsFlow()

    override fun onState(state: BillAction) {
        when (state) {
            is BillAction.Submit -> launchWithMain {
                val accountId = sessionManager.currentAccountId.first()
                val result = if (accountId == null) WalletActionResult.NoActiveAccount
                else walletRepository.payBill(
                        accountId = accountId,
                        billerName = state.billerName,
                        iconKey = state.iconKey,
                        amountMinor = state.amountMinor
                    )
                resultChannel.send(
                    BillPaymentResult(
                        billerName = state.billerName,
                        iconKey = state.iconKey,
                        amountMinor = state.amountMinor,
                        result = result
                    )
                )
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

data class BillPaymentResult(
    val billerName: String,
    val iconKey: String,
    val amountMinor: Long,
    val result: WalletActionResult
)

sealed interface BillAction : IViewModel.IState {
    data class Submit(
        val billerName: String,
        val iconKey: String,
        val amountMinor: Long
    ) : BillAction
}
