package com.lmt.global.base.data.repository

import androidx.room.withTransaction
import com.lmt.global.base.data.AppDatabase
import com.lmt.global.base.data.dao.WalletDao
import com.lmt.global.base.data.mapper.CardMapper
import com.lmt.global.base.data.mapper.RecipientMapper
import com.lmt.global.base.data.mapper.TransactionMapper
import com.lmt.global.base.data.entity.RecipientEntity
import com.lmt.global.base.data.entity.TransactionEntity
import com.lmt.global.base.data.entity.WalletEntity
import com.lmt.global.base.model.Card
import com.lmt.global.base.model.WalletActionResult
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.util.Locale

class WalletRepositoryImpl(
    private val database: AppDatabase,
    private val walletDao: WalletDao
) : WalletRepository {

    override fun observeBalance(accountId: Long): Flow<Long> = walletDao.observeBalance(accountId).map { balance ->
        balance ?: 0L
    }

    override fun observeCards(accountId: Long): Flow<List<Card>> = walletDao.observeCards(accountId).map { entities ->
        entities.map(CardMapper::toModel)
    }

    override fun observeCard(accountId: Long, cardId: String): Flow<Card?> = walletDao.observeCard(accountId, cardId).map {
        it?.let(CardMapper::toModel)
    }

    override fun observeRecentRecipients(accountId: Long) = walletDao.observeRecentRecipients(accountId).map { entities ->
        entities.map(RecipientMapper::toModel)
    }

    override fun observeLatestTransactions(accountId: Long, limit: Int) =
        walletDao.observeLatestTransactions(accountId, limit).map { entities ->
            entities.map(TransactionMapper::toModel)
        }

    override fun observeHistory(accountId: Long) = walletDao.observeHistory(accountId).map { entities ->
        entities.map(TransactionMapper::toModel)
    }

    override suspend fun addBalance(accountId: Long, amountMinor: Long): WalletActionResult {
        if (accountId <= 0L) return WalletActionResult.NoActiveAccount
        if (amountMinor <= 0L) return WalletActionResult.InvalidAmount
        return database.withTransaction {
            ensureWallet(accountId)
            walletDao.addBalance(accountId, amountMinor)
            WalletActionResult.Success()
        }
    }

    override suspend fun addCard(accountId: Long, card: Card): WalletActionResult {
        if (accountId <= 0L) return WalletActionResult.NoActiveAccount
        val cleanCard = card.copy(
            id = card.id.trim().uppercase(Locale.ROOT),
            name = card.name.trim().replace(Regex("\\s+"), " "),
            cardNumber = card.cardNumber.filter(Char::isDigit),
            createdAt = card.createdAt.takeIf { it > 0L } ?: System.currentTimeMillis()
        )
        val hasInvalidNumberCharacter = card.cardNumber.any {
            !it.isDigit() && !it.isWhitespace()
        }
        if (
            cleanCard.id.isBlank() || cleanCard.name.isBlank() || hasInvalidNumberCharacter ||
            cleanCard.cardNumber.length != CARD_NUMBER_LENGTH || cleanCard.balanceMinor < 0L
        ) {
            return WalletActionResult.InvalidCard
        }

        return database.withTransaction {
            ensureWallet(accountId)
            val inserted = walletDao.insertCard(CardMapper.toEntity(cleanCard, accountId))
            if (inserted == -1L) return@withTransaction WalletActionResult.DuplicateCard
            if (cleanCard.balanceMinor > 0L) walletDao.addBalance(accountId, cleanCard.balanceMinor)
            WalletActionResult.Success()
        }
    }

    override suspend fun transfer(
        accountId: Long,
        recipientName: String,
        avatarKey: String,
        amountMinor: Long
    ): WalletActionResult {
        if (accountId <= 0L) return WalletActionResult.NoActiveAccount
        val cleanName = recipientName.trim().replace(Regex("\\s+"), " ")
        if (cleanName.isBlank()) return WalletActionResult.InvalidRecipient
        if (amountMinor <= 0L) return WalletActionResult.InvalidAmount

        return database.withTransaction {
            ensureWallet(accountId)
            if (walletDao.debitBalance(accountId, amountMinor) == 0) {
                return@withTransaction WalletActionResult.InsufficientBalance
            }

            val now = System.currentTimeMillis()
            val normalizedName = cleanName.lowercase(Locale.ROOT)
            val currentRecipient = walletDao.findRecipient(accountId, normalizedName)
            val recipientId = if (currentRecipient == null) {
                walletDao.insertRecipient(
                    RecipientEntity(
                        accountId = accountId,
                        name = cleanName,
                        normalizedName = normalizedName,
                        avatarKey = avatarKey,
                        lastTransferAt = now
                    )
                )
            } else {
                walletDao.updateRecipient(
                    currentRecipient.copy(
                        name = cleanName,
                        avatarKey = avatarKey,
                        lastTransferAt = now
                    )
                )
                currentRecipient.id
            }

            val transactionId = walletDao.insertTransaction(
                TransactionEntity(
                    accountId = accountId,
                    type = TransactionEntity.TYPE_TRANSFER,
                    title = cleanName,
                    recipientId = recipientId,
                    iconKey = avatarKey,
                    amountMinor = amountMinor,
                    createdAt = now
                )
            )
            WalletActionResult.Success(transactionId)
        }
    }

    override suspend fun payBill(
        accountId: Long,
        billerName: String,
        iconKey: String,
        amountMinor: Long
    ): WalletActionResult {
        if (accountId <= 0L) return WalletActionResult.NoActiveAccount
        if (billerName.isBlank()) return WalletActionResult.InvalidRecipient
        if (amountMinor <= 0L) return WalletActionResult.InvalidAmount

        return database.withTransaction {
            ensureWallet(accountId)
            if (walletDao.debitBalance(accountId, amountMinor) == 0) {
                return@withTransaction WalletActionResult.InsufficientBalance
            }
            val transactionId = walletDao.insertTransaction(
                TransactionEntity(
                    accountId = accountId,
                    type = TransactionEntity.TYPE_PAY_BILL,
                    title = billerName.trim(),
                    iconKey = iconKey,
                    billerType = iconKey,
                    amountMinor = amountMinor,
                    createdAt = System.currentTimeMillis()
                )
            )
            WalletActionResult.Success(transactionId)
        }
    }

    private suspend fun ensureWallet(accountId: Long) {
        walletDao.insertWallet(WalletEntity(accountId))
    }

    private companion object {
        const val CARD_NUMBER_LENGTH = 16
    }
}
