package com.lmt.global.base.presenter.transfer

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityTransferAmountBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class TransferAmountActivity : WalletBaseActivity<ActivityTransferAmountBinding>() {
    override fun provideLayout() = R.layout.activity_transfer_amount
    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding.amountInput.showSoftInputOnFocus = false
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        listOf(key0, key1, key2, key3, key4, key5, key6, key7, key8, key9).forEach { key ->
            key.setOnClickListener { appendValue((it as TextView).text.toString()) }
        }
        decimalKey.setOnClickListener { appendValue(".") }
        deleteKey.setOnClickListener {
            val value = amountInput.text?.toString().orEmpty()
            if (value.isNotEmpty()) amountInput.setText(value.dropLast(1))
        }
        doneButton.setOnClickListener {
            val amount = amountInput.text?.toString()?.toDoubleOrNull() ?: 0.0
            if (amount <= 0) {
                Toast.makeText(this@TransferAmountActivity, R.string.enter_amount_error, Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this@TransferAmountActivity, TransferConfirmActivity::class.java).apply {
                    putExtra(TransferConfirmActivity.EXTRA_AMOUNT, amount)
                })
            }
        }
    }

    private fun appendValue(value: String) {
        val current = viewBinding.amountInput.text?.toString().orEmpty()
        if (value == "." && current.contains('.')) return
        if (current.contains('.') && current.substringAfter('.').length >= 2 && value != ".") return
        val next = if (value == "." && current.isEmpty()) "0." else current + value
        if (next.length <= 9) viewBinding.amountInput.setText(next)
    }
}
