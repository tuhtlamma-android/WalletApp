package com.lmt.global.base.presenter.bill

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lmt.global.base.databinding.BottomSheetBillConfirmationBinding

class BillConfirmationBottomSheet : BottomSheetDialogFragment() {
    private var binding: BottomSheetBillConfirmationBinding? = null

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View =
        BottomSheetBillConfirmationBinding.inflate(inflater, container, false).also { binding = it }.root

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        binding?.apply {
            doneButton.setOnClickListener { dismiss() }
            securePaymentButton.setOnClickListener {
                dismiss()
                startActivity(Intent(requireContext(), PaymentSuccessActivity::class.java))
            }
        }
    }

    override fun onDestroyView() { binding = null; super.onDestroyView() }
    companion object { fun newInstance() = BillConfirmationBottomSheet() }
}
