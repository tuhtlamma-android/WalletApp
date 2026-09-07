package com.lmt.global.base.presenter.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.EditorInfo
import android.widget.Toast
import com.google.android.material.bottomsheet.BottomSheetDialogFragment
import com.lmt.global.base.R
import com.lmt.global.base.databinding.BottomSheetForgotPasswordBinding

class ForgotPasswordBottomSheet : BottomSheetDialogFragment() {

    private var binding: BottomSheetForgotPasswordBinding? = null
    private var useMobile = false

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, state: Bundle?): View {
        return BottomSheetForgotPasswordBinding.inflate(inflater, container, false).also {
            binding = it
        }.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        renderMode()
        binding?.apply {
            doneButton.setOnClickListener { dismiss() }
            switchModeButton.setOnClickListener {
                useMobile = !useMobile
                renderMode()
            }
            sendButton.setOnClickListener {
                if (resetInput.text.isNullOrBlank()) {
                    resetInput.requestFocus()
                } else {
                    Toast.makeText(requireContext(), R.string.reset_link_sent, Toast.LENGTH_SHORT).show()
                    dismiss()
                }
            }
            resetInput.imeOptions = EditorInfo.IME_ACTION_DONE
        }
    }

    private fun renderMode() {
        binding?.apply {
            inputLabel.setText(if (useMobile) R.string.mobile_number else R.string.email)
            mobilePrefix.visibility = if (useMobile) View.VISIBLE else View.GONE
            resetInput.layoutParams = resetInput.layoutParams.apply {
                if (this is ViewGroup.MarginLayoutParams) {
                    marginStart = if (useMobile) resources.getDimensionPixelSize(R.dimen.wallet_space_md) else 0
                }
            }
            resetInput.hint = getString(if (useMobile) R.string.phone_hint else R.string.email_hint)
            switchModeButton.setText(if (useMobile) R.string.use_email_instead else R.string.use_mobile_instead)
            resetInput.inputType = if (useMobile) android.text.InputType.TYPE_CLASS_PHONE else
                android.text.InputType.TYPE_CLASS_TEXT or android.text.InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
        }
    }

    override fun onDestroyView() {
        binding = null
        super.onDestroyView()
    }

    companion object {
        fun newInstance() = ForgotPasswordBottomSheet()
    }
}
