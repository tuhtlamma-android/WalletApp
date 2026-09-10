package com.lmt.global.base.presenter.bill

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.annotation.StringRes
import androidx.recyclerview.widget.RecyclerView
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ItemBillerTypeBinding
import com.lmt.global.base.presenter.wallet.WalletVisuals

data class BillerOption(
    val type: String,
    @param:StringRes val nameRes: Int
)

class BillerAdapter(
    private val onSelected: (BillerOption) -> Unit
) : RecyclerView.Adapter<BillerAdapter.Holder>() {
    private var selectedType: String? = null

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemBillerTypeBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun getItemCount() = ITEMS.size

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(ITEMS[position])
    }

    inner class Holder(private val binding: ItemBillerTypeBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: BillerOption) = with(binding) {
            billerContainer.isSelected = item.type == selectedType
            billerIcon.setImageResource(WalletVisuals.iconRes(item.type))
            billerName.setText(item.nameRes)
            billerContainer.setOnClickListener {
                val previousIndex = ITEMS.indexOfFirst { it.type == selectedType }
                selectedType = item.type
                if (previousIndex >= 0) notifyItemChanged(previousIndex)
                val currentIndex = bindingAdapterPosition
                if (currentIndex != RecyclerView.NO_POSITION) notifyItemChanged(currentIndex)
                onSelected(item)
            }
        }
    }

    companion object {
        val ITEMS = listOf(
            BillerOption(WalletVisuals.BILL_ELECTRICITY, R.string.electricity),
            BillerOption(WalletVisuals.BILL_WATER, R.string.water),
            BillerOption(WalletVisuals.BILL_PHONE, R.string.mobile_phone),
            BillerOption(WalletVisuals.BILL_INTERNET, R.string.internet),
            BillerOption(WalletVisuals.BILL_TELEVISION, R.string.television),
            BillerOption(WalletVisuals.BILL_GAS, R.string.gas),
            BillerOption(WalletVisuals.BILL_INSURANCE, R.string.insurance),
            BillerOption(WalletVisuals.BILL_EDUCATION, R.string.education),
            BillerOption(WalletVisuals.BILL_RENT, R.string.rent),
            BillerOption(WalletVisuals.BILL_OTHER, R.string.other)
        )
    }
}
