package com.lmt.global.base.presenter.wallet

import androidx.annotation.DrawableRes

data class WalletTransaction(
    @DrawableRes val iconRes: Int,
    val merchant: String,
    val date: String,
    val amount: String,
    val incoming: Boolean = false,
    val sectionLabel: String? = null
)

data class WalletContact(
    @DrawableRes val avatarRes: Int,
    val name: String,
    val phone: String,
    val sectionLabel: String? = null
)

data class WalletMenuItem(
    val symbol: String,
    val title: String,
    val tint: Int,
    val action: String? = null
)
