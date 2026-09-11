package com.lmt.global.base.presenter.splash

import com.lmt.global.base.common.IViewModel
import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.data.session.SessionManager
import kotlinx.coroutines.channels.Channel
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.receiveAsFlow

class SplashViewModel(
    private val authRepository: AuthRepository,
    private val sessionManager: SessionManager
) : IViewModel<SplashAction>() {
    private val destinationChannel = Channel<SplashDestination>(Channel.CONFLATED)
    val destinations = destinationChannel.receiveAsFlow()

    init {
        launchWithMain {
            val accountId = sessionManager.currentAccountId.first()
            val account = accountId?.let { authRepository.getAccount(it) }
            if (account != null) {
                destinationChannel.send(SplashDestination.Home)
            } else {
                if (accountId != null) sessionManager.clearSession()
                destinationChannel.send(SplashDestination.Login)
            }
        }
    }

    override fun onState(state: SplashAction) = Unit
}

sealed interface SplashAction : IViewModel.IState

enum class SplashDestination { Home, Login }
