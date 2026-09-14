package com.lmt.global.base.presenter.profile

import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.model.ChangePasswordResult
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow
import kotlinx.coroutines.flow.update

class ChangePasswordViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : IViewModel<ChangePasswordAction>() {
    private val mutableUiState = MutableStateFlow(ChangePasswordUiState())
    val uiState = mutableUiState.asStateFlow()

    private val eventChannel = Channel<ChangePasswordEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    override fun onState(state: ChangePasswordAction) {
        when (state) {
            is ChangePasswordAction.Submit -> changePassword(state)
        }
    }

    private fun changePassword(action: ChangePasswordAction.Submit) = launchWithMain(
        error = { eventChannel.trySend(ChangePasswordEvent.Failed) }
    ) {
        if (mutableUiState.value.loading) return@launchWithMain
        mutableUiState.update { it.copy(loading = true) }
        try {
            val accountId = sessionManager.currentAccountId.first()
            if (accountId == null) {
                eventChannel.send(ChangePasswordEvent.SessionExpired)
                return@launchWithMain
            }
            when (authRepository.changePassword(accountId, action.currentPassword, action.newPassword)) {
                ChangePasswordResult.Success -> eventChannel.send(ChangePasswordEvent.Success)
                ChangePasswordResult.WrongCurrentPassword -> {
                    eventChannel.send(ChangePasswordEvent.WrongCurrentPassword)
                }
                ChangePasswordResult.SamePassword -> eventChannel.send(ChangePasswordEvent.SamePassword)
                ChangePasswordResult.AccountNotFound -> eventChannel.send(ChangePasswordEvent.SessionExpired)
                ChangePasswordResult.InvalidInput -> eventChannel.send(ChangePasswordEvent.InvalidInput)
            }
        } finally {
            mutableUiState.update { it.copy(loading = false) }
        }
    }
}

data class ChangePasswordUiState(val loading: Boolean = false)

sealed interface ChangePasswordAction : IViewModel.IState {
    data class Submit(
        val currentPassword: String,
        val newPassword: String
    ) : ChangePasswordAction
}

sealed interface ChangePasswordEvent {
    data object Success : ChangePasswordEvent
    data object WrongCurrentPassword : ChangePasswordEvent
    data object SamePassword : ChangePasswordEvent
    data object InvalidInput : ChangePasswordEvent
    data object SessionExpired : ChangePasswordEvent
    data object Failed : ChangePasswordEvent
}
