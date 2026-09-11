package com.lmt.global.base.presenter.bill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityBillConfirmBinding
import com.lmt.global.base.model.TransactionType
import com.lmt.global.base.model.WalletActionResult
import com.lmt.global.base.presenter.transfer.TransferFailureActivity
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.WalletVisuals
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class BillConfirmActivity : WalletBaseActivity<ActivityBillConfirmBinding>() {
    private val billViewModel by viewModel<BillViewModel>()
    private val billerName by lazy { intent.getStringExtra(EXTRA_BILLER_NAME).orEmpty() }
    private val iconKey by lazy {
        intent.getStringExtra(EXTRA_ICON_KEY) ?: WalletVisuals.BILL_OTHER
    }
    private val amountMinor by lazy { intent.getLongExtra(EXTRA_AMOUNT_MINOR, 0L) }

    override fun provideLayout() = R.layout.activity_bill_confirm

    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        amountText.text = WalletMoney.format(amountMinor)
        billerNameText.text = billerName
        billerIcon.setImageResource(WalletVisuals.iconRes(iconKey))
    }

    override fun initObservers() {
        super.initObservers()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                billViewModel.results.collect(::handlePaymentResult)
            }
        }
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        securePaymentButton.setOnClickListener {
            securePaymentButton.isEnabled = false
            billViewModel.onState(BillAction.Submit(billerName, iconKey, amountMinor))
        }
    }

    private fun handlePaymentResult(event: BillPaymentResult) {
        when (val result = event.result) {
            is WalletActionResult.Success -> openSuccess(result)
            WalletActionResult.InsufficientBalance -> openFailure(
                getString(R.string.payment_failed),
                getString(R.string.insufficient_balance_message)
            )
            else -> openFailure(
                getString(R.string.payment_failed),
                getString(R.string.invalid_transaction_message)
            )
        }
    }

    private fun openSuccess(result: WalletActionResult.Success) {
        startActivity(
            PaymentSuccessActivity.createIntent(
                context = this,
                type = TransactionType.PAY_BILL,
                name = billerName,
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

    companion object {
        private const val EXTRA_BILLER_NAME = "biller_name"
        private const val EXTRA_ICON_KEY = "biller_icon_key"
        private const val EXTRA_AMOUNT_MINOR = "bill_amount_minor"

        fun createIntent(
            context: Context,
            billerName: String,
            iconKey: String,
            amountMinor: Long
        ) = Intent(context, BillConfirmActivity::class.java).apply {
            putExtra(EXTRA_BILLER_NAME, billerName)
            putExtra(EXTRA_ICON_KEY, iconKey)
            putExtra(EXTRA_AMOUNT_MINOR, amountMinor)
        }
    }
}
