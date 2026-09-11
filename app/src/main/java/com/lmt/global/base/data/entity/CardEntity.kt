package com.lmt.global.base.data.entity

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index

@Entity(
    tableName = "cards",
    primaryKeys = ["accountId", "id"],
    foreignKeys = [
        ForeignKey(
            entity = AccountEntity::class,
            parentColumns = ["id"],
            childColumns = ["accountId"],
            onDelete = ForeignKey.CASCADE
        )
    ],
    indices = [
        Index(value = ["accountId", "cardNumber"], unique = true),
        Index(value = ["accountId", "createdAt"])
    ]
)
data class CardEntity(
    val accountId: Long,
    val id: String,
    val name: String,
    val cardNumber: String,
    val balanceMinor: Long,
    val createdAt: Long
)
