package com.lmt.global.base.presenter.auth

import android.content.Intent
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

    override fun initViews(savedInstanceState: Bundle?) = Unit

    override fun initListeners() = with(viewBinding) {
        continueButton.setOnClickListener {
            if (mobileInput.text.isNullOrBlank()) {
                mobileInput.requestFocus()
                Toast.makeText(this@LoginActivity, R.string.enter_phone_error, Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this@LoginActivity, PasswordActivity::class.java))
            }
        }
        createAccountButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, CreateAccountActivity::class.java))
        }
    }

}
