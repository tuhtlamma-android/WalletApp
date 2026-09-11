package com.lmt.global.base.presenter.auth

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.text.InputType
import android.view.View
import android.widget.Toast
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityLoginBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : IActivity<ActivityLoginBinding, CommonViewModel>() {
    private var useEmail = false

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.activity_login

    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        countryCodePicker.registerCarrierNumberEditText(mobileInput)
        countryCodePicker.setTypeFace(mobileInput.typeface)
        countryCodePicker.textView_selectedCountry.includeFontPadding = false
        countryCodePicker.setFlagSize(dpToPx(COUNTRY_FLAG_HEIGHT_DP))

        Resources.getSystem().configuration.locales[0].country
            .takeIf { it.length == ISO_COUNTRY_CODE_LENGTH }
            ?.let { localeCountry ->
                countryCodePicker.setDefaultCountryUsingNameCode(localeCountry)
                countryCodePicker.setCountryForNameCode(localeCountry)
            }

        countryCodePicker.setAutoDetectedCountry(true)
    }

    override fun initListeners() = with(viewBinding) {
        continueButton.setOnClickListener {
            val input = mobileInput.text?.toString().orEmpty().trim()
            val isEmail = useEmail
            when {
                input.isBlank() -> {
                    mobileInput.requestFocus()
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.enter_identifier_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                isEmail && !AuthValidator.isEmailValid(input) -> {
                    mobileInput.error = getString(R.string.invalid_email_error)
                    mobileInput.requestFocus()
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.invalid_email_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                !isEmail && !countryCodePicker.isValidFullNumber -> {
                    mobileInput.error = getString(R.string.invalid_phone_error)
                    mobileInput.requestFocus()
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.invalid_phone_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    val identifier = if (isEmail) input else countryCodePicker.fullNumberWithPlus
                    startActivity(Intent(this@LoginActivity, PasswordActivity::class.java).apply {
                        putExtra(PasswordActivity.EXTRA_IDENTIFIER, identifier)
                    })
                }
            }
        }
        createAccountButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, CreateAccountActivity::class.java))
        }
        identifierModeButton.setOnClickListener {
            useEmail = !useEmail
            mobileInput.text?.clear()
            if (useEmail) {
                countryCodePicker.deregisterCarrierNumberEditText()
                countryCodePicker.visibility = View.GONE
                mobileInput.hint = getString(R.string.email_hint)
                mobileInput.inputType = InputType.TYPE_CLASS_TEXT or
                    InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS
                identifierModeButton.setText(R.string.use_mobile_instead)
            } else {
                countryCodePicker.visibility = View.VISIBLE
                countryCodePicker.registerCarrierNumberEditText(mobileInput)
                mobileInput.hint = getString(R.string.phone_hint)
                mobileInput.inputType = InputType.TYPE_CLASS_PHONE
                identifierModeButton.setText(R.string.use_email_instead)
            }
            mobileInput.requestFocus()
        }
    }

    companion object {
        private const val ISO_COUNTRY_CODE_LENGTH = 2
        private const val COUNTRY_FLAG_HEIGHT_DP = 21
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}
