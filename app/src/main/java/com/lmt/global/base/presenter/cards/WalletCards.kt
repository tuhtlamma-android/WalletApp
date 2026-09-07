package com.lmt.global.base.presenter.cards

import androidx.annotation.ColorRes
import androidx.annotation.DrawableRes
import com.lmt.global.base.R

internal data class WalletCard(
    val number: String,
    val balance: String,
    @param:DrawableRes val backgroundRes: Int,
    @param:ColorRes val contentColorRes: Int
)

internal object WalletCards {
    val all = listOf(
        WalletCard(
            number = "**** 2312",
            balance = "$1,245",
            backgroundRes = R.drawable.bg_wallet_card_light,
            contentColorRes = R.color.wallet_black
        ),
        WalletCard(
            number = "**** 5432",
            balance = "$5,820",
            backgroundRes = R.drawable.bg_wallet_card_mid,
            contentColorRes = R.color.white
        ),
        WalletCard(
            number = "**** 3245",
            balance = "$2,354",
            backgroundRes = R.drawable.bg_wallet_card,
            contentColorRes = R.color.white
        )
    )
}
