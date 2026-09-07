package com.lmt.global.base.presenter.cards

import android.os.Bundle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityCardPaymentBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class CardPaymentActivity : WalletBaseActivity<ActivityCardPaymentBinding>() {
    override fun provideLayout() = R.layout.activity_card_payment
    override fun initViews(savedInstanceState: Bundle?) = Unit
    override fun initListeners() { viewBinding.backButton.setOnClickListener { finish() } }
}
