package com.lmt.global.base.presenter.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityCreateAccountBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateAccountActivity : IActivity<ActivityCreateAccountBinding, CommonViewModel>() {

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.activity_create_account
    override fun initViews(savedInstanceState: Bundle?) = Unit

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        passwordToggle.setOnClickListener {
            passwordInput.transformationMethod = if (passwordInput.transformationMethod == null) {
                PasswordTransformationMethod.getInstance()
            } else {
                null
            }
            passwordInput.setSelection(passwordInput.text?.length ?: 0)
        }
        registerButton.setOnClickListener {
            val valid = when {
                nameInput.text.isNullOrBlank() -> nameInput
                emailInput.text.isNullOrBlank() -> emailInput
                passwordInput.text.isNullOrBlank() -> passwordInput
                else -> null
            }
            when {
                valid != null -> {
                    valid.requestFocus()
                    Toast.makeText(this@CreateAccountActivity, R.string.complete_all_fields, Toast.LENGTH_SHORT).show()
                }
                !termsCheckbox.isChecked -> Toast.makeText(
                    this@CreateAccountActivity,
                    R.string.accept_terms_error,
                    Toast.LENGTH_SHORT
                ).show()
                else -> startActivity(Intent(this@CreateAccountActivity, OtpActivity::class.java))
            }
        }
    }

}
