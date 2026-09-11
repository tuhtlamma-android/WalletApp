package com.lmt.global.base.presenter.auth

import android.content.Intent
import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lmt.global.base.R
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityPasswordBinding
import com.lmt.global.base.presenter.home.HomeActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class PasswordActivity : IActivity<ActivityPasswordBinding, LoginViewModel>() {

    override fun provideViewModel() = viewModel<LoginViewModel>()
    override fun provideLayout() = R.layout.activity_password
    override fun initViews(savedInstanceState: Bundle?) = Unit

    override fun initObservers() {
        super.initObservers()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { viewBinding.loginButton.isEnabled = !it.loading } }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        passwordToggle.setOnClickListener {
            passwordInput.transformationMethod = if (passwordInput.transformationMethod == null) {
                PasswordTransformationMethod.getInstance()
            } else null
            passwordInput.setSelection(passwordInput.text?.length ?: 0)
        }
        forgotButton.setOnClickListener {
            ForgotPasswordBottomSheet.newInstance().show(supportFragmentManager, "forgot-password")
        }
        loginButton.setOnClickListener {
            val password = passwordInput.text?.toString().orEmpty()
            if (password.isBlank()) {
                passwordInput.requestFocus()
                Toast.makeText(this@PasswordActivity, R.string.enter_password_error, Toast.LENGTH_SHORT).show()
            } else if (!AuthValidator.isPasswordValid(password)) {
                passwordInput.error = getString(R.string.password_length_error)
                passwordInput.requestFocus()
                Toast.makeText(this@PasswordActivity, R.string.password_length_error, Toast.LENGTH_SHORT).show()
            } else {
                viewModel.onState(
                    LoginAction.Submit(
                        identifier = intent.getStringExtra(EXTRA_IDENTIFIER).orEmpty(),
                        password = password
                    )
                )
            }
        }
    }

    private fun handleEvent(event: LoginEvent) {
        when (event) {
            LoginEvent.NavigateHome -> startActivity(Intent(this, HomeActivity::class.java).apply {
                flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            })
            LoginEvent.AccountNotFound -> {
                Toast.makeText(this, R.string.account_not_found_create, Toast.LENGTH_LONG).show()
                startActivity(Intent(this, CreateAccountActivity::class.java).apply {
                    putExtra(CreateAccountActivity.EXTRA_IDENTIFIER, intent.getStringExtra(EXTRA_IDENTIFIER))
                })
            }
            LoginEvent.WrongPassword -> {
                viewBinding.passwordInput.error = getString(R.string.incorrect_password)
                viewBinding.passwordInput.requestFocus()
                Toast.makeText(this, R.string.incorrect_password, Toast.LENGTH_SHORT).show()
            }
            LoginEvent.InvalidInput -> Toast.makeText(this, R.string.invalid_login_input, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_IDENTIFIER = "auth_identifier"
    }
}
