package com.lmt.global.base.data

import androidx.room.migration.Migration
import androidx.sqlite.db.SupportSQLiteDatabase

object DatabaseMigrations {
    val MIGRATION_2_3 = object : Migration(2, 3) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `cards` (
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `cardNumber` TEXT NOT NULL,
                    `balanceMinor` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`id`)
                )
                """.trimIndent()
            )
            db.execSQL(
                "CREATE UNIQUE INDEX IF NOT EXISTS `index_cards_cardNumber` ON `cards` (`cardNumber`)"
            )
            db.execSQL(
                "CREATE INDEX IF NOT EXISTS `index_cards_createdAt` ON `cards` (`createdAt`)"
            )
            db.execSQL("ALTER TABLE `transactions` ADD COLUMN `billerType` TEXT")
        }
    }
}
