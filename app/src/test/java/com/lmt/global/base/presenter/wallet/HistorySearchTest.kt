package com.lmt.global.base.presenter.wallet

import com.lmt.global.base.model.TransactionType
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class HistorySearchTest {

    private val transaction = WalletTransaction(
        id = 1L,
        iconKey = WalletVisuals.AVATAR_ALI,
        merchant = "huhu",
        createdAt = 0L,
        amountMinor = 1_000L,
        type = TransactionType.TRANSFER
    )

    private val billPayment = transaction.copy(
        id = 2L,
        iconKey = WalletVisuals.BILL_WATER,
        merchant = "Water",
        type = TransactionType.PAY_BILL
    )

    @Test
    fun search_matchesContactOrBillerNameIgnoringCase() {
        assertTrue(transaction.matchesHistoryQuery("h"))
        assertTrue(transaction.matchesHistoryQuery("HUHU"))
        assertTrue(transaction.matchesHistoryQuery("  huh  "))
        assertTrue(billPayment.matchesHistoryQuery("wat"))
    }

    @Test
    fun search_doesNotMatchDateOrTimeText() {
        assertFalse(transaction.matchesHistoryQuery("today"))
        assertFalse(transaction.matchesHistoryQuery("08:57"))
        assertFalse(transaction.matchesHistoryQuery("o"))
    }

    @Test
    fun blankSearch_keepsTransactionVisible() {
        assertTrue(transaction.matchesHistoryQuery("   "))
    }
}
