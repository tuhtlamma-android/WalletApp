package com.lmt.global.base.presenter.transfer

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.Recipient
import com.lmt.global.base.model.WalletActionResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class TransferViewModel(
    private val walletRepository: WalletRepository,
    private val sessionManager: SessionManager
) : IViewModel<TransferAction>() {

    val uiState = sessionManager.currentAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(TransferUiState())
            else walletRepository.observeRecentRecipients(accountId).map(::TransferUiState)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = TransferUiState()
        )

    private val resultChannel = Channel<WalletActionResult>(Channel.BUFFERED)
    val results = resultChannel.receiveAsFlow()

    override fun onState(state: TransferAction) {
        when (state) {
            is TransferAction.Submit -> launchWithMain {
                val accountId = sessionManager.currentAccountId.first()
                resultChannel.send(if (accountId == null) WalletActionResult.NoActiveAccount else
                    walletRepository.transfer(
                        accountId = accountId,
                        recipientName = state.recipientName,
                        avatarKey = state.avatarKey,
                        amountMinor = state.amountMinor
                    )
                )
            }
        }
    }

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

data class TransferUiState(val recipients: List<Recipient> = emptyList())

sealed interface TransferAction : IViewModel.IState {
    data class Submit(
        val recipientName: String,
        val avatarKey: String,
        val amountMinor: Long
    ) : TransferAction
}
