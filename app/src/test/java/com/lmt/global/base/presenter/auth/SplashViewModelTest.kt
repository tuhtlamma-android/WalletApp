package com.lmt.global.base.presenter.auth

import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.model.Account
import com.lmt.global.base.model.LoginResult
import com.lmt.global.base.model.RegisterResult
import com.lmt.global.base.presenter.splash.SplashDestination
import com.lmt.global.base.presenter.splash.SplashViewModel
import com.lmt.global.base.presenter.wallet.FakeSessionManager
import com.lmt.global.base.presenter.wallet.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SplashViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun validStoredAccount_restoresHome() = runTest {
        val account = Account(42L, "Account", "user@example.com", null)
        val session = FakeSessionManager(account.id)
        val viewModel = SplashViewModel(FakeAuthRepository(account), session)
        advanceUntilIdle()

        assertEquals(SplashDestination.Home, viewModel.destinations.first())
        assertEquals(account.id, session.currentAccountId.first())
    }

    @Test
    fun staleStoredAccount_clearsSessionAndOpensLogin() = runTest {
        val session = FakeSessionManager(404L)
        val viewModel = SplashViewModel(FakeAuthRepository(null), session)
        advanceUntilIdle()

        assertEquals(SplashDestination.Login, viewModel.destinations.first())
        assertNull(session.currentAccountId.first())
    }

    private class FakeAuthRepository(private val account: Account?) : AuthRepository {
        override suspend fun login(identifier: String, password: String) = LoginResult.InvalidInput
        override suspend fun register(name: String, identifier: String, password: String) =
            RegisterResult.InvalidInput
        override suspend fun getAccount(id: Long): Account? = account?.takeIf { it.id == id }
        override suspend fun accountExists(identifier: String) = false
    }
}
