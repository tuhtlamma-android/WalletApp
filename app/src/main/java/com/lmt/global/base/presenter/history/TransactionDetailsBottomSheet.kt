package com.lmt.global.base.presenter.history

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lmt.global.base.R
import com.lmt.global.base.databinding.BottomSheetTransactionDetailsBinding
import com.lmt.global.base.data.entity.TransactionEntity
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.WalletTransaction
import com.lmt.global.base.presenter.wallet.WalletVisuals
import com.lmt.global.base.presenter.wallet.walletFullDateTime
import com.lmt.global.base.presenter.wallet.walletTransactionNumber

class TransactionDetailsBottomSheet : BottomSheetDialogFragment() {
    private var binding: BottomSheetTransactionDetailsBinding? = null
    private val transactionId get() = requireArguments().getLong(ARG_ID)
    private val transactionNumber get() = walletTransactionNumber(transactionId)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        BottomSheetTransactionDetailsBinding.inflate(inflater, container, false).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            val iconKey = requireArguments().getString(ARG_ICON_KEY).orEmpty()
            val merchant = requireArguments().getString(ARG_MERCHANT).orEmpty()
            val amountMinor = requireArguments().getLong(ARG_AMOUNT_MINOR)
            val createdAt = requireArguments().getLong(ARG_CREATED_AT)
            val type = requireArguments().getString(ARG_TYPE).orEmpty()
            merchantIcon.setImageResource(WalletVisuals.iconRes(iconKey))
            merchantText.text = merchant
            typeText.text = if (type == TransactionEntity.TYPE_TRANSFER) {
                getString(R.string.money_transfer)
            } else {
                getString(R.string.bill_payment)
            }
            amountText.text = "-${WalletMoney.format(amountMinor)}"
            dateText.text = walletFullDateTime(createdAt)
            transactionNumberText.text = getString(R.string.transaction_number_value, transactionNumber)
            doneButton.setOnClickListener { dismiss() }
            copyButton.setOnClickListener {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Transaction number", transactionNumber))
                Toast.makeText(requireContext(), R.string.transaction_copied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        private const val ARG_ID = "id"
        private const val ARG_ICON_KEY = "icon_key"
        private const val ARG_MERCHANT = "merchant"
        private const val ARG_AMOUNT_MINOR = "amount_minor"
        private const val ARG_CREATED_AT = "created_at"
        private const val ARG_TYPE = "type"

        fun newInstance(transaction: WalletTransaction) = TransactionDetailsBottomSheet().apply {
            arguments = Bundle().apply {
                putLong(ARG_ID, transaction.id)
                putString(ARG_ICON_KEY, transaction.iconKey)
                putString(ARG_MERCHANT, transaction.merchant)
                putLong(ARG_AMOUNT_MINOR, transaction.amountMinor)
                putLong(ARG_CREATED_AT, transaction.createdAt)
                putString(ARG_TYPE, transaction.type)
            }
        }
    }
}
