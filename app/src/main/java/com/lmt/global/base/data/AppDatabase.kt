package com.lmt.global.base.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lmt.global.base.data.dao.WalletDao
import com.lmt.global.base.data.dao.AccountDao
import com.lmt.global.base.data.entity.AccountEntity
import com.lmt.global.base.data.entity.RecipientEntity
import com.lmt.global.base.data.entity.TransactionEntity
import com.lmt.global.base.data.entity.WalletEntity
import com.lmt.global.base.data.entity.CardEntity

@Database(
    entities = [
        AccountEntity::class,
        WalletEntity::class,
        RecipientEntity::class,
        TransactionEntity::class,
        CardEntity::class
    ],
    version = 4,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao
    abstract fun accountDao(): AccountDao

    companion object {
        const val DATABASE_NAME = "wallet.db"
    }
}
