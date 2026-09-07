package com.lmt.global.base.presenter.transfer

import android.os.Bundle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityTransferFailureBinding
import com.lmt.global.base.presenter.home.HomeActivity
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class TransferFailureActivity : WalletBaseActivity<ActivityTransferFailureBinding>() {
    override fun provideLayout() = R.layout.activity_transfer_failure
    override fun initViews(savedInstanceState: Bundle?) { applyStatusBarStyle(R.color.wallet_error_soft, true) }
    override fun initListeners() {
        viewBinding.backToWalletButton.setOnClickListener { openRoot(HomeActivity::class.java) }
    }
}
