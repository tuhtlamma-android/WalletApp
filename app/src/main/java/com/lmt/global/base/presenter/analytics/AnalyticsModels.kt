package com.lmt.global.base.presenter.analytics

import com.lmt.global.base.model.Transaction
import com.lmt.global.base.model.TransactionType
import com.lmt.global.base.presenter.wallet.WalletVisuals
import java.util.Locale
import org.threeten.bp.Instant
import org.threeten.bp.LocalDate
import org.threeten.bp.YearMonth
import org.threeten.bp.ZoneId
import org.threeten.bp.format.DateTimeFormatter

enum class AnalyticsPeriod {
    LAST_7_DAYS,
    LAST_30_DAYS,
    ALL
}

enum class AnalyticsCategoryKey {
    TRANSFER,
    ELECTRICITY,
    WATER,
    MOBILE_PHONE,
    INTERNET,
    TELEVISION,
    GAS,
    INSURANCE,
    EDUCATION,
    RENT,
    OTHER
}

data class AnalyticsTrendPoint(
    val label: String,
    val amountMinor: Long
)

data class AnalyticsCategory(
    val key: AnalyticsCategoryKey,
    val amountMinor: Long,
    val share: Float
)

data class AnalyticsUiState(
    val selectedPeriod: AnalyticsPeriod = AnalyticsPeriod.LAST_7_DAYS,
    val totalSpentMinor: Long = 0L,
    val transactionCount: Int = 0,
    val averageTransactionMinor: Long = 0L,
    val trend: List<AnalyticsTrendPoint> = emptyList(),
    val categories: List<AnalyticsCategory> = emptyList(),
    val hasActiveAccount: Boolean = false
) {
    val hasTransactions: Boolean get() = transactionCount > 0
}

object AnalyticsCalculator {
    private const val DAYS_IN_WEEK = 7L
    private const val DAYS_IN_MONTH_VIEW = 30L
    private const val FIVE_DAY_BUCKETS = 6
    private const val DAYS_PER_BUCKET = 5L

    private val dayLabelFormatter = DateTimeFormatter.ofPattern("EEE", Locale.US)
    private val rangeLabelFormatter = DateTimeFormatter.ofPattern("M/d", Locale.US)
    private val monthLabelFormatter = DateTimeFormatter.ofPattern("MMM yy", Locale.US)

    fun calculate(
        transactions: List<Transaction>,
        period: AnalyticsPeriod,
        nowMillis: Long,
        zoneId: ZoneId,
        hasActiveAccount: Boolean = true
    ): AnalyticsUiState {
        val today = localDate(nowMillis, zoneId)
        val visibleTransactions = transactions
            .asSequence()
            .filter { it.amountMinor > 0L && it.createdAt <= nowMillis }
            .filter { transaction ->
                val startDate = period.startDate(today)
                startDate == null || !localDate(transaction.createdAt, zoneId).isBefore(startDate)
            }
            .toList()

        val totalSpent = visibleTransactions.sumOf(Transaction::amountMinor)
        val categories = visibleTransactions
            .groupBy(::categoryFor)
            .map { (key, items) ->
                val amount = items.sumOf(Transaction::amountMinor)
                AnalyticsCategory(
                    key = key,
                    amountMinor = amount,
                    share = if (totalSpent == 0L) 0f else amount.toFloat() / totalSpent.toFloat()
                )
            }
            .sortedWith(compareByDescending<AnalyticsCategory> { it.amountMinor }.thenBy { it.key.ordinal })

        return AnalyticsUiState(
            selectedPeriod = period,
            totalSpentMinor = totalSpent,
            transactionCount = visibleTransactions.size,
            averageTransactionMinor = if (visibleTransactions.isEmpty()) {
                0L
            } else {
                totalSpent / visibleTransactions.size
            },
            trend = buildTrend(visibleTransactions, period, today, zoneId),
            categories = categories,
            hasActiveAccount = hasActiveAccount
        )
    }

