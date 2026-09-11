package com.lmt.global.base.presenter.cards

import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.ExperimentalCoroutinesApi
import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.Card
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.flow.flatMapLatest
import kotlinx.coroutines.flow.flowOf
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class CardsViewModel(
    private val walletRepository: WalletRepository,
    private val sessionManager: SessionManager
) : IViewModel<CardsAction>() {

    val uiState = sessionManager.currentAccountId
        .flatMapLatest { accountId ->
            if (accountId == null) flowOf(CardsUiState())
            else walletRepository.observeCards(accountId).map(::CardsUiState)
        }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = CardsUiState()
        )

    fun observeCard(cardId: String) = sessionManager.currentAccountId.flatMapLatest { accountId ->
        if (accountId == null) flowOf(null) else walletRepository.observeCard(accountId, cardId)
    }

    override fun onState(state: CardsAction) = Unit

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

data class CardsUiState(val cards: List<Card> = emptyList())

sealed interface CardsAction : IViewModel.IState
