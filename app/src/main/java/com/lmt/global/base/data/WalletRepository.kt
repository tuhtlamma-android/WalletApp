package com.lmt.global.base.data

import androidx.room.withTransaction
import com.lmt.global.base.data.entity.RecipientEntity
import com.lmt.global.base.data.entity.TransactionEntity
import com.lmt.global.base.data.entity.WalletEntity
import java.util.Locale

class WalletRepository(private val database: AppDatabase) {
    private val dao = database.walletDao()

    val balance = dao.observeBalance()
    val recentRecipients = dao.observeRecentRecipients()
    val latestTransactions = dao.observeLatestTransactions(LATEST_TRANSACTION_LIMIT)
    val history = dao.observeHistory()

    suspend fun addBalance(amountMinor: Long): WalletActionResult {
        if (amountMinor <= 0L) return WalletActionResult.InvalidAmount
        return database.withTransaction {
            ensureWallet()
            dao.addBalance(amountMinor)
            WalletActionResult.Success()
        }
    }

    suspend fun transfer(
        recipientName: String,
        avatarKey: String,
        amountMinor: Long
    ): WalletActionResult {
        val cleanName = recipientName.trim().replace(Regex("\\s+"), " ")
        if (cleanName.isBlank()) return WalletActionResult.InvalidRecipient
        if (amountMinor <= 0L) return WalletActionResult.InvalidAmount

        return database.withTransaction {
            ensureWallet()
            if (dao.debitBalance(amountMinor) == 0) {
                return@withTransaction WalletActionResult.InsufficientBalance
            }

            val now = System.currentTimeMillis()
            val normalizedName = cleanName.lowercase(Locale.ROOT)
            val currentRecipient = dao.findRecipient(normalizedName)
            val recipientId = if (currentRecipient == null) {
                dao.insertRecipient(
                    RecipientEntity(
                        name = cleanName,
                        normalizedName = normalizedName,
                        avatarKey = avatarKey,
                        lastTransferAt = now
                    )
                )
            } else {
                dao.updateRecipient(
                    currentRecipient.copy(
                        name = cleanName,
                        avatarKey = avatarKey,
                        lastTransferAt = now
                    )
                )
                currentRecipient.id
            }

            val transactionId = dao.insertTransaction(
                TransactionEntity(
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

    suspend fun payBill(
        billerName: String,
        iconKey: String,
        amountMinor: Long
    ): WalletActionResult {
        if (billerName.isBlank()) return WalletActionResult.InvalidRecipient
        if (amountMinor <= 0L) return WalletActionResult.InvalidAmount

        return database.withTransaction {
            ensureWallet()
            if (dao.debitBalance(amountMinor) == 0) {
                return@withTransaction WalletActionResult.InsufficientBalance
            }
            val transactionId = dao.insertTransaction(
                TransactionEntity(
                    type = TransactionEntity.TYPE_PAY_BILL,
                    title = billerName.trim(),
                    iconKey = iconKey,
                    amountMinor = amountMinor,
                    createdAt = System.currentTimeMillis()
                )
            )
            WalletActionResult.Success(transactionId)
        }
    }

    private suspend fun ensureWallet() {
        dao.insertWallet(WalletEntity())
    }

    companion object {
        private const val LATEST_TRANSACTION_LIMIT = 5
    }
}

sealed interface WalletActionResult {
    data class Success(val transactionId: Long? = null) : WalletActionResult
    data object InsufficientBalance : WalletActionResult
    data object InvalidAmount : WalletActionResult
    data object InvalidRecipient : WalletActionResult
}
