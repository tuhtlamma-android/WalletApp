package com.lmt.global.base.presenter.wallet

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lmt.global.base.databinding.ItemWalletContactBinding

class ContactAdapter(
    private val onClick: (WalletContact) -> Unit,
    private val onListChanged: (Int) -> Unit = {}
) : ListAdapter<WalletContact, ContactAdapter.Holder>(DiffCallback) {

    private var sourceItems = emptyList<WalletContact>()
    private var query = ""

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemWalletContactBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    fun submitContacts(items: List<WalletContact>) {
        sourceItems = items
        applyFilter()
    }

    fun filter(query: String) {
        this.query = query
        applyFilter()
    }

    private fun applyFilter() {
        val visibleItems = if (query.isBlank()) sourceItems else sourceItems.filter {
            it.name.contains(query, ignoreCase = true) || it.phone.contains(query)
        }
        submitList(visibleItems) { onListChanged(visibleItems.size) }
    }

    inner class Holder(private val binding: ItemWalletContactBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WalletContact) = with(binding) {
            sectionText.visibility = if (item.sectionLabel == null) View.GONE else View.VISIBLE
            sectionText.text = item.sectionLabel
            avatarImage.setImageResource(WalletVisuals.iconRes(item.avatarKey))
            nameText.text = item.name
            phoneText.text = item.phone
            phoneText.visibility = if (item.phone.isBlank()) View.GONE else View.VISIBLE
            root.setOnClickListener { onClick(item) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<WalletContact>() {
        override fun areItemsTheSame(oldItem: WalletContact, newItem: WalletContact) =
            oldItem.id == newItem.id

        override fun areContentsTheSame(oldItem: WalletContact, newItem: WalletContact) =
            oldItem == newItem
    }
}
