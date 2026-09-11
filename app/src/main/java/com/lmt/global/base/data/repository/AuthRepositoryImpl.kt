package com.lmt.global.base.data.repository

import androidx.room.withTransaction
import com.lmt.global.base.data.AppDatabase
import com.lmt.global.base.data.dao.AccountDao
import com.lmt.global.base.data.entity.AccountEntity
import com.lmt.global.base.data.mapper.AccountMapper
import com.lmt.global.base.data.security.PasswordHasher
import com.lmt.global.base.model.LoginResult
import com.lmt.global.base.model.RegisterResult
import java.util.Locale
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

class AuthRepositoryImpl(
    private val database: AppDatabase,
    private val accountDao: AccountDao,
    private val passwordHasher: PasswordHasher
) : AuthRepository {
    override suspend fun login(identifier: String, password: String): LoginResult {
        val normalized = normalize(identifier) ?: return LoginResult.InvalidInput
        if (password.isBlank()) return LoginResult.InvalidInput
        val entity = find(normalized) ?: return LoginResult.AccountNotFound
        val passwordMatches = withContext(Dispatchers.Default) {
            passwordHasher.verify(password, entity.passwordHash)
        }
        if (!passwordMatches) return LoginResult.WrongPassword
        return LoginResult.Success(AccountMapper.toModel(entity))
    }

    override suspend fun register(
        name: String,
        identifier: String,
        password: String
    ): RegisterResult {
        val cleanName = name.trim().replace(Regex("\\s+"), " ")
        val normalized = normalize(identifier)
        if (cleanName.isBlank() || normalized == null || password.length < MIN_PASSWORD_LENGTH) {
            return RegisterResult.InvalidInput
        }
        if (find(normalized) != null) return RegisterResult.AlreadyExists
        val passwordHash = withContext(Dispatchers.Default) { passwordHasher.hash(password) }
        return database.withTransaction {
            if (find(normalized) != null) return@withTransaction RegisterResult.AlreadyExists
            val account = AccountEntity(
                name = cleanName,
                email = normalized.value.takeIf { normalized.isEmail },
                phone = normalized.value.takeUnless { normalized.isEmail },
                passwordHash = passwordHash,
                createdAt = System.currentTimeMillis()
            )
            val id = accountDao.insert(account)
            if (id == -1L) RegisterResult.AlreadyExists
            else RegisterResult.Success(AccountMapper.toModel(account.copy(id = id)))
        }
    }

    override suspend fun getAccount(id: Long) = accountDao.findById(id)?.let(AccountMapper::toModel)

    override suspend fun accountExists(identifier: String): Boolean {
        val normalized = normalize(identifier) ?: return false
        return find(normalized) != null
    }

    private suspend fun find(identifier: NormalizedIdentifier): AccountEntity? =
        if (identifier.isEmail) accountDao.findByEmail(identifier.value)
        else accountDao.findByPhone(identifier.value)

    private fun normalize(raw: String): NormalizedIdentifier? {
        val trimmed = raw.trim()
        if (EMAIL_PATTERN.matches(trimmed)) {
            return NormalizedIdentifier(trimmed.lowercase(Locale.ROOT), isEmail = true)
        }
        val phone = trimmed.filterNot { it.isWhitespace() || it == '-' || it == '(' || it == ')' }
        if (!PHONE_PATTERN.matches(phone)) return null
        return NormalizedIdentifier(phone, isEmail = false)
    }

    private data class NormalizedIdentifier(val value: String, val isEmail: Boolean)

    private companion object {
        const val MIN_PASSWORD_LENGTH = 6
        val EMAIL_PATTERN = Regex("^[A-Z0-9._%+-]+@[A-Z0-9.-]+\\.[A-Z]{2,}$", RegexOption.IGNORE_CASE)
        val PHONE_PATTERN = Regex("^\\+[1-9][0-9]{7,14}$")
    }
}
