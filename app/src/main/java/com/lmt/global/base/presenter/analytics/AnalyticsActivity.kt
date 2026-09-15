package com.lmt.global.base.presenter.analytics

import android.os.Bundle
import androidx.core.view.isVisible
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lmt.global.base.R
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityAnalyticsBinding
import com.lmt.global.base.presenter.wallet.WalletMoney
import java.text.NumberFormat
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AnalyticsActivity : IActivity<ActivityAnalyticsBinding, AnalyticsViewModel>() {

    private val categoryAdapter = AnalyticsCategoryAdapter()

    override fun provideViewModel() = viewModel<AnalyticsViewModel>()
    override fun provideLayout() = R.layout.activity_analytics

    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        categoryList.layoutManager = LinearLayoutManager(this@AnalyticsActivity)
        categoryList.adapter = categoryAdapter
        categoryList.isNestedScrollingEnabled = false
    }

    override fun initObservers() {
        super.initObservers()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect(::render)
            }
        }
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        period7DaysButton.setOnClickListener { selectPeriod(AnalyticsPeriod.LAST_7_DAYS) }
        period30DaysButton.setOnClickListener { selectPeriod(AnalyticsPeriod.LAST_30_DAYS) }
        periodAllButton.setOnClickListener { selectPeriod(AnalyticsPeriod.ALL) }
    }

    private fun selectPeriod(period: AnalyticsPeriod) {
        viewModel.onState(AnalyticsAction.SelectPeriod(period))
    }

    private fun render(state: AnalyticsUiState) = with(viewBinding) {
        period7DaysButton.isSelected = state.selectedPeriod == AnalyticsPeriod.LAST_7_DAYS
        period30DaysButton.isSelected = state.selectedPeriod == AnalyticsPeriod.LAST_30_DAYS
        periodAllButton.isSelected = state.selectedPeriod == AnalyticsPeriod.ALL

        emptyState.isVisible = !state.hasTransactions
        analyticsContent.isVisible = state.hasTransactions
        if (!state.hasTransactions) {
            emptyState.text = getString(
                if (state.hasActiveAccount) {
                    R.string.analytics_empty_state
                } else {
                    R.string.analytics_session_empty_state
                }
            )
            return@with
        }

        totalSpentText.text = WalletMoney.format(state.totalSpentMinor)
        transactionCountText.text = NumberFormat.getIntegerInstance().format(state.transactionCount)
        averageTransactionText.text = WalletMoney.format(state.averageTransactionMinor)
        periodSummaryText.text = getString(state.selectedPeriod.summaryRes())
        trendPeriodText.text = getString(state.selectedPeriod.trendRes())
        spendingChart.setPoints(state.trend)
        categoryAdapter.submitList(state.categories)
        categoryCountText.text = resources.getQuantityString(
            R.plurals.analytics_category_count,
            state.categories.size,
            state.categories.size
        )
    }
}

private fun AnalyticsPeriod.summaryRes(): Int = when (this) {
    AnalyticsPeriod.LAST_7_DAYS -> R.string.analytics_summary_7_days
    AnalyticsPeriod.LAST_30_DAYS -> R.string.analytics_summary_30_days
    AnalyticsPeriod.ALL -> R.string.analytics_summary_all
}

private fun AnalyticsPeriod.trendRes(): Int = when (this) {
    AnalyticsPeriod.LAST_7_DAYS -> R.string.analytics_trend_daily
    AnalyticsPeriod.LAST_30_DAYS -> R.string.analytics_trend_five_day
    AnalyticsPeriod.ALL -> R.string.analytics_trend_monthly
}
