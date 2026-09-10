package com.lmt.global.base.presenter.wallet

import android.content.Context
import android.view.LayoutInflater
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lmt.global.base.R
import com.lmt.global.base.databinding.DialogAddBalanceBinding
import com.lmt.global.base.databinding.DialogAddRecipientBinding

fun showRecipientInputDialog(
    context: Context,
    onRecipient: (name: String, avatarKey: String) -> Unit
) {
    val binding = DialogAddRecipientBinding.inflate(LayoutInflater.from(context))
    val dialog = MaterialAlertDialogBuilder(context)
        .setView(binding.root)
        .create()

    binding.cancelButton.setOnClickListener { dialog.dismiss() }
    binding.continueButton.setOnClickListener {
        val name = binding.recipientNameInput.text?.toString()?.trim().orEmpty()
        if (name.isBlank()) {
            binding.recipientNameInput.error = context.getString(R.string.recipient_name_error)
        } else {
            dialog.dismiss()
            onRecipient(name, WalletVisuals.avatarKeyForName(name))
        }
    }
    dialog.setOnShowListener {
        binding.recipientNameInput.requestFocus()
    }
    dialog.show()
}

fun showAddBalanceDialog(context: Context, onAmount: (Long) -> Unit) {
    val binding = DialogAddBalanceBinding.inflate(LayoutInflater.from(context))
    val dialog = MaterialAlertDialogBuilder(context)
        .setView(binding.root)
        .create()

    binding.cancelButton.setOnClickListener { dialog.dismiss() }
    binding.addButton.setOnClickListener {
        val amountMinor = WalletMoney.parseToMinor(
            binding.amountInput.text?.toString().orEmpty()
        )
        if (amountMinor == null) {
            binding.amountInput.error = context.getString(R.string.enter_valid_amount_error)
        } else {
            dialog.dismiss()
            onAmount(amountMinor)
        }
    }
    dialog.setOnShowListener {
        binding.amountInput.requestFocus()
    }
    dialog.show()
}
