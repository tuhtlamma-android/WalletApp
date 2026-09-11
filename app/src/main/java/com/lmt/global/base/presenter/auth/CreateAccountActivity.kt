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
import com.lmt.global.base.databinding.ActivityCreateAccountBinding
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CreateAccountActivity : IActivity<ActivityCreateAccountBinding, RegisterViewModel>() {

    override fun provideViewModel() = viewModel<RegisterViewModel>()
    override fun provideLayout() = R.layout.activity_create_account
    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding.emailInput.setText(intent.getStringExtra(EXTRA_IDENTIFIER).orEmpty())
    }

    override fun initObservers() {
        super.initObservers()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch { viewModel.uiState.collect { viewBinding.registerButton.isEnabled = !it.loading } }
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
        registerButton.setOnClickListener {
            val emptyInput = when {
                nameInput.text.isNullOrBlank() -> nameInput
                emailInput.text.isNullOrBlank() -> emailInput
                passwordInput.text.isNullOrBlank() -> passwordInput
                else -> null
            }
            val identifier = emailInput.text?.toString().orEmpty()
            val password = passwordInput.text?.toString().orEmpty()
            when {
                emptyInput != null -> {
                    emptyInput.requestFocus()
                    Toast.makeText(this@CreateAccountActivity, R.string.complete_all_fields, Toast.LENGTH_SHORT).show()
                }
                !AuthValidator.isIdentifierValid(identifier) -> {
                    emailInput.error = getString(R.string.invalid_identifier_error)
                    emailInput.requestFocus()
                    Toast.makeText(this@CreateAccountActivity, R.string.invalid_identifier_error, Toast.LENGTH_SHORT).show()
                }
                !AuthValidator.isPasswordValid(password) -> {
                    passwordInput.error = getString(R.string.password_length_error)
                    passwordInput.requestFocus()
                    Toast.makeText(this@CreateAccountActivity, R.string.password_length_error, Toast.LENGTH_SHORT).show()
                }
                !termsCheckbox.isChecked -> Toast.makeText(
                    this@CreateAccountActivity,
                    R.string.accept_terms_error,
                    Toast.LENGTH_SHORT
                ).show()
                else -> viewModel.onState(
                    RegisterAction.Submit(
                        name = nameInput.text?.toString().orEmpty(),
                        identifier = identifier,
                        password = password
                    )
                )
            }
        }
    }

    private fun handleEvent(event: RegisterEvent) {
        when (event) {
            RegisterEvent.NavigateOtp -> startActivity(Intent(this, OtpActivity::class.java))
            RegisterEvent.AlreadyExists -> {
                viewBinding.emailInput.error = getString(R.string.account_already_exists)
                viewBinding.emailInput.requestFocus()
                Toast.makeText(this, R.string.account_already_exists, Toast.LENGTH_SHORT).show()
            }
            RegisterEvent.InvalidInput -> Toast.makeText(this, R.string.invalid_registration_input, Toast.LENGTH_SHORT).show()
        }
    }

    companion object {
        const val EXTRA_IDENTIFIER = "register_identifier"
    }
}
