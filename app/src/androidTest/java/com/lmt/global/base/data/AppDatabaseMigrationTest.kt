package com.lmt.global.base.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AppDatabaseMigrationTest {
    private val context = ApplicationProvider.getApplicationContext<Context>()
    private var database: AppDatabase? = null

    @Before
    fun setUp() {
        context.deleteDatabase(TEST_DATABASE)
    }

    @After
    fun tearDown() {
        database?.close()
        context.deleteDatabase(TEST_DATABASE)
    }

    @Test
    fun migration2To3_preservesWalletAndTransactionsAndAddsCards() = runBlocking {
        createVersion2Database()

        database = Room.databaseBuilder(context, AppDatabase::class.java, TEST_DATABASE)
            .addMigrations(DatabaseMigrations.MIGRATION_2_3)
            .allowMainThreadQueries()
            .build()

        val repository = WalletRepository(requireNotNull(database))
        assertEquals(123_456L, repository.balance.first())
        assertEquals("Electricity", repository.history.first().single().title)
        assertTrue(repository.cards.first().isEmpty())
    }

    private fun createVersion2Database() {
        val sqlite = SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(TEST_DATABASE), null)
        sqlite.execSQL(
            "CREATE TABLE wallet (id INTEGER NOT NULL, balanceMinor INTEGER NOT NULL, PRIMARY KEY(id))"
        )
        sqlite.execSQL(
            "CREATE TABLE recipients (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, name TEXT NOT NULL, normalizedName TEXT NOT NULL, avatarKey TEXT NOT NULL, lastTransferAt INTEGER NOT NULL)"
        )
        sqlite.execSQL(
            "CREATE UNIQUE INDEX index_recipients_normalizedName ON recipients (normalizedName)"
        )
        sqlite.execSQL(
            "CREATE TABLE transactions (id INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, type TEXT NOT NULL, title TEXT NOT NULL, recipientId INTEGER, iconKey TEXT NOT NULL, amountMinor INTEGER NOT NULL, createdAt INTEGER NOT NULL, FOREIGN KEY(recipientId) REFERENCES recipients(id) ON UPDATE NO ACTION ON DELETE SET NULL)"
        )
        sqlite.execSQL("CREATE INDEX index_transactions_recipientId ON transactions (recipientId)")
        sqlite.execSQL("CREATE INDEX index_transactions_createdAt ON transactions (createdAt)")
        sqlite.execSQL("INSERT INTO wallet (id, balanceMinor) VALUES (1, 123456)")
        sqlite.execSQL(
            "INSERT INTO transactions (type, title, recipientId, iconKey, amountMinor, createdAt) VALUES ('PAY_BILL', 'Electricity', NULL, 'bill_electricity', 1000, 1)"
        )
        sqlite.version = 2
        sqlite.close()
    }

    private companion object {
        const val TEST_DATABASE = "wallet-migration-test.db"
    }
}
