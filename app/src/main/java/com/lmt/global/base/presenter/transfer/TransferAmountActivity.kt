package com.lmt.global.base.presenter.transfer

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityTransferAmountBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.WalletVisuals

class TransferAmountActivity : WalletBaseActivity<ActivityTransferAmountBinding>() {
    override fun provideLayout() = R.layout.activity_transfer_amount
    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        viewBinding.amountInput.showSoftInputOnFocus = false
        val name = intent.getStringExtra(EXTRA_RECIPIENT_NAME).orEmpty()
        val avatarKey = intent.getStringExtra(EXTRA_AVATAR_KEY) ?: WalletVisuals.AVATAR_ALI
        recipientNameText.text = name
        recipientAvatar.setImageResource(WalletVisuals.iconRes(avatarKey))
        recipientSubtitle.visibility = android.view.View.GONE
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
                Toast.makeText(this@TransferAmountActivity, R.string.enter_amount_error, Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this@TransferAmountActivity, TransferConfirmActivity::class.java).apply {
                    putExtra(TransferConfirmActivity.EXTRA_AMOUNT_MINOR, amountMinor)
                    putExtra(EXTRA_RECIPIENT_NAME, intent.getStringExtra(EXTRA_RECIPIENT_NAME))
                    putExtra(EXTRA_AVATAR_KEY, intent.getStringExtra(EXTRA_AVATAR_KEY))
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

    companion object {
        const val EXTRA_RECIPIENT_NAME = "recipient_name"
        const val EXTRA_AVATAR_KEY = "recipient_avatar_key"
    }
}
