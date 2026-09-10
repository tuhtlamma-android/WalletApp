package com.lmt.global.base.presenter.cards

import android.content.res.ColorStateList
import android.os.Bundle
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.widget.ImageViewCompat
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityCardPaymentBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.WalletViewModel
import com.lmt.global.base.presenter.wallet.maskCardNumber
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CardPaymentActivity : WalletBaseActivity<ActivityCardPaymentBinding>() {
    private val walletViewModel by viewModel<WalletViewModel>()

    override fun provideLayout() = R.layout.activity_card_payment
    override fun initViews(savedInstanceState: Bundle?) {
        val cardId = intent.getStringExtra(EXTRA_CARD_ID)
        if (cardId.isNullOrBlank()) {
            showMissingCardAndFinish()
            return
        }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                walletViewModel.card(cardId).collect { card ->
                    if (card != null) {
                        val contentColor = ContextCompat.getColor(
                            this@CardPaymentActivity,
                            R.color.white
                        )
                        with(viewBinding) {
                            paymentCard.setBackgroundResource(R.drawable.bg_wallet_card)
                            cardHolderText.text = card.name
                            cardHolderText.setTextColor(contentColor)
                            cardNumberText.text = maskCardNumber(card.cardNumber)
                            cardNumberText.setTextColor(contentColor)
                            cardBalanceLabel.setTextColor(contentColor)
                            cardBalanceText.text = WalletMoney.format(card.balanceMinor)
                            cardBalanceText.setTextColor(contentColor)
                            ImageViewCompat.setImageTintList(
                                paymentCardNfc,
                                ColorStateList.valueOf(contentColor)
                            )
                        }
                    }
                }
            }
        }
    }
    override fun initListeners() { viewBinding.backButton.setOnClickListener { finish() } }

    companion object {
        const val EXTRA_CARD_ID = "extra_card_id"
    }

    private fun showMissingCardAndFinish() {
        Toast.makeText(this, R.string.card_not_found, Toast.LENGTH_SHORT).show()
        finish()
    }
}