    private fun buildTrend(
        transactions: List<Transaction>,
        period: AnalyticsPeriod,
        today: LocalDate,
        zoneId: ZoneId
    ): List<AnalyticsTrendPoint> = when (period) {
        AnalyticsPeriod.LAST_7_DAYS -> {
            val start = today.minusDays(DAYS_IN_WEEK - 1L)
            (0 until DAYS_IN_WEEK.toInt()).map { offset ->
                val date = start.plusDays(offset.toLong())
                AnalyticsTrendPoint(
                    label = date.format(dayLabelFormatter),
                    amountMinor = amountOnDate(transactions, date, zoneId)
                )
            }
        }

        AnalyticsPeriod.LAST_30_DAYS -> {
            val start = today.minusDays(DAYS_IN_MONTH_VIEW - 1L)
            (0 until FIVE_DAY_BUCKETS).map { index ->
                val bucketStart = start.plusDays(index * DAYS_PER_BUCKET)
                val bucketEnd = bucketStart.plusDays(DAYS_PER_BUCKET - 1L)
                AnalyticsTrendPoint(
                    label = "${bucketStart.format(rangeLabelFormatter)}–${bucketEnd.format(rangeLabelFormatter)}",
                    amountMinor = transactions.sumOf { transaction ->
                        val date = localDate(transaction.createdAt, zoneId)
                        transaction.amountMinor.takeIf {
                            !date.isBefore(bucketStart) && !date.isAfter(bucketEnd)
                        } ?: 0L
                    }
                )
            }
        }

        AnalyticsPeriod.ALL -> buildMonthlyTrend(transactions, today, zoneId)
    }

    private fun buildMonthlyTrend(
        transactions: List<Transaction>,
        today: LocalDate,
        zoneId: ZoneId
    ): List<AnalyticsTrendPoint> {
        if (transactions.isEmpty()) return emptyList()
        val amountsByMonth = transactions.groupBy { transaction ->
            YearMonth.from(localDate(transaction.createdAt, zoneId))
        }.mapValues { (_, items) -> items.sumOf(Transaction::amountMinor) }

        val firstMonth = amountsByMonth.keys.minOrNull() ?: return emptyList()
        val currentMonth = YearMonth.from(today)
        val result = mutableListOf<AnalyticsTrendPoint>()
        var month = firstMonth
        while (!month.isAfter(currentMonth)) {
            result += AnalyticsTrendPoint(
                label = month.format(monthLabelFormatter),
                amountMinor = amountsByMonth[month] ?: 0L
            )
            month = month.plusMonths(1L)
        }
        return result
    }

    private fun amountOnDate(
        transactions: List<Transaction>,
        date: LocalDate,
        zoneId: ZoneId
    ): Long = transactions.sumOf { transaction ->
        transaction.amountMinor.takeIf {
            localDate(transaction.createdAt, zoneId) == date
        } ?: 0L
    }

    private fun AnalyticsPeriod.startDate(today: LocalDate): LocalDate? = when (this) {
        AnalyticsPeriod.LAST_7_DAYS -> today.minusDays(DAYS_IN_WEEK - 1L)
        AnalyticsPeriod.LAST_30_DAYS -> today.minusDays(DAYS_IN_MONTH_VIEW - 1L)
        AnalyticsPeriod.ALL -> null
    }

    private fun localDate(timestamp: Long, zoneId: ZoneId): LocalDate =
        Instant.ofEpochMilli(timestamp).atZone(zoneId).toLocalDate()

    private fun categoryFor(transaction: Transaction): AnalyticsCategoryKey {
        if (transaction.type == TransactionType.TRANSFER) return AnalyticsCategoryKey.TRANSFER
        return when (transaction.billerType ?: transaction.iconKey) {
            WalletVisuals.BILL_ELECTRICITY -> AnalyticsCategoryKey.ELECTRICITY
            WalletVisuals.BILL_WATER -> AnalyticsCategoryKey.WATER
            WalletVisuals.BILL_PHONE -> AnalyticsCategoryKey.MOBILE_PHONE
            WalletVisuals.BILL_INTERNET -> AnalyticsCategoryKey.INTERNET
            WalletVisuals.BILL_TELEVISION -> AnalyticsCategoryKey.TELEVISION
            WalletVisuals.BILL_GAS -> AnalyticsCategoryKey.GAS
            WalletVisuals.BILL_INSURANCE -> AnalyticsCategoryKey.INSURANCE
            WalletVisuals.BILL_EDUCATION -> AnalyticsCategoryKey.EDUCATION
            WalletVisuals.BILL_RENT -> AnalyticsCategoryKey.RENT
            else -> AnalyticsCategoryKey.OTHER
        }
    }
}
