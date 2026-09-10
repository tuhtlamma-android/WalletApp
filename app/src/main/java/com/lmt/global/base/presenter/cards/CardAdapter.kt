package com.lmt.global.base.presenter.cards

import android.content.res.ColorStateList
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.lmt.global.base.R
import com.lmt.global.base.data.entity.CardEntity
import com.lmt.global.base.databinding.ItemWalletCardBinding
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.maskCardNumber

class CardAdapter(
    private val onClick: (CardEntity) -> Unit
) : ListAdapter<CardEntity, CardAdapter.Holder>(DiffCallback) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int) = Holder(
        ItemWalletCardBinding.inflate(LayoutInflater.from(parent.context), parent, false)
    )

    override fun onBindViewHolder(holder: Holder, position: Int) {
        holder.bind(getItem(position), position)
    }

    inner class Holder(private val binding: ItemWalletCardBinding) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(card: CardEntity, position: Int) = with(binding) {
            val lightCard = position % CARD_STYLE_COUNT == 0
            cardContainer.setBackgroundResource(
                when (position % CARD_STYLE_COUNT) {
                    0 -> R.drawable.bg_wallet_card_light
                    1 -> R.drawable.bg_wallet_card_mid
                    else -> R.drawable.bg_wallet_card
                }
            )
            val contentColor = ContextCompat.getColor(
                root.context,
                if (lightCard) R.color.wallet_black else R.color.white
            )
            cardHolderText.text = card.name
            cardNumberText.text = maskCardNumber(card.cardNumber)
            cardBalanceText.text = WalletMoney.format(card.balanceMinor)
            listOf(cardHolderText, cardNumberText, cardBalanceLabel, cardBalanceText)
                .forEach { it.setTextColor(contentColor) }
            ImageViewCompat.setImageTintList(cardNfcIcon, ColorStateList.valueOf(contentColor))
            cardContainer.setOnClickListener { onClick(card) }
        }
    }

    private object DiffCallback : DiffUtil.ItemCallback<CardEntity>() {
        override fun areItemsTheSame(oldItem: CardEntity, newItem: CardEntity) = oldItem.id == newItem.id
        override fun areContentsTheSame(oldItem: CardEntity, newItem: CardEntity) = oldItem == newItem
    }

    private companion object {
        const val CARD_STYLE_COUNT = 3
    }
}
