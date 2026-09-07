package com.lmt.global.base.presenter.transfer

import android.content.Intent
import android.os.Bundle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityTransferConfirmBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import java.text.NumberFormat
import java.util.Locale

class TransferConfirmActivity : WalletBaseActivity<ActivityTransferConfirmBinding>() {
    override fun provideLayout() = R.layout.activity_transfer_confirm

    override fun initViews(savedInstanceState: Bundle?) {
        val amount = intent.getDoubleExtra(EXTRA_AMOUNT, 0.0)
        viewBinding.amountText.text = NumberFormat.getCurrencyInstance(Locale.US).format(amount)
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        securePaymentButton.setOnClickListener {
            startActivity(Intent(this@TransferConfirmActivity, TransferFailureActivity::class.java))
        }
    }

    companion object { const val EXTRA_AMOUNT = "transfer_amount" }
}
