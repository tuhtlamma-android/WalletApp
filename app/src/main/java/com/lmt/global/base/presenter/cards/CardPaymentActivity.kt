package com.lmt.global.base.presenter.cards

import android.os.Bundle
import android.content.res.ColorStateList
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityCardPaymentBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class CardPaymentActivity : WalletBaseActivity<ActivityCardPaymentBinding>() {
    override fun provideLayout() = R.layout.activity_card_payment
    override fun initViews(savedInstanceState: Bundle?) {
        val selectedIndex = intent.getIntExtra(EXTRA_CARD_INDEX, DEFAULT_CARD_INDEX)
        val selectedCard = WalletCards.all.getOrElse(selectedIndex) {
            WalletCards.all[DEFAULT_CARD_INDEX]
        }
        val contentColor = ContextCompat.getColor(this, selectedCard.contentColorRes)

        with(viewBinding) {
            paymentCard.setBackgroundResource(selectedCard.backgroundRes)
            cardHolderText.setTextColor(contentColor)
            cardNumberText.text = selectedCard.number
            cardNumberText.setTextColor(contentColor)
            cardBalanceLabel.setTextColor(contentColor)
            cardBalanceText.text = selectedCard.balance
            cardBalanceText.setTextColor(contentColor)
            ImageViewCompat.setImageTintList(
                paymentCardNfc,
                ColorStateList.valueOf(contentColor)
            )
        }
    }
    override fun initListeners() { viewBinding.backButton.setOnClickListener { finish() } }

    companion object {
        const val EXTRA_CARD_INDEX = "extra_card_index"
        private const val DEFAULT_CARD_INDEX = 2
    }
}
