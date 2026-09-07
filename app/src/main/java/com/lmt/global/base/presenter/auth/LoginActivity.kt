package com.lmt.global.base.presenter.auth

import android.content.Intent
import android.content.res.Resources
import android.os.Bundle
import android.widget.Toast
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityLoginBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : IActivity<ActivityLoginBinding, CommonViewModel>() {

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
            when {
                mobileInput.text.isNullOrBlank() -> {
                    mobileInput.requestFocus()
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.enter_phone_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                !countryCodePicker.isValidFullNumber -> {
                    mobileInput.error = getString(R.string.invalid_phone_error)
                    mobileInput.requestFocus()
                    Toast.makeText(
                        this@LoginActivity,
                        R.string.invalid_phone_error,
                        Toast.LENGTH_SHORT
                    ).show()
                }
                else -> {
                    startActivity(Intent(this@LoginActivity, PasswordActivity::class.java))
                }
            }
        }
        createAccountButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, CreateAccountActivity::class.java))
        }
    }

    companion object {
        private const val ISO_COUNTRY_CODE_LENGTH = 2
        private const val COUNTRY_FLAG_HEIGHT_DP = 21
    }

    private fun dpToPx(dp: Int): Int =
        (dp * resources.displayMetrics.density).toInt()
}
