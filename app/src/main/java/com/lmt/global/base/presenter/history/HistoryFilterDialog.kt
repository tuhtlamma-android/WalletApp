package com.lmt.global.base.presenter.history

import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.annotation.DrawableRes
import androidx.annotation.StringRes
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lmt.global.base.R
import com.lmt.global.base.databinding.DialogHistoryFilterBinding
import com.lmt.global.base.databinding.ItemHistoryFilterBinding
import com.lmt.global.base.presenter.wallet.HistoryTransactionFilter

fun showHistoryFilterDialog(
    context: Context,
    currentFilters: Set<HistoryTransactionFilter>,
    onApply: (Set<HistoryTransactionFilter>) -> Unit
) {
    val binding = DialogHistoryFilterBinding.inflate(LayoutInflater.from(context))
    val filterAdapter = HistoryFilterAdapter(currentFilters)
    val dialog = MaterialAlertDialogBuilder(context)
        .setView(binding.root)
        .create()

    binding.filterList.apply {
        layoutManager = GridLayoutManager(context, FILTER_COLUMN_COUNT)
        adapter = filterAdapter
    }
    binding.clearFiltersButton.setOnClickListener { filterAdapter.clearSelection() }
    binding.cancelButton.setOnClickListener { dialog.dismiss() }
    binding.applyButton.setOnClickListener {
        onApply(filterAdapter.selectedFilters())
        dialog.dismiss()
    }
    dialog.show()
}

private data class HistoryFilterOption(
    val filter: HistoryTransactionFilter,
    @param:StringRes val labelRes: Int,
    @param:DrawableRes val iconRes: Int
)

private class HistoryFilterAdapter(
    selectedFilters: Set<HistoryTransactionFilter>
) : RecyclerView.Adapter<HistoryFilterAdapter.Holder>() {
    private val selection = selectedFilters.toMutableSet()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemHistoryFilterBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = OPTIONS.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(OPTIONS[position])
    }

    fun selectedFilters(): Set<HistoryTransactionFilter> = selection.toSet()

    fun clearSelection() {
        if (selection.isEmpty()) return
        selection.clear()
        notifyItemRangeChanged(0, itemCount)
    }

    private fun toggle(filter: HistoryTransactionFilter) {
        if (filter == HistoryTransactionFilter.ALL_BILLERS) {
            selection.removeAll(BILLER_FILTERS)
        } else if (filter in BILLER_FILTERS) {
            selection.remove(HistoryTransactionFilter.ALL_BILLERS)
        }

        if (!selection.remove(filter)) selection.add(filter)
        notifyItemRangeChanged(0, itemCount)
    }

    inner class Holder(
        private val binding: ItemHistoryFilterBinding
    ) : RecyclerView.ViewHolder(binding.root) {
        fun bind(item: HistoryFilterOption) = with(binding) {
            val isSelected = item.filter in selection
            filterContainer.isSelected = isSelected
            filterContainer.contentDescription = root.context.getString(item.labelRes)
            filterIcon.setImageResource(item.iconRes)
            filterName.setText(item.labelRes)
            selectedIndicator.visibility = if (isSelected) View.VISIBLE else View.GONE
            filterContainer.setOnClickListener { toggle(item.filter) }
        }
    }

    private companion object {
        val BILLER_FILTERS = setOf(
            HistoryTransactionFilter.ELECTRICITY,
            HistoryTransactionFilter.WATER,
            HistoryTransactionFilter.MOBILE_PHONE,
            HistoryTransactionFilter.INTERNET,
            HistoryTransactionFilter.TELEVISION,
            HistoryTransactionFilter.GAS,
            HistoryTransactionFilter.INSURANCE,
            HistoryTransactionFilter.EDUCATION,
            HistoryTransactionFilter.RENT,
            HistoryTransactionFilter.OTHER
        )

        val OPTIONS = listOf(
            HistoryFilterOption(
                HistoryTransactionFilter.TRANSFER,
                R.string.transfer,
                R.drawable.ic_more_transfer
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.ALL_BILLERS,
                R.string.all_billers,
                R.drawable.ic_more_pay_bills
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.ELECTRICITY,
                R.string.electricity,
                R.drawable.ic_biller_electricity
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.WATER,
                R.string.water,
                R.drawable.ic_biller_water
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.MOBILE_PHONE,
                R.string.mobile_phone,
                R.drawable.ic_biller_phone
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.INTERNET,
                R.string.internet,
                R.drawable.ic_biller_internet
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.TELEVISION,
                R.string.television,
                R.drawable.ic_biller_television
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.GAS,
                R.string.gas,
                R.drawable.ic_biller_gas
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.INSURANCE,
                R.string.insurance,
                R.drawable.ic_biller_insurance
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.EDUCATION,
                R.string.education,
                R.drawable.ic_biller_education
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.RENT,
                R.string.rent,
                R.drawable.ic_biller_rent
            ),
            HistoryFilterOption(
                HistoryTransactionFilter.OTHER,
                R.string.other,
                R.drawable.ic_biller_other
            )
        )
    }
}

private const val FILTER_COLUMN_COUNT = 2
