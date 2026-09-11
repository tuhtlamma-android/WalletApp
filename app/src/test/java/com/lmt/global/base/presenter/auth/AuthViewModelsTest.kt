package com.lmt.global.base.presenter.auth

import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.model.Account
import com.lmt.global.base.model.LoginResult
import com.lmt.global.base.model.RegisterResult
import com.lmt.global.base.presenter.wallet.FakeSessionManager
import com.lmt.global.base.presenter.wallet.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AuthViewModelsTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun successfulLogin_savesOnlyAccountIdAndNavigatesHome() = runTest {
        val account = Account(42L, "Account", "user@example.com", null)
        val session = FakeSessionManager()
        val viewModel = LoginViewModel(FakeAuthRepository(loginResult = LoginResult.Success(account)), session)
        val event = async(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.first() }

        viewModel.onState(LoginAction.Submit("user@example.com", "secret123"))
        advanceUntilIdle()

        assertEquals(LoginEvent.NavigateHome, event.await())
        assertEquals(42L, session.currentAccountId.first())
    }

    @Test
    fun accountNotFound_emitsRecommendationEventWithoutSavingSession() = runTest {
        val session = FakeSessionManager()
        val viewModel = LoginViewModel(FakeAuthRepository(loginResult = LoginResult.AccountNotFound), session)
        val event = async(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.first() }

        viewModel.onState(LoginAction.Submit("missing@example.com", "secret123"))
        advanceUntilIdle()

        assertEquals(LoginEvent.AccountNotFound, event.await())
        assertEquals(null, session.currentAccountId.first())
    }

    @Test
    fun successfulRegistration_savesSessionAndNavigatesOtp() = runTest {
        val account = Account(77L, "Account", null, "+962791234567")
        val session = FakeSessionManager()
        val viewModel = RegisterViewModel(
            FakeAuthRepository(registerResult = RegisterResult.Success(account)),
            session
        )
        val event = async(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.first() }

        viewModel.onState(RegisterAction.Submit("Account", "+962791234567", "secret123"))
        advanceUntilIdle()

        assertEquals(RegisterEvent.NavigateOtp, event.await())
        assertEquals(77L, session.currentAccountId.first())
    }

    private class FakeAuthRepository(
        private val loginResult: LoginResult = LoginResult.InvalidInput,
        private val registerResult: RegisterResult = RegisterResult.InvalidInput
    ) : AuthRepository {
        override suspend fun login(identifier: String, password: String) = loginResult
        override suspend fun register(name: String, identifier: String, password: String) = registerResult
        override suspend fun getAccount(id: Long): Account? = null
        override suspend fun accountExists(identifier: String) = false
    }
}
