package com.lmt.global.base.presenter.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.RecyclerView
import com.lmt.global.base.databinding.ItemWalletContactBinding

class ContactAdapter(
    items: List<WalletContact>,
    private val onClick: (WalletContact) -> Unit
) : RecyclerView.Adapter<ContactAdapter.Holder>() {

    private val sourceItems = items.toList()
    private val visibleItems = items.toMutableList()

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemWalletContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(visibleItems[position])
    override fun getItemCount() = visibleItems.size

    fun filter(query: String) {
        visibleItems.clear()
        visibleItems += if (query.isBlank()) sourceItems else sourceItems.filter {
            it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
        }
        notifyDataSetChanged()
    }

    inner class Holder(private val binding: ItemWalletContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WalletContact) = with(binding) {
            sectionText.visibility = if (item.sectionLabel == null) View.GONE else View.VISIBLE
            sectionText.text = item.sectionLabel
            avatarImage.setImageResource(item.avatarRes)
            nameText.text = item.name
            phoneText.text = item.phone
            root.setOnClickListener { onClick(item) }
        }
    }
}
