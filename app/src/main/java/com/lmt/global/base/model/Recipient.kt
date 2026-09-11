package com.lmt.global.base.model

data class Recipient(
    val id: Long,
    val name: String,
    val avatarKey: String,
    val lastTransferAt: Long
)
