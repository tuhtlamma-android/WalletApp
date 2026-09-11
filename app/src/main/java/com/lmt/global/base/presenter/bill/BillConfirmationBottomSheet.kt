package com.lmt.global.base.presenter.bill

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lmt.global.base.R
import com.lmt.global.base.databinding.BottomSheetBillConfirmationBinding
import com.lmt.global.base.model.TransactionType
import com.lmt.global.base.model.WalletActionResult
import com.lmt.global.base.presenter.transfer.TransferFailureActivity
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.WalletVisuals
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class BillConfirmationBottomSheet : BottomSheetDialogFragment() {
    private var binding: BottomSheetBillConfirmationBinding? = null
    private val billViewModel by viewModel<BillViewModel>()
    private val billerName get() = requireArguments().getString(ARG_BILLER_NAME).orEmpty()
    private val iconKey get() = requireArguments().getString(ARG_ICON_KEY).orEmpty()
    private val amountMinor get() = requireArguments().getLong(ARG_AMOUNT_MINOR)

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        BottomSheetBillConfirmationBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            billerIcon.setImageResource(WalletVisuals.iconRes(iconKey))
            billerNameText.text = billerName
            dueAmountText.text = getString(R.string.due_amount, WalletMoney.format(amountMinor))
            doneButton.setOnClickListener { dismiss() }
            securePaymentButton.setOnClickListener {
                securePaymentButton.isEnabled = false
                billViewModel.onState(BillAction.Submit(billerName, iconKey, amountMinor))
            }
        }
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                billViewModel.results.collect(::handlePaymentResult)
            }
        }
    }

    private fun handlePaymentResult(event: BillPaymentResult) {
        when (val result = event.result) {
            is WalletActionResult.Success -> openSuccessActivity(event, result)
            WalletActionResult.InsufficientBalance -> openFailureActivity(
                getString(R.string.payment_failed),
                getString(R.string.insufficient_balance_message)
            )
            else -> openFailureActivity(
                getString(R.string.payment_failed),
                getString(R.string.invalid_transaction_message)
            )
        }
    }

    private fun openSuccessActivity(
        event: BillPaymentResult,
        result: WalletActionResult.Success
    ) {
        val host = requireActivity()
        val intent = PaymentSuccessActivity.createIntent(
            context = host,
            type = TransactionType.PAY_BILL,
            name = event.billerName,
            amountMinor = event.amountMinor,
            transactionId = result.transactionId ?: 0L
        )
        dismiss()
        startActivity(intent)
        host.finish()
    }

    private fun openFailureActivity(title: String, message: String) {
        val host = requireActivity()
        val intent = TransferFailureActivity.createIntent(host, title, message)
        dismiss()
        startActivity(intent)
        host.finish()
    }

    override fun onDestroyView() { binding = null; super.onDestroyView() }
    companion object {
        private const val ARG_BILLER_NAME = "biller_name"
        private const val ARG_ICON_KEY = "icon_key"
        private const val ARG_AMOUNT_MINOR = "amount_minor"

        fun newInstance(name: String, iconKey: String, amountMinor: Long) =
            BillConfirmationBottomSheet().apply {
                arguments = Bundle().apply {
                    putString(ARG_BILLER_NAME, name)
                    putString(ARG_ICON_KEY, iconKey)
                    putLong(ARG_AMOUNT_MINOR, amountMinor)
                }
            }
    }
}
