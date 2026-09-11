package com.lmt.global.base.presenter.auth

import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.LoginResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class LoginViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : IViewModel<LoginAction>() {
    private val mutableUiState = MutableStateFlow(LoginUiState())
    val uiState = mutableUiState.asStateFlow()

    private val eventChannel = Channel<LoginEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    override fun onState(state: LoginAction) {
        when (state) {
            is LoginAction.Submit -> login(state.identifier, state.password)
        }
    }

    private fun login(identifier: String, password: String) = launchWithMain {
        if (mutableUiState.value.loading) return@launchWithMain
        mutableUiState.update { it.copy(loading = true) }
        try {
            when (val result = authRepository.login(identifier, password)) {
                is LoginResult.Success -> {
                    sessionManager.saveAccountId(result.account.id)
                    eventChannel.send(LoginEvent.NavigateHome)
                }
                LoginResult.AccountNotFound -> eventChannel.send(LoginEvent.AccountNotFound)
                LoginResult.WrongPassword -> eventChannel.send(LoginEvent.WrongPassword)
                LoginResult.InvalidInput -> eventChannel.send(LoginEvent.InvalidInput)
            }
        } finally {
            mutableUiState.update { it.copy(loading = false) }
        }
    }
}

data class LoginUiState(val loading: Boolean = false)

sealed interface LoginAction : IViewModel.IState {
    data class Submit(val identifier: String, val password: String) : LoginAction
}

sealed interface LoginEvent {
    data object NavigateHome : LoginEvent
    data object AccountNotFound : LoginEvent
    data object WrongPassword : LoginEvent
    data object InvalidInput : LoginEvent
}
