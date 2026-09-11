package com.lmt.global.base.presenter.auth

import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.RegisterResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class RegisterViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : IViewModel<RegisterAction>() {
    private val mutableUiState = MutableStateFlow(RegisterUiState())
    val uiState = mutableUiState.asStateFlow()

    private val eventChannel = Channel<RegisterEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    override fun onState(state: RegisterAction) {
        when (state) {
            is RegisterAction.Submit -> register(state)
        }
    }

    private fun register(action: RegisterAction.Submit) = launchWithMain {
        if (mutableUiState.value.loading) return@launchWithMain
        mutableUiState.update { it.copy(loading = true) }
        try {
            when (val result = authRepository.register(action.name, action.identifier, action.password)) {
                is RegisterResult.Success -> {
                    sessionManager.saveAccountId(result.account.id)
                    eventChannel.send(RegisterEvent.NavigateOtp)
                }
                RegisterResult.AlreadyExists -> eventChannel.send(RegisterEvent.AlreadyExists)
                RegisterResult.InvalidInput -> eventChannel.send(RegisterEvent.InvalidInput)
            }
        } finally {
            mutableUiState.update { it.copy(loading = false) }
        }
    }
}

data class RegisterUiState(val loading: Boolean = false)

sealed interface RegisterAction : IViewModel.IState {
    data class Submit(val name: String, val identifier: String, val password: String) : RegisterAction
}

sealed interface RegisterEvent {
    data object NavigateOtp : RegisterEvent
    data object AlreadyExists : RegisterEvent
    data object InvalidInput : RegisterEvent
}
