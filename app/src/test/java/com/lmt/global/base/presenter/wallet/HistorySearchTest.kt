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

    @Test
    fun transferFilter_onlyMatchesTransfers() {
        val filters = setOf(HistoryTransactionFilter.TRANSFER)

        assertTrue(transaction.matchesHistoryFilters(filters))
        assertFalse(billPayment.matchesHistoryFilters(filters))
    }

    @Test
    fun allBillersFilter_matchesEveryBillPayment() {
        val electricityPayment = billPayment.copy(
            id = 3L,
            iconKey = WalletVisuals.BILL_ELECTRICITY,
            merchant = "Electricity"
        )
        val filters = setOf(HistoryTransactionFilter.ALL_BILLERS)

        assertTrue(billPayment.matchesHistoryFilters(filters))
        assertTrue(electricityPayment.matchesHistoryFilters(filters))
        assertFalse(transaction.matchesHistoryFilters(filters))
    }

    @Test
    fun specificBillerFilter_onlyMatchesItsBillerType() {
        val electricityPayment = billPayment.copy(
            id = 3L,
            iconKey = WalletVisuals.BILL_ELECTRICITY,
            merchant = "Electricity"
        )
        val filters = setOf(HistoryTransactionFilter.WATER)

        assertTrue(billPayment.matchesHistoryFilters(filters))
        assertFalse(electricityPayment.matchesHistoryFilters(filters))
    }

    @Test
    fun multipleFilters_useOrWhileSearchAndFilterUseAnd() {
        val filters = setOf(
            HistoryTransactionFilter.TRANSFER,
            HistoryTransactionFilter.WATER
        )

        assertTrue(transaction.matchesHistoryFilters(filters))
        assertTrue(billPayment.matchesHistoryFilters(filters))
        assertTrue(
            billPayment.matchesHistoryFilters(filters) &&
                billPayment.matchesHistoryQuery("wat")
        )
        assertFalse(
            transaction.matchesHistoryFilters(filters) &&
                transaction.matchesHistoryQuery("wat")
        )
    }

    @Test
    fun emptyFilters_keepEveryTransactionVisible() {
        assertTrue(transaction.matchesHistoryFilters(emptySet()))
        assertTrue(billPayment.matchesHistoryFilters(emptySet()))
    }
}
