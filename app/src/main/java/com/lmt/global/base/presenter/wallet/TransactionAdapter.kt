package com.lmt.global.base.presenter.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ItemWalletTransactionBinding

class TransactionAdapter(
    private val showSections: Boolean = false,
    private val onClick: (WalletTransaction) -> Unit = {},
    private val onListChanged: (Int) -> Unit = {}
) : ListAdapter<WalletTransaction, TransactionAdapter.Holder>(DiffCallback) {

    private var sourceItems = emptyList<WalletTransaction>()
    private var query = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemWalletTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) =
        holder.bind(getItem(position), position)

    fun submitTransactions(items: List<WalletTransaction>) {
        sourceItems = items
        applyFilter()
    }

    fun filter(query: String) {
        this.query = query
        applyFilter()
    }

    private fun applyFilter() {
        val filtered = if (query.isBlank()) sourceItems else sourceItems.filter {
            it.merchant.contains(query, ignoreCase = true) ||
                walletDateTime(it.createdAt).contains(query, ignoreCase = true)
        }
        val visibleItems = if (showSections) {
            filtered.mapIndexed { index, item ->
                val currentSection = walletSection(item.createdAt)
                val previousSection = filtered.getOrNull(index - 1)?.let { walletSection(it.createdAt) }
                item.copy(sectionLabel = currentSection.takeIf { it != previousSection })
            }
        } else {
            filtered.map { it.copy(sectionLabel = null) }
        }
        submitList(visibleItems) { onListChanged(visibleItems.size) }
    }

    inner class Holder(private val binding: ItemWalletTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WalletTransaction, position: Int) = with(binding) {
            sectionDivider.visibility =
                if (item.sectionLabel != null && position > 0) View.VISIBLE else View.GONE
            sectionText.visibility = if (item.sectionLabel == null) View.GONE else View.VISIBLE
            sectionText.text = item.sectionLabel
            merchantIcon.setImageResource(WalletVisuals.iconRes(item.iconKey))
            merchantText.text = item.merchant
            dateText.text = walletDateTime(item.createdAt)
            amountText.text = "-${WalletMoney.format(item.amountMinor)}"
            amountText.setTextColor(
                ContextCompat.getColor(root.context, R.color.wallet_error)
            )
            transactionRow.setOnClickListener { onClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<WalletTransaction>() {
        override fun areItemsTheSame(oldItem: WalletTransaction, newItem: WalletTransaction) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: WalletTransaction, newItem: WalletTransaction) =
            oldItem == newItem
    }
}
