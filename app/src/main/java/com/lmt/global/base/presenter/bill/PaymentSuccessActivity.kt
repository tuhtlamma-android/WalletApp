package com.lmt.global.base.presenter.bill

import android.os.Bundle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityPaymentSuccessBinding
import com.lmt.global.base.presenter.home.HomeActivity
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class PaymentSuccessActivity : WalletBaseActivity<ActivityPaymentSuccessBinding>() {
    override fun provideLayout() = R.layout.activity_payment_success
    override fun initViews(savedInstanceState: Bundle?) = Unit
    override fun initListeners() = with(viewBinding) {
        copyButton.setOnClickListener { copyTransactionNumber() }
        backToWalletButton.setOnClickListener { openRoot(HomeActivity::class.java) }
    }
}
