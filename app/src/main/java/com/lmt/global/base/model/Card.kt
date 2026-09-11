package com.lmt.global.base.model

data class Card(
    val id: String,
    val name: String,
    val cardNumber: String,
    val balanceMinor: Long,
    val createdAt: Long = 0L
)
