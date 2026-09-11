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

    val MIGRATION_3_4 = object : Migration(3, 4) {
        override fun migrate(db: SupportSQLiteDatabase) {
            db.execSQL(
                """
                CREATE TABLE IF NOT EXISTS `accounts` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `name` TEXT NOT NULL,
                    `email` TEXT,
                    `phone` TEXT,
                    `passwordHash` TEXT NOT NULL,
                    `createdAt` INTEGER NOT NULL
                )
                """.trimIndent()
            )
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_accounts_email` ON `accounts` (`email`)")
            db.execSQL("CREATE UNIQUE INDEX IF NOT EXISTS `index_accounts_phone` ON `accounts` (`phone`)")
            db.execSQL(
                """
                INSERT OR IGNORE INTO `accounts`
                    (`id`, `name`, `email`, `phone`, `passwordHash`, `createdAt`)
                VALUES
                    ($LEGACY_ACCOUNT_ID, 'Legacy local account', NULL, NULL, 'legacy-login-disabled', 0)
                """.trimIndent()
            )

            db.execSQL("ALTER TABLE `wallet` RENAME TO `wallet_legacy`")
            db.execSQL(
                """
                CREATE TABLE `wallet` (
                    `accountId` INTEGER NOT NULL,
                    `balanceMinor` INTEGER NOT NULL,
                    PRIMARY KEY(`accountId`),
                    FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "INSERT INTO `wallet` (`accountId`, `balanceMinor`) " +
                    "SELECT $LEGACY_ACCOUNT_ID, `balanceMinor` FROM `wallet_legacy` WHERE `id` = 1"
            )

            db.execSQL("ALTER TABLE `cards` RENAME TO `cards_legacy`")
            db.execSQL("DROP INDEX IF EXISTS `index_cards_cardNumber`")
            db.execSQL("DROP INDEX IF EXISTS `index_cards_createdAt`")
            db.execSQL(
                """
                CREATE TABLE `cards` (
                    `accountId` INTEGER NOT NULL,
                    `id` TEXT NOT NULL,
                    `name` TEXT NOT NULL,
                    `cardNumber` TEXT NOT NULL,
                    `balanceMinor` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    PRIMARY KEY(`accountId`, `id`),
                    FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "INSERT INTO `cards` (`accountId`, `id`, `name`, `cardNumber`, `balanceMinor`, `createdAt`) " +
                    "SELECT $LEGACY_ACCOUNT_ID, `id`, `name`, `cardNumber`, `balanceMinor`, `createdAt` FROM `cards_legacy`"
            )
            db.execSQL("CREATE UNIQUE INDEX `index_cards_accountId_cardNumber` ON `cards` (`accountId`, `cardNumber`)")
            db.execSQL("CREATE INDEX `index_cards_accountId_createdAt` ON `cards` (`accountId`, `createdAt`)")

            db.execSQL("ALTER TABLE `transactions` RENAME TO `transactions_legacy`")
            db.execSQL("DROP INDEX IF EXISTS `index_transactions_recipientId`")
            db.execSQL("DROP INDEX IF EXISTS `index_transactions_createdAt`")
            db.execSQL("ALTER TABLE `recipients` RENAME TO `recipients_legacy`")
            db.execSQL("DROP INDEX IF EXISTS `index_recipients_normalizedName`")
            db.execSQL(
                """
                CREATE TABLE `recipients` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `accountId` INTEGER NOT NULL,
                    `name` TEXT NOT NULL,
                    `normalizedName` TEXT NOT NULL,
                    `avatarKey` TEXT NOT NULL,
                    `lastTransferAt` INTEGER NOT NULL,
                    FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE
                )
                """.trimIndent()
            )
            db.execSQL(
                "INSERT INTO `recipients` (`id`, `accountId`, `name`, `normalizedName`, `avatarKey`, `lastTransferAt`) " +
                    "SELECT `id`, $LEGACY_ACCOUNT_ID, `name`, `normalizedName`, `avatarKey`, `lastTransferAt` FROM `recipients_legacy`"
            )
            db.execSQL("CREATE UNIQUE INDEX `index_recipients_accountId_normalizedName` ON `recipients` (`accountId`, `normalizedName`)")
            db.execSQL("CREATE INDEX `index_recipients_accountId_lastTransferAt` ON `recipients` (`accountId`, `lastTransferAt`)")

            db.execSQL(
                """
                CREATE TABLE `transactions` (
                    `id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
                    `accountId` INTEGER NOT NULL,
                    `type` TEXT NOT NULL,
                    `title` TEXT NOT NULL,
                    `recipientId` INTEGER,
                    `iconKey` TEXT NOT NULL,
                    `billerType` TEXT,
                    `amountMinor` INTEGER NOT NULL,
                    `createdAt` INTEGER NOT NULL,
                    FOREIGN KEY(`accountId`) REFERENCES `accounts`(`id`) ON UPDATE NO ACTION ON DELETE CASCADE,
                    FOREIGN KEY(`recipientId`) REFERENCES `recipients`(`id`) ON UPDATE NO ACTION ON DELETE SET NULL
                )
                """.trimIndent()
            )
            db.execSQL(
                "INSERT INTO `transactions` (`id`, `accountId`, `type`, `title`, `recipientId`, `iconKey`, `billerType`, `amountMinor`, `createdAt`) " +
                    "SELECT `id`, $LEGACY_ACCOUNT_ID, `type`, `title`, `recipientId`, `iconKey`, `billerType`, `amountMinor`, `createdAt` FROM `transactions_legacy`"
            )
            db.execSQL("CREATE INDEX `index_transactions_recipientId` ON `transactions` (`recipientId`)")
            db.execSQL("CREATE INDEX `index_transactions_accountId_createdAt` ON `transactions` (`accountId`, `createdAt`)")

            db.execSQL("DROP TABLE `transactions_legacy`")
            db.execSQL("DROP TABLE `recipients_legacy`")
            db.execSQL("DROP TABLE `cards_legacy`")
            db.execSQL("DROP TABLE `wallet_legacy`")
        }
    }

    private const val LEGACY_ACCOUNT_ID = 1L
}
