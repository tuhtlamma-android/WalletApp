package com.lmt.global.base.presenter.wallet

import android.content.Context
import android.text.InputType
import android.widget.EditText
import androidx.appcompat.app.AlertDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.lmt.global.base.R

fun showRecipientInputDialog(
    context: Context,
    onRecipient: (name: String, avatarKey: String) -> Unit
) {
    val input = EditText(context).apply {
        hint = context.getString(R.string.recipient_name_hint)
        inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PERSON_NAME
        maxLines = 1
    }
    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(R.string.new_recipient)
        .setView(input)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.continue_label, null)
        .create()

    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val name = input.text?.toString()?.trim().orEmpty()
            if (name.isBlank()) {
                input.error = context.getString(R.string.recipient_name_error)
            } else {
                dialog.dismiss()
                onRecipient(name, WalletVisuals.avatarKeyForName(name))
            }
        }
        input.requestFocus()
    }
    dialog.show()
}

fun showAddBalanceDialog(context: Context, onAmount: (Long) -> Unit) {
    val input = EditText(context).apply {
        hint = "0.00"
        inputType = InputType.TYPE_CLASS_NUMBER or InputType.TYPE_NUMBER_FLAG_DECIMAL
        maxLines = 1
    }
    val dialog = MaterialAlertDialogBuilder(context)
        .setTitle(R.string.add_balance)
        .setView(input)
        .setNegativeButton(android.R.string.cancel, null)
        .setPositiveButton(R.string.add, null)
        .create()

    dialog.setOnShowListener {
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
            val amountMinor = WalletMoney.parseToMinor(input.text?.toString().orEmpty())
            if (amountMinor == null) {
                input.error = context.getString(R.string.enter_valid_amount_error)
            } else {
                dialog.dismiss()
                onAmount(amountMinor)
            }
        }
        input.requestFocus()
    }
    dialog.show()
}
