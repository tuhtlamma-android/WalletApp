package com.lmt.global.base.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lmt.global.base.data.entity.TransactionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class WalletRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: WalletRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = WalletRepository(database)
    }

    @After
    fun tearDown() {
        database.close()
    }

    @Test
    fun balance_isAbsentUntilFirstTopUp() = runBlocking {
        assertNull(repository.balance.first())

        repository.addBalance(100_000L)

        assertEquals(100_000L, repository.balance.first())
    }

    @Test
    fun transfer_debitsBalanceAndCreatesHistoryAndRecentRecipient() = runBlocking {
        repository.addBalance(100_000L)

        val result = repository.transfer("Anh An", "avatar_steve", 40_000L)

        assertTrue(result is WalletActionResult.Success)
        assertTrue((result as WalletActionResult.Success).transactionId != null)
        assertEquals(60_000L, repository.balance.first())
        assertEquals("Anh An", repository.recentRecipients.first().single().name)
        assertEquals(TransactionEntity.TYPE_TRANSFER, repository.history.first().single().type)
    }

    @Test
    fun transferWithInsufficientBalance_changesNothing() = runBlocking {
        repository.addBalance(100_000L)

        val result = repository.transfer("Anh An", "avatar_steve", 100_001L)

        assertEquals(WalletActionResult.InsufficientBalance, result)
        assertEquals(100_000L, repository.balance.first())
        assertTrue(repository.recentRecipients.first().isEmpty())
        assertTrue(repository.history.first().isEmpty())
    }

    @Test
    fun repeatedTransfer_reusesRecipient() = runBlocking {
        repository.addBalance(100_000L)

        repository.transfer("Anh An", "avatar_steve", 10_000L)
        repository.transfer("  ANH   AN  ", "avatar_steve", 10_000L)

        assertEquals(1, repository.recentRecipients.first().size)
        assertEquals(2, repository.history.first().size)
        assertEquals(80_000L, repository.balance.first())
    }

    @Test
    fun payBill_debitsBalanceAndDoesNotCreateRecipient() = runBlocking {
        repository.addBalance(100_000L)

        val result = repository.payBill("Electricity", "bill_electricity", 13_232L)

        assertTrue(result is WalletActionResult.Success)
        assertTrue((result as WalletActionResult.Success).transactionId != null)
        assertEquals(86_768L, repository.balance.first())
        assertTrue(repository.recentRecipients.first().isEmpty())
        val transaction = repository.history.first().single()
        assertEquals(TransactionEntity.TYPE_PAY_BILL, transaction.type)
        assertEquals("bill_electricity", transaction.billerType)
    }

    @Test
    fun addCards_persistsCardsAndAddsEachInitialBalance() = runBlocking {
        repository.addBalance(100_000_000L)

        val first = repository.addCard("CARD001", "Visa", "1234567812345678", 50_000_000L)
        val second = repository.addCard("CARD002", "Mastercard", "9876543212345678", 20_000_000L)

        assertTrue(first is WalletActionResult.Success)
        assertTrue(second is WalletActionResult.Success)
        assertEquals(2, repository.cards.first().size)
        assertEquals(170_000_000L, repository.balance.first())
    }

    @Test
    fun duplicateOrInvalidCard_changesNeitherCardsNorBalance() = runBlocking {
        repository.addBalance(100_000_000L)
        repository.addCard("CARD001", "Visa", "1234567812345678", 50_000_000L)

        val duplicateId = repository.addCard("CARD001", "Other", "9876543212345678", 20_000_000L)
        val duplicateNumber = repository.addCard("CARD002", "Other", "1234567812345678", 20_000_000L)
        val invalid = repository.addCard("", "", "123", -1L)
        val nonNumeric = repository.addCard("CARD003", "Other", "1234abcd567812345678", 20_000_000L)

        assertEquals(WalletActionResult.DuplicateCard, duplicateId)
        assertEquals(WalletActionResult.DuplicateCard, duplicateNumber)
        assertEquals(WalletActionResult.InvalidCard, invalid)
        assertEquals(WalletActionResult.InvalidCard, nonNumeric)
        assertEquals(1, repository.cards.first().size)
        assertEquals(150_000_000L, repository.balance.first())
    }

    @Test
    fun billPayment_withInsufficientBalanceChangesNothing() = runBlocking {
        repository.addBalance(100_000L)

        val result = repository.payBill("Electricity", "bill_electricity", 100_001L)

        assertEquals(WalletActionResult.InsufficientBalance, result)
        assertEquals(100_000L, repository.balance.first())
        assertTrue(repository.history.first().isEmpty())
    }
}
