package com.lmt.global.base.presenter.wallet

data class WalletTransaction(
    val id: Long,
    val iconKey: String,
    val merchant: String,
    val createdAt: Long,
    val amountMinor: Long,
    val type: String,
    val sectionLabel: String? = null
)

data class WalletContact(
    val id: Long,
    val avatarKey: String,
    val name: String,
    val phone: String = "",
    val sectionLabel: String? = null
)

data class WalletMenuItem(
    val symbol: String,
    val title: String,
    val tint: Int,
    val action: String? = null
)
