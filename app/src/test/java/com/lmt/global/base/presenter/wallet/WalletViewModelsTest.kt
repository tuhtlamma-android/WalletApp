package com.lmt.global.base.presenter.wallet

import com.lmt.global.base.model.Card
import com.lmt.global.base.model.Account
import com.lmt.global.base.model.LoginResult
import com.lmt.global.base.model.RegisterResult
import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.model.Recipient
import com.lmt.global.base.model.Transaction
import com.lmt.global.base.model.TransactionType
import com.lmt.global.base.model.WalletActionResult
import com.lmt.global.base.presenter.bill.BillAction
import com.lmt.global.base.presenter.bill.BillViewModel
import com.lmt.global.base.presenter.cards.AddCardAction
import com.lmt.global.base.presenter.cards.AddCardViewModel
import com.lmt.global.base.presenter.cards.CardsViewModel
import com.lmt.global.base.presenter.history.HistoryViewModel
import com.lmt.global.base.presenter.home.HomeAction
import com.lmt.global.base.presenter.home.HomeViewModel
import com.lmt.global.base.presenter.transfer.TransferAction
import com.lmt.global.base.presenter.transfer.TransferViewModel
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.UnconfinedTestDispatcher
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class WalletViewModelsTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun homeViewModel_switchAccountChangesAllAccountOwnedState() = runTest {
        val repository = FakeWalletRepository()
        val session = FakeSessionManager(ACCOUNT_A)
        repository.balance(ACCOUNT_A).value = 25_000L
        repository.balance(ACCOUNT_B).value = 50_000L
        repository.recipients(ACCOUNT_A).value = listOf(Recipient(1L, "A recipient", "a", 1L))
        repository.recipients(ACCOUNT_B).value = listOf(Recipient(2L, "B recipient", "b", 2L))
        val viewModel = HomeViewModel(
            repository,
            session,
            FakeAuthRepository(
                mapOf(
                    ACCOUNT_A to Account(ACCOUNT_A, "Account A", "a@example.com", null),
                    ACCOUNT_B to Account(ACCOUNT_B, "Account B", null, "+962791234567")
                )
            )
        )
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        assertEquals(25_000L, viewModel.uiState.value.balance)
        assertEquals("Account A", viewModel.uiState.value.account?.name)
        assertEquals("A recipient", viewModel.uiState.value.recentRecipients.single().name)

        session.saveAccountId(ACCOUNT_B)
        advanceUntilIdle()

        assertEquals(50_000L, viewModel.uiState.value.balance)
        assertEquals("Account B", viewModel.uiState.value.account?.name)
        assertEquals("B recipient", viewModel.uiState.value.recentRecipients.single().name)
    }

    @Test
    fun cardsAndHistoryViewModels_doNotLeakWhenSessionSwitches() = runTest {
        val repository = FakeWalletRepository()
        val session = FakeSessionManager(ACCOUNT_A)
        repository.cards(ACCOUNT_A).value = listOf(Card("A1", "A", "1111111111111111", 0L))
        repository.cards(ACCOUNT_B).value = listOf(Card("B1", "B", "2222222222222222", 0L))
        repository.transactions(ACCOUNT_A).value = listOf(transaction(1L, "A payment"))
        repository.transactions(ACCOUNT_B).value = listOf(transaction(2L, "B payment"))
        val cardsViewModel = CardsViewModel(repository, session)
        val historyViewModel = HistoryViewModel(repository, session)
        cardsViewModel.uiState.launchIn(backgroundScope)
        historyViewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        assertEquals("A1", cardsViewModel.uiState.value.cards.single().id)
        assertEquals("A payment", historyViewModel.uiState.value.transactions.single().title)

        session.saveAccountId(ACCOUNT_B)
        advanceUntilIdle()

        assertEquals("B1", cardsViewModel.uiState.value.cards.single().id)
        assertEquals("B payment", historyViewModel.uiState.value.transactions.single().title)
    }

    @Test
    fun actionViewModels_passCurrentAccountIdToRepository() = runTest {
        val repository = FakeWalletRepository().apply {
            addCardResult = WalletActionResult.DuplicateCard
        }
        val session = FakeSessionManager(ACCOUNT_B)
        val home = HomeViewModel(repository, session, FakeAuthRepository())
        val addCard = AddCardViewModel(repository, session)
        val transfer = TransferViewModel(repository, session)
        val bill = BillViewModel(repository, session)
        val card = Card("CARD001", "Visa", "1234567812345678", 10_000L)
        val cardResult = async(UnconfinedTestDispatcher(testScheduler)) { addCard.results.first() }
        val transferResult = async(UnconfinedTestDispatcher(testScheduler)) { transfer.results.first() }
        val billResult = async(UnconfinedTestDispatcher(testScheduler)) { bill.results.first() }
        val balanceResult = async(UnconfinedTestDispatcher(testScheduler)) { home.actionResults.first() }

        home.onState(HomeAction.AddBalance(1_000L))
        addCard.onState(AddCardAction.Submit(card))
        transfer.onState(TransferAction.Submit("Recipient", "avatar", 100L))
        bill.onState(BillAction.Submit("Electricity", "bill_electricity", 100L))
        advanceUntilIdle()

        assertEquals(WalletActionResult.DuplicateCard, cardResult.await())
        transferResult.await()
        billResult.await()
        balanceResult.await()
        assertEquals(ACCOUNT_B, repository.lastAddBalanceAccountId)
        assertEquals(ACCOUNT_B, repository.lastAddCardAccountId)
        assertEquals(ACCOUNT_B, repository.lastTransferAccountId)
        assertEquals(ACCOUNT_B, repository.lastPayBillAccountId)
        assertEquals(card, repository.lastAddedCard)
    }

    private fun transaction(id: Long, title: String) = Transaction(
        id = id,
        type = TransactionType.PAY_BILL,
        title = title,
        recipientId = null,
        iconKey = "bill_electricity",
        billerType = "bill_electricity",
        amountMinor = 100L,
        createdAt = id
    )

    private class FakeAuthRepository(
        private val accounts: Map<Long, Account> = emptyMap()
    ) : AuthRepository {
        override suspend fun login(identifier: String, password: String) = LoginResult.InvalidInput
        override suspend fun register(name: String, identifier: String, password: String) =
            RegisterResult.InvalidInput
        override suspend fun getAccount(id: Long) = accounts[id]
        override suspend fun accountExists(identifier: String) = false
    }

    private companion object {
        const val ACCOUNT_A = 10L
        const val ACCOUNT_B = 20L
    }
}
