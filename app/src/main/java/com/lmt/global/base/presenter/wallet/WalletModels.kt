package com.lmt.global.base.presenter.wallet

import com.lmt.global.base.model.TransactionType

data class WalletTransaction(
    val id: Long,
    val iconKey: String,
    val merchant: String,
    val createdAt: Long,
    val amountMinor: Long,
    val type: TransactionType,
    val sectionLabel: String? = null
)

internal fun WalletTransaction.matchesHistoryQuery(query: String): Boolean =
    query.isBlank() || merchant.contains(query.trim(), ignoreCase = true)

enum class HistoryTransactionFilter {
    TRANSFER,
    ALL_BILLERS,
    ELECTRICITY,
    WATER,
    MOBILE_PHONE,
    INTERNET,
    TELEVISION,
    GAS,
    INSURANCE,
    EDUCATION,
    RENT,
    OTHER;

    fun matches(transaction: WalletTransaction): Boolean = when (this) {
        TRANSFER -> transaction.type == TransactionType.TRANSFER
        ALL_BILLERS -> transaction.type == TransactionType.PAY_BILL
        ELECTRICITY -> transaction.matchesBiller(WalletVisuals.BILL_ELECTRICITY)
        WATER -> transaction.matchesBiller(WalletVisuals.BILL_WATER)
        MOBILE_PHONE -> transaction.matchesBiller(WalletVisuals.BILL_PHONE)
        INTERNET -> transaction.matchesBiller(WalletVisuals.BILL_INTERNET)
        TELEVISION -> transaction.matchesBiller(WalletVisuals.BILL_TELEVISION)
        GAS -> transaction.matchesBiller(WalletVisuals.BILL_GAS)
        INSURANCE -> transaction.matchesBiller(WalletVisuals.BILL_INSURANCE)
        EDUCATION -> transaction.matchesBiller(WalletVisuals.BILL_EDUCATION)
        RENT -> transaction.matchesBiller(WalletVisuals.BILL_RENT)
        OTHER -> transaction.matchesBiller(WalletVisuals.BILL_OTHER)
    }
}

internal fun WalletTransaction.matchesHistoryFilters(
    filters: Set<HistoryTransactionFilter>
): Boolean = filters.isEmpty() || filters.any { it.matches(this) }

private fun WalletTransaction.matchesBiller(iconKey: String): Boolean =
    type == TransactionType.PAY_BILL && this.iconKey == iconKey

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
