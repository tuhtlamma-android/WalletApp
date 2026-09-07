package com.lmt.global.base.presenter.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityPasswordBinding
import com.lmt.global.base.presenter.home.HomeActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class PasswordActivity : IActivity<ActivityPasswordBinding, CommonViewModel>() {

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.activity_password
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
        forgotButton.setOnClickListener {
            ForgotPasswordBottomSheet.newInstance().show(supportFragmentManager, "forgot-password")
        }
        loginButton.setOnClickListener {
            if (passwordInput.text.isNullOrBlank()) {
                passwordInput.requestFocus()
                Toast.makeText(this@PasswordActivity, R.string.enter_password_error, Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this@PasswordActivity, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            }
        }
    }

}
