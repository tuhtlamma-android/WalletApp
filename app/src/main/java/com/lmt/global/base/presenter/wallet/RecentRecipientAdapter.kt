package com.lmt.global.base.presenter.wallet

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lmt.global.base.databinding.ItemWalletRecentRecipientBinding

class RecentRecipientAdapter(
    private val onClick: (WalletContact) -> Unit
) : ListAdapter<WalletContact, RecentRecipientAdapter.Holder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemWalletRecentRecipientBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) = holder.bind(getItem(position))

    inner class Holder(private val binding: ItemWalletRecentRecipientBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(item: WalletContact) = with(binding) {
            avatarImage.setImageResource(WalletVisuals.iconRes(item.avatarKey))
            nameText.text = item.name
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
