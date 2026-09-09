package com.lmt.global.base.presenter.transfer

import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.lmt.global.base.R
import com.lmt.global.base.data.WalletActionResult
import com.lmt.global.base.data.entity.TransactionEntity
import com.lmt.global.base.databinding.ActivityTransferConfirmBinding
import com.lmt.global.base.presenter.bill.PaymentSuccessActivity
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.WalletViewModel
import com.lmt.global.base.presenter.wallet.WalletVisuals
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransferConfirmActivity : WalletBaseActivity<ActivityTransferConfirmBinding>() {
    private val walletViewModel by viewModel<WalletViewModel>()
    private val amountMinor by lazy { intent.getLongExtra(EXTRA_AMOUNT_MINOR, 0L) }
    private val recipientName by lazy {
        intent.getStringExtra(TransferAmountActivity.EXTRA_RECIPIENT_NAME).orEmpty()
    }
    private val avatarKey by lazy {
        intent.getStringExtra(TransferAmountActivity.EXTRA_AVATAR_KEY) ?: WalletVisuals.AVATAR_ALI
    }

    override fun provideLayout() = R.layout.activity_transfer_confirm

    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        amountText.text = WalletMoney.format(amountMinor)
        recipientNameText.text = recipientName
        recipientAvatar.setImageResource(WalletVisuals.iconRes(avatarKey))
        recipientSubtitle.visibility = android.view.View.GONE
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        securePaymentButton.setOnClickListener {
            securePaymentButton.isEnabled = false
            lifecycleScope.launch {
                val result = walletViewModel.transfer(recipientName, avatarKey, amountMinor)
                when (result) {
                    is WalletActionResult.Success -> openSuccess(result)
                    WalletActionResult.InsufficientBalance -> {
                        openFailure(
                            getString(R.string.transfer_failed),
                            getString(R.string.insufficient_balance_message)
                        )
                    }
                    else -> openFailure(
                        getString(R.string.transfer_failed),
                        getString(R.string.invalid_transaction_message)
                    )
                }
            }
        }
    }

    private fun openSuccess(result: WalletActionResult.Success) {
        startActivity(
            PaymentSuccessActivity.createIntent(
                context = this,
                type = TransactionEntity.TYPE_TRANSFER,
                name = recipientName,
                amountMinor = amountMinor,
                transactionId = result.transactionId ?: 0L
            )
        )
        finish()
    }

    private fun openFailure(title: String, message: String) {
        startActivity(TransferFailureActivity.createIntent(this, title, message))
        finish()
    }

    companion object { const val EXTRA_AMOUNT_MINOR = "transfer_amount_minor" }
}
