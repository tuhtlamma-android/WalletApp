package com.lmt.global.base.presenter.bill

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.GridLayoutManager
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityNewBillerBinding
import com.lmt.global.base.model.TransactionType
import com.lmt.global.base.model.WalletActionResult
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletMoney
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class NewBillerActivity : WalletBaseActivity<ActivityNewBillerBinding>() {
    private val billViewModel by viewModel<BillViewModel>()
    private var selectedBiller: BillerOption? = null

    override fun provideLayout() = R.layout.activity_new_biller

    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        billerList.layoutManager = GridLayoutManager(this@NewBillerActivity, GRID_SPAN_COUNT)
        billerList.adapter = BillerAdapter { selectedBiller = it }
    }

    override fun initObservers() {
        super.initObservers()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    billViewModel.balance.collect { balance ->
                        viewBinding.availableBalanceText.text = getString(
                            R.string.available_balance,
                            WalletMoney.format(balance)
                        )
                    }
                }
                launch {
                    billViewModel.results.collect(::handlePaymentResult)
                }
            }
        }
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        payBillButton.setOnClickListener { submitPayment() }
    }

    private fun submitPayment() = with(viewBinding) {
        amountInput.error = null
        val biller = selectedBiller
        if (biller == null) {
            Toast.makeText(this@NewBillerActivity, R.string.select_biller_error, Toast.LENGTH_SHORT).show()
            return@with
        }
        val rawAmount = amountInput.text.toString()
        if (rawAmount.isBlank()) {
            amountInput.error = getString(R.string.enter_amount_error)
            return@with
        }
        val amountMinor = WalletMoney.parseToMinor(rawAmount)
        if (amountMinor == null) {
            amountInput.error = getString(R.string.enter_valid_amount_error)
            return@with
        }

        payBillButton.isEnabled = false
        billViewModel.onState(
            BillAction.Submit(getString(biller.nameRes), biller.type, amountMinor)
        )
    }

    private fun handlePaymentResult(event: BillPaymentResult) = with(viewBinding) {
        when (val result = event.result) {
            is WalletActionResult.Success -> {
                startActivity(
                    PaymentSuccessActivity.createIntent(
                        context = this@NewBillerActivity,
                        type = TransactionType.PAY_BILL,
                        name = event.billerName,
                        amountMinor = event.amountMinor,
                        transactionId = result.transactionId ?: 0L
                    )
                )
                finish()
            }
            WalletActionResult.InsufficientBalance -> {
                amountInput.error = getString(R.string.insufficient_balance)
                payBillButton.isEnabled = true
            }
            else -> {
                amountInput.error = getString(R.string.enter_valid_amount_error)
                payBillButton.isEnabled = true
            }
        }
    }

    private companion object {
        const val GRID_SPAN_COUNT = 2
    }
}
