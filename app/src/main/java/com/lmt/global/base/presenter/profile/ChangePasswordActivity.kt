package com.lmt.global.base.presenter.profile

import android.os.Bundle
import android.text.method.PasswordTransformationMethod
import android.view.inputmethod.EditorInfo
import android.widget.EditText
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lmt.global.base.R
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityChangePasswordBinding
import com.lmt.global.base.presenter.auth.AuthValidator
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ChangePasswordActivity : IActivity<ActivityChangePasswordBinding, ChangePasswordViewModel>() {

    override fun provideViewModel() = viewModel<ChangePasswordViewModel>()
    override fun provideLayout() = R.layout.activity_change_password
    override fun initViews(savedInstanceState: Bundle?) = Unit

    override fun initObservers() {
        super.initObservers()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        with(viewBinding) {
                            updatePasswordButton.isEnabled = !state.loading
                            currentPasswordInput.isEnabled = !state.loading
                            newPasswordInput.isEnabled = !state.loading
                            confirmPasswordInput.isEnabled = !state.loading
                        }
                    }
                }
                launch { viewModel.events.collect(::handleEvent) }
            }
        }
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        currentPasswordToggle.setOnClickListener {
            togglePasswordVisibility(currentPasswordInput)
        }
        newPasswordToggle.setOnClickListener {
            togglePasswordVisibility(newPasswordInput)
        }
        confirmPasswordToggle.setOnClickListener {
            togglePasswordVisibility(confirmPasswordInput)
        }
        confirmPasswordInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_DONE) {
                updatePasswordButton.performClick()
                true
            } else {
                false
            }
        }
        updatePasswordButton.setOnClickListener { submitChangePassword() }
    }

    private fun submitChangePassword() = with(viewBinding) {
        currentPasswordInput.error = null
        newPasswordInput.error = null
        confirmPasswordInput.error = null

        val currentPassword = currentPasswordInput.text?.toString().orEmpty()
        val newPassword = newPasswordInput.text?.toString().orEmpty()
        val confirmPassword = confirmPasswordInput.text?.toString().orEmpty()
        val emptyInput = when {
            currentPassword.isBlank() -> currentPasswordInput
            newPassword.isBlank() -> newPasswordInput
            confirmPassword.isBlank() -> confirmPasswordInput
            else -> null
        }
        when {
            emptyInput != null -> {
                emptyInput.requestFocus()
                Toast.makeText(
                    this@ChangePasswordActivity,
                    R.string.complete_all_fields,
                    Toast.LENGTH_SHORT
                ).show()
            }
            !AuthValidator.isPasswordValid(newPassword) -> {
                newPasswordInput.error = getString(R.string.password_length_error)
                newPasswordInput.requestFocus()
            }
            newPassword != confirmPassword -> {
                confirmPasswordInput.error = getString(R.string.password_confirmation_error)
                confirmPasswordInput.requestFocus()
            }
            else -> viewModel.onState(ChangePasswordAction.Submit(currentPassword, newPassword))
        }
    }

    private fun togglePasswordVisibility(input: EditText) {
        input.transformationMethod = if (input.transformationMethod == null) {
            PasswordTransformationMethod.getInstance()
        } else {
            null
        }
        input.setSelection(input.text?.length ?: 0)
    }

    private fun handleEvent(event: ChangePasswordEvent) {
        when (event) {
            ChangePasswordEvent.Success -> {
                Toast.makeText(this, R.string.password_changed, Toast.LENGTH_SHORT).show()
                finish()
            }
            ChangePasswordEvent.WrongCurrentPassword -> with(viewBinding.currentPasswordInput) {
                error = getString(R.string.incorrect_current_password)
                requestFocus()
            }
            ChangePasswordEvent.SamePassword -> with(viewBinding.newPasswordInput) {
                error = getString(R.string.new_password_must_be_different)
                requestFocus()
            }
            ChangePasswordEvent.InvalidInput -> {
                Toast.makeText(this, R.string.invalid_password_change, Toast.LENGTH_SHORT).show()
            }
            ChangePasswordEvent.SessionExpired -> {
                Toast.makeText(this, R.string.account_session_unavailable, Toast.LENGTH_SHORT).show()
                finish()
            }
            ChangePasswordEvent.Failed -> {
                Toast.makeText(this, R.string.password_change_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }
}
