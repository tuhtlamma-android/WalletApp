package com.lmt.global.base.presenter.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ItemWalletTransactionBinding

class TransactionAdapter(
    items: List<WalletTransaction>,
    private val onClick: (WalletTransaction) -> Unit = {}
) : RecyclerView.Adapter<TransactionAdapter.Holder>() {

    private val sourceItems = items.toList()
    private val visibleItems = items.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemWalletTransactionBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(visibleItems[position])
    override fun getItemCount() = visibleItems.size

    fun filter(query: String) {
        visibleItems.clear()
        visibleItems += if (query.isBlank()) sourceItems else sourceItems.filter {
            it.merchant.contains(query, ignoreCase = true) || it.date.contains(query, ignoreCase = true)
        }
        notifyDataSetChanged()
    }

    inner class Holder(private val binding: ItemWalletTransactionBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WalletTransaction) = with(binding) {
            sectionText.visibility = if (item.sectionLabel == null) View.GONE else View.VISIBLE
            sectionText.text = item.sectionLabel
            merchantIcon.setImageResource(item.iconRes)
            merchantText.text = item.merchant
            dateText.text = item.date
            amountText.text = item.amount
            amountText.setTextColor(
                ContextCompat.getColor(root.context, if (item.incoming) R.color.wallet_success else R.color.wallet_error)
            )
            transactionRow.setOnClickListener { onClick(item) }
        }
    }
}
