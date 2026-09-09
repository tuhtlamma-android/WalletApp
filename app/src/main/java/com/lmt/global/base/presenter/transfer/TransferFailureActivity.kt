package com.lmt.global.base.presenter.transfer

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityTransferFailureBinding
import com.lmt.global.base.presenter.home.HomeActivity
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class TransferFailureActivity : WalletBaseActivity<ActivityTransferFailureBinding>() {
    override fun provideLayout() = R.layout.activity_transfer_failure
    override fun initViews(savedInstanceState: Bundle?) {
        applyStatusBarStyle(R.color.wallet_error_soft, true)
        viewBinding.failureTitleText.text = intent.getStringExtra(EXTRA_TITLE)
            ?: getString(R.string.transfer_failed)
        viewBinding.failureMessageText.text = intent.getStringExtra(EXTRA_MESSAGE)
            ?: getString(R.string.invalid_transaction_message)
    }
    override fun initListeners() {
        viewBinding.backToWalletButton.setOnClickListener { openRoot(HomeActivity::class.java) }
    }

    companion object {
        private const val EXTRA_TITLE = "failure_title"
        private const val EXTRA_MESSAGE = "failure_message"

        fun createIntent(context: Context, title: String, message: String) =
            Intent(context, TransferFailureActivity::class.java).apply {
                putExtra(EXTRA_TITLE, title)
                putExtra(EXTRA_MESSAGE, message)
            }
    }
}
