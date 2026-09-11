package com.lmt.global.base.model

sealed interface WalletActionResult {
    data class Success(val transactionId: Long? = null) : WalletActionResult
    data object InsufficientBalance : WalletActionResult
    data object InvalidAmount : WalletActionResult
    data object InvalidRecipient : WalletActionResult
    data object InvalidCard : WalletActionResult
    data object DuplicateCard : WalletActionResult
    data object NoActiveAccount : WalletActionResult
}
