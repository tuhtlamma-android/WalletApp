package com.lmt.global.base.presenter.profile

import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.model.Account
import com.lmt.global.base.model.ChangePasswordResult
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
import org.junit.Assert.assertNull
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class ChangePasswordViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun successfulChange_usesCurrentSessionAccountAndEmitsSuccess() = runTest {
        val repository = FakeAuthRepository(ChangePasswordResult.Success)
        val viewModel = ChangePasswordViewModel(repository, FakeSessionManager(ACCOUNT_ID))
        val event = async(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.first() }

        viewModel.onState(ChangePasswordAction.Submit(CURRENT_PASSWORD, NEW_PASSWORD))
        advanceUntilIdle()

        assertEquals(ChangePasswordEvent.Success, event.await())
        assertEquals(ACCOUNT_ID, repository.changedAccountId)
        assertEquals(CURRENT_PASSWORD, repository.currentPassword)
        assertEquals(NEW_PASSWORD, repository.newPassword)
        assertEquals(false, viewModel.uiState.value.loading)
    }

    @Test
    fun wrongCurrentPassword_emitsFieldSpecificEvent() = runTest {
        val repository = FakeAuthRepository(ChangePasswordResult.WrongCurrentPassword)
        val viewModel = ChangePasswordViewModel(repository, FakeSessionManager(ACCOUNT_ID))
        val event = async(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.first() }

        viewModel.onState(ChangePasswordAction.Submit("wrong-password", NEW_PASSWORD))
        advanceUntilIdle()

        assertEquals(ChangePasswordEvent.WrongCurrentPassword, event.await())
    }

    @Test
    fun missingSession_emitsSessionExpiredWithoutUpdatingRepository() = runTest {
        val repository = FakeAuthRepository(ChangePasswordResult.Success)
        val viewModel = ChangePasswordViewModel(repository, FakeSessionManager())
        val event = async(UnconfinedTestDispatcher(testScheduler)) { viewModel.events.first() }

        viewModel.onState(ChangePasswordAction.Submit(CURRENT_PASSWORD, NEW_PASSWORD))
        advanceUntilIdle()

        assertEquals(ChangePasswordEvent.SessionExpired, event.await())
        assertNull(repository.changedAccountId)
    }

    private class FakeAuthRepository(
        private val changePasswordResult: ChangePasswordResult
    ) : AuthRepository {
        var changedAccountId: Long? = null
        var currentPassword: String? = null
        var newPassword: String? = null

        override suspend fun changePassword(
            accountId: Long,
            currentPassword: String,
            newPassword: String
        ): ChangePasswordResult {
            changedAccountId = accountId
            this.currentPassword = currentPassword
            this.newPassword = newPassword
            return changePasswordResult
        }

        override suspend fun login(identifier: String, password: String) = LoginResult.InvalidInput
        override suspend fun register(name: String, identifier: String, password: String) =
            RegisterResult.InvalidInput
        override suspend fun getAccount(id: Long): Account? = null
        override suspend fun accountExists(identifier: String) = false
    }

    private companion object {
        const val ACCOUNT_ID = 42L
        const val CURRENT_PASSWORD = "secret123"
        const val NEW_PASSWORD = "new-secret456"
    }
}
