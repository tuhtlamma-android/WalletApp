package com.lmt.global.base.presenter.home

import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.model.Card
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest

class HomeViewModel(
    private val walletRepository: WalletRepository
): IViewModel<HomeState>() {

    private val _cardList = MutableStateFlow<List<Card>>(emptyList())
    internal val cardList = _cardList.asStateFlow()

    init {
        launchWithDefault {
            walletRepository.getAll().collectLatest {
                _cardList.value = it
            }
        }
    }

    override fun onState(state: HomeState) {
        when (state) {
            is HomeState.AddUser -> {
                launchWithIO {
                    walletRepository.addCard()
                }
            }
        }
    }
}

sealed interface HomeState : IViewModel.IState {
    data class AddUser(val name: String) : HomeState
}