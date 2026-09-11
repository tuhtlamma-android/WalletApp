package com.lmt.global.base.model

data class Transaction(
    val id: Long,
    val type: TransactionType,
    val title: String,
    val recipientId: Long?,
    val iconKey: String,
    val billerType: String?,
    val amountMinor: Long,
    val createdAt: Long
)

enum class TransactionType(val storageValue: String) {
    TRANSFER("TRANSFER"),
    PAY_BILL("PAY_BILL");

    companion object {
        fun fromStorage(value: String): TransactionType = entries.firstOrNull {
            it.storageValue == value
        } ?: error("Unsupported transaction type: $value")
    }
}
