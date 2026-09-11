package com.lmt.global.base.presenter.bill

import android.content.Context
import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityBillAmountBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.WalletVisuals
import java.math.BigDecimal

class BillAmountActivity : WalletBaseActivity<ActivityBillAmountBinding>() {
    private val billerName by lazy { intent.getStringExtra(EXTRA_BILLER_NAME).orEmpty() }
    private val iconKey by lazy {
        intent.getStringExtra(EXTRA_ICON_KEY) ?: WalletVisuals.BILL_OTHER
    }
    private val suggestedAmountMinor by lazy {
        intent.getLongExtra(EXTRA_SUGGESTED_AMOUNT_MINOR, NO_SUGGESTED_AMOUNT)
    }

    override fun provideLayout() = R.layout.activity_bill_amount

    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        amountInput.showSoftInputOnFocus = false
        billerNameText.text = billerName
        billerIcon.setImageResource(WalletVisuals.iconRes(iconKey))
        if (savedInstanceState == null && suggestedAmountMinor > 0L) {
            amountInput.setText(BigDecimal.valueOf(suggestedAmountMinor, 2).toPlainString())
            amountInput.setSelection(amountInput.text?.length ?: 0)
        }
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
            val amountMinor = WalletMoney.parseToMinor(amountInput.text?.toString().orEmpty())
            if (amountMinor == null) {
                Toast.makeText(this@BillAmountActivity, R.string.enter_amount_error, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            startActivity(
                BillConfirmActivity.createIntent(
                    context = this@BillAmountActivity,
                    billerName = billerName,
                    iconKey = iconKey,
                    amountMinor = amountMinor
                )
            )
        }
    }

    private fun appendValue(value: String) {
        val current = viewBinding.amountInput.text?.toString().orEmpty()
        if (value == "." && current.contains('.')) return
        if (current.contains('.') && current.substringAfter('.').length >= 2 && value != ".") return
        val next = if (value == "." && current.isEmpty()) "0." else current + value
        if (next.length <= MAX_AMOUNT_LENGTH) {
            viewBinding.amountInput.setText(next)
            viewBinding.amountInput.setSelection(next.length)
        }
    }

    companion object {
        private const val EXTRA_BILLER_NAME = "biller_name"
        private const val EXTRA_ICON_KEY = "biller_icon_key"
        private const val EXTRA_SUGGESTED_AMOUNT_MINOR = "suggested_amount_minor"
        private const val NO_SUGGESTED_AMOUNT = -1L
        private const val MAX_AMOUNT_LENGTH = 9

        fun createIntent(
            context: Context,
            billerName: String,
            iconKey: String,
            suggestedAmountMinor: Long = NO_SUGGESTED_AMOUNT
        ) = Intent(context, BillAmountActivity::class.java).apply {
            putExtra(EXTRA_BILLER_NAME, billerName)
            putExtra(EXTRA_ICON_KEY, iconKey)
            putExtra(EXTRA_SUGGESTED_AMOUNT_MINOR, suggestedAmountMinor)
        }
    }
}
