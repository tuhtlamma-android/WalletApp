package com.lmt.global.base.presenter.more

import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.session.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.receiveAsFlow

class MoreViewModel(
    private val sessionManager: SessionManager
) : IViewModel<MoreAction>() {
    private val eventChannel = Channel<MoreEvent>(Channel.BUFFERED)
    val events = eventChannel.receiveAsFlow()

    override fun onState(state: MoreAction) {
        when (state) {
            MoreAction.Logout -> launchWithMain {
                sessionManager.clearSession()
                eventChannel.send(MoreEvent.NavigateLogin)
            }
        }
    }
}

sealed interface MoreAction : IViewModel.IState {
    data object Logout : MoreAction
}

sealed interface MoreEvent {
    data object NavigateLogin : MoreEvent
}
