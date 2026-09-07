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
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class TransactionDetailsBottomSheet : BottomSheetDialogFragment() {
    private var binding: BottomSheetTransactionDetailsBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        BottomSheetTransactionDetailsBinding.inflate(inflater, container, false).also {
            binding = it
        }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            doneButton.setOnClickListener { dismiss() }
            copyButton.setOnClickListener {
                val clipboard = requireContext().getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
                clipboard.setPrimaryClip(ClipData.newPlainText("Transaction number", WalletBaseActivity.TRANSACTION_NUMBER))
                Toast.makeText(requireContext(), R.string.transaction_copied, Toast.LENGTH_SHORT).show()
            }
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object { fun newInstance() = TransactionDetailsBottomSheet() }
}
