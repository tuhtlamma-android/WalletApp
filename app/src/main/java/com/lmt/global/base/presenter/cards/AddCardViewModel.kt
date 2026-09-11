package com.lmt.global.base.presenter.cards

import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.Card
import com.lmt.global.base.model.WalletActionResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.first

class AddCardViewModel(
    private val walletRepository: WalletRepository,
    private val sessionManager: SessionManager
) : IViewModel<AddCardAction>() {

    private val resultChannel = Channel<WalletActionResult>(Channel.BUFFERED)
    val results = resultChannel.receiveAsFlow()

    override fun onState(state: AddCardAction) {
        when (state) {
            is AddCardAction.Submit -> launchWithMain {
                val accountId = sessionManager.currentAccountId.first()
                val result = if (accountId == null) WalletActionResult.NoActiveAccount
                else walletRepository.addCard(accountId, state.card)
                resultChannel.send(result)
            }
        }
    }
}

sealed interface AddCardAction : IViewModel.IState {
    data class Submit(val card: Card) : AddCardAction
}
