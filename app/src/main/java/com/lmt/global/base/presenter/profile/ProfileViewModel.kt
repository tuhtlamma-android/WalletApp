package com.lmt.global.base.presenter.profile

import androidx.lifecycle.viewModelScope
import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.data.session.SessionManager
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.mapLatest
import kotlinx.coroutines.flow.stateIn

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModel(
    sessionManager: SessionManager,
    authRepository: AuthRepository
) : IViewModel<ProfileAction>() {
    val account = sessionManager.currentAccountId
        .mapLatest { accountId -> accountId?.let { authRepository.getAccount(it) } }
        .stateIn(
            scope = viewModelScope,
            started = SharingStarted.WhileSubscribed(STOP_TIMEOUT_MILLIS),
            initialValue = null
        )

    override fun onState(state: ProfileAction) = Unit

    private companion object {
        const val STOP_TIMEOUT_MILLIS = 5_000L
    }
}

sealed interface ProfileAction : IViewModel.IState
