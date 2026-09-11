package com.lmt.global.base.presenter.bill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityPaymentSuccessBinding
import com.lmt.global.base.model.TransactionType
import com.lmt.global.base.presenter.home.HomeActivity
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.walletTransactionNumber

class PaymentSuccessActivity : WalletBaseActivity<ActivityPaymentSuccessBinding>() {
    private val transactionId by lazy { intent.getLongExtra(EXTRA_TRANSACTION_ID, 0L) }
    private val transactionNumber by lazy { walletTransactionNumber(transactionId) }

    override fun provideLayout() = R.layout.activity_payment_success
    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        val type = intent.getStringExtra(EXTRA_TYPE).orEmpty()
        val name = intent.getStringExtra(EXTRA_NAME).orEmpty()
        val amountMinor = intent.getLongExtra(EXTRA_AMOUNT_MINOR, 0L)
        val isTransfer = type == TransactionType.TRANSFER.storageValue

        successTitleText.text = getString(
            if (isTransfer) R.string.transfer_done else R.string.payment_done
        )
        successMessageText.text = getString(
            if (isTransfer) R.string.transfer_completed_message else R.string.bill_completed_message
        )
        counterpartyText.text = getString(
            if (isTransfer) R.string.user_contact_value else R.string.biller_value,
            name
        )
        amountText.text = getString(R.string.amount_value, WalletMoney.format(amountMinor))
        transactionNumberText.text = getString(
            R.string.transaction_number_value,
            transactionNumber
        )
    }

    override fun initListeners() = with(viewBinding) {
        copyButton.setOnClickListener { copyTransactionNumber(transactionNumber) }
        backToWalletButton.setOnClickListener { openRoot(HomeActivity::class.java) }
    }

    companion object {
        private const val EXTRA_TYPE = "transaction_type"
        private const val EXTRA_NAME = "counterparty_name"
        private const val EXTRA_AMOUNT_MINOR = "amount_minor"
        private const val EXTRA_TRANSACTION_ID = "transaction_id"

        fun createIntent(
            context: Context,
            type: TransactionType,
            name: String,
            amountMinor: Long,
            transactionId: Long
        ) = Intent(context, PaymentSuccessActivity::class.java).apply {
            putExtra(EXTRA_TYPE, type.storageValue)
            putExtra(EXTRA_NAME, name)
            putExtra(EXTRA_AMOUNT_MINOR, amountMinor)
            putExtra(EXTRA_TRANSACTION_ID, transactionId)
        }
    }
}
