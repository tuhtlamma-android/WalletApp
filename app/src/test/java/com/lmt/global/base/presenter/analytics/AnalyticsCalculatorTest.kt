package com.lmt.global.base.presenter.analytics

import com.lmt.global.base.model.Transaction
import com.lmt.global.base.model.TransactionType
import com.lmt.global.base.presenter.wallet.FakeSessionManager
import com.lmt.global.base.presenter.wallet.FakeWalletRepository
import com.lmt.global.base.presenter.wallet.MainDispatcherRule
import com.lmt.global.base.presenter.wallet.WalletVisuals
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Rule
import org.junit.Test
import org.threeten.bp.ZoneId
import org.threeten.bp.ZonedDateTime

@OptIn(ExperimentalCoroutinesApi::class)
class AnalyticsCalculatorTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun lastSevenDays_excludesOlderAndFutureTransactions() {
        val result = AnalyticsCalculator.calculate(
            transactions = listOf(
                transaction(1L, 100L, timestamp(2026, 9, 9), TransactionType.TRANSFER),
                transaction(2L, 900L, timestamp(2026, 9, 8), TransactionType.TRANSFER),
                transaction(
                    3L,
                    300L,
                    timestamp(2026, 9, 15),
                    TransactionType.PAY_BILL,
                    WalletVisuals.BILL_WATER
                ),
                transaction(4L, 500L, timestamp(2026, 9, 16), TransactionType.TRANSFER)
            ),
            period = AnalyticsPeriod.LAST_7_DAYS,
            nowMillis = NOW,
            zoneId = UTC
        )

        assertEquals(400L, result.totalSpentMinor)
        assertEquals(2, result.transactionCount)
        assertEquals(200L, result.averageTransactionMinor)
        assertEquals(7, result.trend.size)
        assertEquals(400L, result.trend.sumOf(AnalyticsTrendPoint::amountMinor))
    }

    @Test
    fun categories_groupAndSortSpendingWithCalculatedShares() {
        val result = AnalyticsCalculator.calculate(
            transactions = listOf(
                transaction(1L, 100L, timestamp(2026, 9, 14), TransactionType.TRANSFER),
                transaction(
                    2L,
                    300L,
                    timestamp(2026, 9, 15),
                    TransactionType.PAY_BILL,
                    WalletVisuals.BILL_WATER
                ),
                transaction(
                    3L,
                    100L,
                    timestamp(2026, 9, 15),
                    TransactionType.PAY_BILL,
                    WalletVisuals.BILL_WATER
                )
            ),
            period = AnalyticsPeriod.LAST_7_DAYS,
            nowMillis = NOW,
            zoneId = UTC
        )

        assertEquals(listOf(AnalyticsCategoryKey.WATER, AnalyticsCategoryKey.TRANSFER), result.categories.map { it.key })
        assertEquals(400L, result.categories.first().amountMinor)
        assertEquals(0.8f, result.categories.first().share, 0.001f)
        assertEquals(0.2f, result.categories.last().share, 0.001f)
    }

    @Test
    fun lastThirtyDays_buildsSixFiveDayBuckets() {
        val result = AnalyticsCalculator.calculate(
            transactions = listOf(
                transaction(1L, 250L, timestamp(2026, 8, 17), TransactionType.TRANSFER),
                transaction(2L, 900L, timestamp(2026, 8, 16), TransactionType.TRANSFER)
            ),
            period = AnalyticsPeriod.LAST_30_DAYS,
            nowMillis = NOW,
            zoneId = UTC
        )

        assertEquals(250L, result.totalSpentMinor)
        assertEquals(6, result.trend.size)
        assertEquals(250L, result.trend.sumOf(AnalyticsTrendPoint::amountMinor))
    }

    @Test
    fun allPeriod_buildsContinuousMonthlyTrend() {
        val result = AnalyticsCalculator.calculate(
            transactions = listOf(
                transaction(1L, 100L, timestamp(2026, 7, 3), TransactionType.TRANSFER),
                transaction(2L, 300L, timestamp(2026, 9, 10), TransactionType.TRANSFER)
            ),
            period = AnalyticsPeriod.ALL,
            nowMillis = NOW,
            zoneId = UTC
        )

        assertEquals(listOf("Jul 26", "Aug 26", "Sep 26"), result.trend.map { it.label })
        assertEquals(listOf(100L, 0L, 300L), result.trend.map { it.amountMinor })
    }

    @Test
    fun emptyTransactions_returnSafeZeroState() {
        val result = AnalyticsCalculator.calculate(
            transactions = emptyList(),
            period = AnalyticsPeriod.LAST_7_DAYS,
            nowMillis = NOW,
            zoneId = UTC
        )

        assertFalse(result.hasTransactions)
        assertEquals(0L, result.totalSpentMinor)
        assertEquals(0L, result.averageTransactionMinor)
        assertTrue(result.categories.isEmpty())
    }

    @Test
    fun viewModel_usesCurrentAccountAndReactsToPeriodChanges() = runTest {
        val repository = FakeWalletRepository()
        val session = FakeSessionManager(ACCOUNT_ID)
        repository.transactions(ACCOUNT_ID).value = listOf(
            transaction(1L, 100L, timestamp(2026, 9, 15), TransactionType.TRANSFER),
            transaction(2L, 300L, timestamp(2026, 8, 25), TransactionType.TRANSFER)
        )
        val viewModel = AnalyticsViewModel(repository, session, { NOW }, UTC)
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        assertTrue(viewModel.uiState.value.hasActiveAccount)
        assertEquals(100L, viewModel.uiState.value.totalSpentMinor)

        viewModel.onState(AnalyticsAction.SelectPeriod(AnalyticsPeriod.LAST_30_DAYS))
        advanceUntilIdle()

        assertEquals(400L, viewModel.uiState.value.totalSpentMinor)
        assertEquals(AnalyticsPeriod.LAST_30_DAYS, viewModel.uiState.value.selectedPeriod)
    }

    @Test
    fun viewModel_withoutSession_returnsSessionEmptyState() = runTest {
        val viewModel = AnalyticsViewModel(
            FakeWalletRepository(),
            FakeSessionManager(),
            { NOW },
            UTC
        )
        viewModel.uiState.launchIn(backgroundScope)
        advanceUntilIdle()

        assertFalse(viewModel.uiState.value.hasActiveAccount)
        assertFalse(viewModel.uiState.value.hasTransactions)
    }

    private fun transaction(
        id: Long,
        amountMinor: Long,
        createdAt: Long,
        type: TransactionType,
        billerType: String? = null
    ) = Transaction(
        id = id,
        type = type,
        title = if (type == TransactionType.TRANSFER) "Transfer" else "Biller",
        recipientId = null,
        iconKey = billerType ?: WalletVisuals.AVATAR_ALI,
        billerType = billerType,
        amountMinor = amountMinor,
        createdAt = createdAt
    )

    private fun timestamp(year: Int, month: Int, day: Int): Long = ZonedDateTime.of(
        year,
        month,
        day,
        12,
        0,
        0,
        0,
        UTC
    ).toInstant().toEpochMilli()

    private companion object {
        val UTC: ZoneId = ZoneId.of("UTC")
        val NOW: Long = ZonedDateTime.of(2026, 9, 15, 18, 0, 0, 0, UTC)
            .toInstant()
            .toEpochMilli()
        const val ACCOUNT_ID = 10L
    }
}
