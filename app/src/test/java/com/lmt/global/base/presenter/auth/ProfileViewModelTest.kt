package com.lmt.global.base.presenter.auth

import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.model.Account
import com.lmt.global.base.model.LoginResult
import com.lmt.global.base.model.RegisterResult
import com.lmt.global.base.presenter.profile.ProfileViewModel
import com.lmt.global.base.presenter.wallet.FakeSessionManager
import com.lmt.global.base.presenter.wallet.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ProfileViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun currentAccount_exposesRegisteredNameEmailAndPhone() = runTest {
        val expected = Account(
            id = 42L,
            name = "Nguyen Van A",
            email = "a@example.com",
            phone = "+84901234567"
        )
        val viewModel = ProfileViewModel(
            sessionManager = FakeSessionManager(expected.id),
            authRepository = FakeAuthRepository(expected)
        )
        viewModel.account.launchIn(backgroundScope)

        advanceUntilIdle()

        assertEquals(expected, viewModel.account.value)
    }

    private class FakeAuthRepository(private val account: Account) : AuthRepository {
        override suspend fun login(identifier: String, password: String) = LoginResult.InvalidInput
        override suspend fun register(name: String, identifier: String, password: String) =
            RegisterResult.InvalidInput
        override suspend fun getAccount(id: Long) = account.takeIf { it.id == id }
        override suspend fun accountExists(identifier: String) = false
    }
}
