package com.lmt.global.base.presenter.auth

import com.lmt.global.base.presenter.more.MoreAction
import com.lmt.global.base.presenter.more.MoreEvent
import com.lmt.global.base.presenter.more.MoreViewModel
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
class LogoutViewModelTest {
    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun logout_clearsCurrentAccountAndNavigatesToLogin() = runTest {
        val sessionManager = FakeSessionManager(accountId = 42L)
        val viewModel = MoreViewModel(sessionManager)
        val event = async(UnconfinedTestDispatcher(testScheduler)) {
            viewModel.events.first()
        }

        viewModel.onState(MoreAction.Logout)
        advanceUntilIdle()

        assertEquals(MoreEvent.NavigateLogin, event.await())
        assertNull(sessionManager.currentAccountId.first())
    }
}
