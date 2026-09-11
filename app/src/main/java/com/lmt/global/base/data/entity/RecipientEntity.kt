package com.lmt.global.base.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(
    tableName = "recipients",
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId", "normalizedName"], unique = true),
        Index(value = ["accountId", "lastTransferAt"])
    ]
)
data class RecipientEntity(
    @PrimaryKey(autoGenerate = true) val id: Long = 0L,
    val accountId: Long,
    val name: String,
    val normalizedName: String,
    val avatarKey: String,
    val lastTransferAt: Long
)
