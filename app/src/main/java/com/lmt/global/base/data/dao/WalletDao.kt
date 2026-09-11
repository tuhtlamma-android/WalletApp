package com.lmt.global.base.data.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.lmt.global.base.data.entity.RecipientEntity
import com.lmt.global.base.data.entity.TransactionEntity
import com.lmt.global.base.data.entity.WalletEntity
import com.lmt.global.base.data.entity.CardEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface WalletDao {
    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertWallet(wallet: WalletEntity): Long

    @Query("SELECT balanceMinor FROM wallet WHERE accountId = :accountId")
    fun observeBalance(accountId: Long): Flow<Long?>

    @Query("UPDATE wallet SET balanceMinor = balanceMinor + :amountMinor WHERE accountId = :accountId")
    suspend fun addBalance(accountId: Long, amountMinor: Long): Int

    @Query(
        "UPDATE wallet SET balanceMinor = balanceMinor - :amountMinor " +
            "WHERE accountId = :accountId AND balanceMinor >= :amountMinor"
    )
    suspend fun debitBalance(accountId: Long, amountMinor: Long): Int

    @Insert(onConflict = OnConflictStrategy.IGNORE)
    suspend fun insertCard(card: CardEntity): Long

    @Query("SELECT * FROM cards WHERE accountId = :accountId ORDER BY createdAt DESC, id ASC")
    fun observeCards(accountId: Long): Flow<List<CardEntity>>

    @Query("SELECT * FROM cards WHERE accountId = :accountId AND id = :cardId LIMIT 1")
    fun observeCard(accountId: Long, cardId: String): Flow<CardEntity?>

    @Query("SELECT * FROM recipients WHERE accountId = :accountId AND normalizedName = :normalizedName LIMIT 1")
    suspend fun findRecipient(accountId: Long, normalizedName: String): RecipientEntity?

    @Insert
    suspend fun insertRecipient(recipient: RecipientEntity): Long

    @Update
    suspend fun updateRecipient(recipient: RecipientEntity)

    @Query("SELECT * FROM recipients WHERE accountId = :accountId ORDER BY lastTransferAt DESC")
    fun observeRecentRecipients(accountId: Long): Flow<List<RecipientEntity>>

    @Insert
    suspend fun insertTransaction(transaction: TransactionEntity): Long

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY createdAt DESC, id DESC LIMIT :limit")
    fun observeLatestTransactions(accountId: Long, limit: Int): Flow<List<TransactionEntity>>

    @Query("SELECT * FROM transactions WHERE accountId = :accountId ORDER BY createdAt DESC, id DESC")
    fun observeHistory(accountId: Long): Flow<List<TransactionEntity>>
}
