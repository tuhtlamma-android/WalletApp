package com.lmt.global.base.data

import androidx.room.Database
import androidx.room.RoomDatabase
import com.lmt.global.base.data.dao.WalletDao
import com.lmt.global.base.data.entity.RecipientEntity
import com.lmt.global.base.data.entity.TransactionEntity
import com.lmt.global.base.data.entity.WalletEntity

@Database(
    entities = [
        WalletEntity::class,
        RecipientEntity::class,
        TransactionEntity::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun walletDao(): WalletDao

    companion object {
        const val DATABASE_NAME = "wallet.db"
    }
}
