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
import com.lmt.global.base.model.ChangePasswordResult
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

    @Test
    fun changePasswordWithCorrectCurrentPassword_updatesStoredHashAndLoginCredential() = runBlocking {
        val account = (repository.register(
            "Account A",
            "user@example.com",
            PASSWORD
        ) as RegisterResult.Success).account
        val oldHash = database.accountDao().findById(account.id)?.passwordHash

        val result = repository.changePassword(account.id, PASSWORD, NEW_PASSWORD)

        assertEquals(ChangePasswordResult.Success, result)
        assertNotEquals(oldHash, database.accountDao().findById(account.id)?.passwordHash)
        assertEquals(LoginResult.WrongPassword, repository.login("user@example.com", PASSWORD))
        assertTrue(repository.login("user@example.com", NEW_PASSWORD) is LoginResult.Success)
    }

    @Test
    fun changePasswordWithWrongCurrentPassword_keepsExistingCredential() = runBlocking {
        val account = (repository.register(
            "Account A",
            "user@example.com",
            PASSWORD
        ) as RegisterResult.Success).account

        val result = repository.changePassword(account.id, "wrong-password", NEW_PASSWORD)

        assertEquals(ChangePasswordResult.WrongCurrentPassword, result)
        assertTrue(repository.login("user@example.com", PASSWORD) is LoginResult.Success)
    }

    @Test
    fun changePasswordRejectsCurrentPasswordAsNewPassword() = runBlocking {
        val account = (repository.register(
            "Account A",
            "user@example.com",
            PASSWORD
        ) as RegisterResult.Success).account

        assertEquals(
            ChangePasswordResult.SamePassword,
            repository.changePassword(account.id, PASSWORD, PASSWORD)
        )
    }

    private companion object {
        const val PASSWORD = "secret123"
        const val NEW_PASSWORD = "new-secret456"
    }
}
