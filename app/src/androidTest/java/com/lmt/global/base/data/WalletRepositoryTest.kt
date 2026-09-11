package com.lmt.global.base.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.repository.WalletRepositoryImpl
import com.lmt.global.base.data.entity.AccountEntity
import com.lmt.global.base.model.Card
import com.lmt.global.base.model.TransactionType
import com.lmt.global.base.model.WalletActionResult
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalletRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: WalletRepository
    private var accountId: Long = 0L

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WalletRepositoryImpl(database, database.walletDao())
        accountId = runBlocking { createAccount("Account A", "a@example.com") }
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun balance_defaultsToZeroUntilFirstTopUp() = runBlocking {
        assertEquals(0L, repository.observeBalance(accountId).first())

        repository.addBalance(accountId, 100_000L)

        assertEquals(100_000L, repository.observeBalance(accountId).first())
    }

    @Test
    fun transfer_debitsBalanceAndCreatesHistoryAndRecentRecipient() = runBlocking {
        repository.addBalance(accountId, 100_000L)

        val result = repository.transfer(accountId, "Anh An", "avatar_steve", 40_000L)

        assertTrue(result is WalletActionResult.Success)
        assertTrue((result as WalletActionResult.Success).transactionId != null)
        assertEquals(60_000L, repository.observeBalance(accountId).first())
        assertEquals("Anh An", repository.observeRecentRecipients(accountId).first().single().name)
        assertEquals(TransactionType.TRANSFER, repository.observeHistory(accountId).first().single().type)
    }

    @Test
    fun transferWithInsufficientBalance_changesNothing() = runBlocking {
        repository.addBalance(accountId, 100_000L)

        val result = repository.transfer(accountId, "Anh An", "avatar_steve", 100_001L)

        assertEquals(WalletActionResult.InsufficientBalance, result)
        assertEquals(100_000L, repository.observeBalance(accountId).first())
        assertTrue(repository.observeRecentRecipients(accountId).first().isEmpty())
        assertTrue(repository.observeHistory(accountId).first().isEmpty())
    }

    @Test
    fun repeatedTransfer_reusesRecipient() = runBlocking {
        repository.addBalance(accountId, 100_000L)

        repository.transfer(accountId, "Anh An", "avatar_steve", 10_000L)
        repository.transfer(accountId, "  ANH   AN  ", "avatar_steve", 10_000L)

        assertEquals(1, repository.observeRecentRecipients(accountId).first().size)
        assertEquals(2, repository.observeHistory(accountId).first().size)
        assertEquals(80_000L, repository.observeBalance(accountId).first())
    }

    @Test
    fun payBill_debitsBalanceAndDoesNotCreateRecipient() = runBlocking {
        repository.addBalance(accountId, 100_000L)

        val result = repository.payBill(accountId, "Electricity", "bill_electricity", 13_232L)

        assertTrue(result is WalletActionResult.Success)
        assertTrue((result as WalletActionResult.Success).transactionId != null)
        assertEquals(86_768L, repository.observeBalance(accountId).first())
        assertTrue(repository.observeRecentRecipients(accountId).first().isEmpty())
        val transaction = repository.observeHistory(accountId).first().single()
        assertEquals(TransactionType.PAY_BILL, transaction.type)
        assertEquals("bill_electricity", transaction.billerType)
    }

    @Test
    fun addCards_persistsCardsAndAddsEachInitialBalance() = runBlocking {
        repository.addBalance(accountId, 100_000_000L)

        val first = repository.addCard(accountId, Card("CARD001", "Visa", "1234567812345678", 50_000_000L))
        val second = repository.addCard(accountId, Card("CARD002", "Mastercard", "9876543212345678", 20_000_000L))

        assertTrue(first is WalletActionResult.Success)
        assertTrue(second is WalletActionResult.Success)
        assertEquals(2, repository.observeCards(accountId).first().size)
        assertEquals(170_000_000L, repository.observeBalance(accountId).first())
    }

    @Test
    fun duplicateOrInvalidCard_changesNeitherCardsNorBalance() = runBlocking {
        repository.addBalance(accountId, 100_000_000L)
        repository.addCard(accountId, Card("CARD001", "Visa", "1234567812345678", 50_000_000L))

        val duplicateId = repository.addCard(accountId, Card("CARD001", "Other", "9876543212345678", 20_000_000L))
        val duplicateNumber = repository.addCard(accountId, Card("CARD002", "Other", "1234567812345678", 20_000_000L))
        val invalid = repository.addCard(accountId, Card("", "", "123", -1L))
        val nonNumeric = repository.addCard(accountId, Card("CARD003", "Other", "1234abcd567812345678", 20_000_000L))

        assertEquals(WalletActionResult.DuplicateCard, duplicateId)
        assertEquals(WalletActionResult.DuplicateCard, duplicateNumber)
        assertEquals(WalletActionResult.InvalidCard, invalid)
        assertEquals(WalletActionResult.InvalidCard, nonNumeric)
        assertEquals(1, repository.observeCards(accountId).first().size)
        assertEquals(150_000_000L, repository.observeBalance(accountId).first())
    }

    @Test
    fun billPayment_withInsufficientBalanceChangesNothing() = runBlocking {
        repository.addBalance(accountId, 100_000L)

        val result = repository.payBill(accountId, "Electricity", "bill_electricity", 100_001L)

        assertEquals(WalletActionResult.InsufficientBalance, result)
        assertEquals(100_000L, repository.observeBalance(accountId).first())
        assertTrue(repository.observeHistory(accountId).first().isEmpty())
    }

    @Test
    fun accountOwnedWalletData_isStrictlyIsolated() = runBlocking {
        val accountB = createAccount("Account B", "b@example.com")
        repository.addBalance(accountId, 100_000L)
        repository.addCard(accountId, Card("A1", "A card", "1111111111111111", 0L))
        repository.payBill(accountId, "Electricity", "bill_electricity", 10_000L)

        assertEquals(0L, repository.observeBalance(accountB).first())
        assertTrue(repository.observeCards(accountB).first().isEmpty())
        assertTrue(repository.observeHistory(accountB).first().isEmpty())

        repository.addCard(accountB, Card("B1", "B card", "2222222222222222", 0L))

        assertEquals(listOf("A1"), repository.observeCards(accountId).first().map(Card::id))
        assertEquals(listOf("B1"), repository.observeCards(accountB).first().map(Card::id))
        assertEquals("Electricity", repository.observeHistory(accountId).first().single().title)
        assertTrue(repository.observeHistory(accountB).first().isEmpty())
    }

    private suspend fun createAccount(name: String, email: String): Long =
        database.accountDao().insert(
            AccountEntity(
                name = name,
                email = email,
                phone = null,
                passwordHash = "test-only",
                createdAt = 1L
            )
        )
}
