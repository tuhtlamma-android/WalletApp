package com.lmt.global.base.presenter.analytics

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ItemAnalyticsCategoryBinding
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.WalletVisuals
import kotlin.math.roundToInt

class AnalyticsCategoryAdapter :
    ListAdapter<AnalyticsCategory, AnalyticsCategoryAdapter.Holder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemAnalyticsCategoryBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position))
    }

    class Holder(
        private val binding: ItemAnalyticsCategoryBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: AnalyticsCategory) = with(binding) {
            categoryIcon.setImageResource(item.key.iconRes())
            categoryName.setText(item.key.labelRes())
            categoryAmount.text = WalletMoney.format(item.amountMinor)
            val percentage = (item.share * PERCENT_MAX).roundToInt().coerceIn(0, PERCENT_MAX)
            categoryPercentage.text = root.context.getString(
                R.string.analytics_percentage,
                percentage
            )
            categoryProgress.progress = percentage
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<AnalyticsCategory>() {
        override fun areItemsTheSame(oldItem: AnalyticsCategory, newItem: AnalyticsCategory) =
            oldItem.key == newItem.key

        override fun areContentsTheSame(oldItem: AnalyticsCategory, newItem: AnalyticsCategory) =
            oldItem == newItem
    }

    private companion object {
        const val PERCENT_MAX = 100
    }
}

@StringRes
private fun AnalyticsCategoryKey.labelRes(): Int = when (this) {
    AnalyticsCategoryKey.TRANSFER -> R.string.transfer
    AnalyticsCategoryKey.ELECTRICITY -> R.string.electricity
    AnalyticsCategoryKey.WATER -> R.string.water
    AnalyticsCategoryKey.MOBILE_PHONE -> R.string.mobile_phone
    AnalyticsCategoryKey.INTERNET -> R.string.internet
    AnalyticsCategoryKey.TELEVISION -> R.string.television
    AnalyticsCategoryKey.GAS -> R.string.gas
    AnalyticsCategoryKey.INSURANCE -> R.string.insurance
    AnalyticsCategoryKey.EDUCATION -> R.string.education
    AnalyticsCategoryKey.RENT -> R.string.rent
    AnalyticsCategoryKey.OTHER -> R.string.other
}

@DrawableRes
private fun AnalyticsCategoryKey.iconRes(): Int = when (this) {
    AnalyticsCategoryKey.TRANSFER -> R.drawable.ic_more_transfer
    AnalyticsCategoryKey.ELECTRICITY -> WalletVisuals.iconRes(WalletVisuals.BILL_ELECTRICITY)
    AnalyticsCategoryKey.WATER -> WalletVisuals.iconRes(WalletVisuals.BILL_WATER)
    AnalyticsCategoryKey.MOBILE_PHONE -> WalletVisuals.iconRes(WalletVisuals.BILL_PHONE)
    AnalyticsCategoryKey.INTERNET -> WalletVisuals.iconRes(WalletVisuals.BILL_INTERNET)
    AnalyticsCategoryKey.TELEVISION -> WalletVisuals.iconRes(WalletVisuals.BILL_TELEVISION)
    AnalyticsCategoryKey.GAS -> WalletVisuals.iconRes(WalletVisuals.BILL_GAS)
    AnalyticsCategoryKey.INSURANCE -> WalletVisuals.iconRes(WalletVisuals.BILL_INSURANCE)
    AnalyticsCategoryKey.EDUCATION -> WalletVisuals.iconRes(WalletVisuals.BILL_EDUCATION)
    AnalyticsCategoryKey.RENT -> WalletVisuals.iconRes(WalletVisuals.BILL_RENT)
    AnalyticsCategoryKey.OTHER -> WalletVisuals.iconRes(WalletVisuals.BILL_OTHER)
}
