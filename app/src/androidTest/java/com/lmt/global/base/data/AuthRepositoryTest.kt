package com.lmt.global.base.data

import android.content.Context
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.data.repository.AuthRepositoryImpl
import com.lmt.global.base.data.security.Pbkdf2PasswordHasher
import com.lmt.global.base.model.LoginResult
import com.lmt.global.base.model.RegisterResult
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class AuthRepositoryTest {
    private lateinit var database: AppDatabase
    private lateinit var repository: AuthRepository

    @Before
    fun setUp() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        database = Room.inMemoryDatabaseBuilder(context, AppDatabase::class.java)
            .allowMainThreadQueries()
            .build()
        repository = AuthRepositoryImpl(database, database.accountDao(), Pbkdf2PasswordHasher())
    }

    @After
    fun tearDown() = database.close()

    @Test
    fun registerNewAccount_normalizesEmailAndDoesNotStorePlaintext() = runBlocking {
        val result = repository.register("  Account   A ", " USER@Example.com ", PASSWORD)

        assertTrue(result is RegisterResult.Success)
        val account = (result as RegisterResult.Success).account
        assertEquals("Account A", account.name)
        assertEquals("user@example.com", account.email)
        assertNotEquals(PASSWORD, database.accountDao().findById(account.id)?.passwordHash)
    }

    @Test
    fun loginWithCorrectPassword_returnsAccount() = runBlocking {
        repository.register("Account A", "user@example.com", PASSWORD)

        val result = repository.login("USER@example.com", PASSWORD)

        assertTrue(result is LoginResult.Success)
    }

    @Test
    fun loginWithWrongPassword_returnsWrongPassword() = runBlocking {
        repository.register("Account A", "user@example.com", PASSWORD)

        assertEquals(LoginResult.WrongPassword, repository.login("user@example.com", "wrong-password"))
    }

    @Test
    fun loginWithUnknownIdentifier_returnsAccountNotFound() = runBlocking {
        assertEquals(LoginResult.AccountNotFound, repository.login("missing@example.com", PASSWORD))
    }

    @Test
    fun duplicateEmail_returnsAlreadyExists() = runBlocking {
        repository.register("Account A", "user@example.com", PASSWORD)

        assertEquals(
            RegisterResult.AlreadyExists,
            repository.register("Account B", "USER@EXAMPLE.COM", PASSWORD)
        )
    }

    @Test
    fun duplicateNormalizedPhone_returnsAlreadyExists() = runBlocking {
        repository.register("Account A", "+962 79 123 4567", PASSWORD)

        assertEquals(
            RegisterResult.AlreadyExists,
            repository.register("Account B", "+962-79-123-4567", PASSWORD)
        )
    }

    private companion object {
        const val PASSWORD = "secret123"
    }
}
