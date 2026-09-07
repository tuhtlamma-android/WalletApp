package com.lmt.global.base.presenter.auth

import android.content.Intent
import android.os.Bundle
import android.os.CountDownTimer
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.core.widget.doAfterTextChanged
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityOtpBinding
import com.lmt.global.base.presenter.home.HomeActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class OtpActivity : IActivity<ActivityOtpBinding, CommonViewModel>() {

    private var resendTimer: CountDownTimer? = null

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.activity_otp

    override fun initViews(savedInstanceState: Bundle?) {
        viewBinding.otpInput.showSoftInputOnFocus = false
        updateValidState()
        startResendCountdown()
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        otpInput.doAfterTextChanged { updateValidState() }
        listOf(key0, key1, key2, key3, key4, key5, key6, key7, key8, key9).forEach { key ->
            key.setOnClickListener { appendDigit((it as TextView).text.toString()) }
        }
        deleteKey.setOnClickListener {
            val digits = otpDigits()
            if (digits.isNotEmpty()) renderOtp(digits.dropLast(1))
        }
        resendButton.setOnClickListener { startResendCountdown() }
        doneButton.setOnClickListener {
            if (otpDigits().length == OTP_LENGTH) {
                startActivity(Intent(this@OtpActivity, HomeActivity::class.java).apply {
                    flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                })
            } else {
                Toast.makeText(this@OtpActivity, R.string.enter_otp_error, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun appendDigit(digit: String) {
        val value = otpDigits()
        if (value.length < OTP_LENGTH) renderOtp(value + digit)
    }

    private fun updateValidState() = with(viewBinding) {
        val valid = otpDigits().length == OTP_LENGTH
        validIcon.visibility = if (valid) View.VISIBLE else View.GONE
    }

    private fun startResendCountdown() = with(viewBinding.resendButton) {
        resendTimer?.cancel()
        isEnabled = false
        alpha = DISABLED_ALPHA
        text = getString(R.string.resend_countdown, COUNTDOWN_SECONDS)

        resendTimer = object : CountDownTimer(COUNTDOWN_MILLIS, TICK_MILLIS) {
            override fun onTick(millisUntilFinished: Long) {
                val seconds = (millisUntilFinished + TICK_MILLIS - 1) / TICK_MILLIS
                text = getString(R.string.resend_countdown, seconds)
            }

            override fun onFinish() {
                text = getString(R.string.resend_code)
                isEnabled = true
                alpha = ENABLED_ALPHA
            }
        }.start()
    }

    private fun otpDigits(): String = viewBinding.otpInput.text?.filter(Char::isDigit)?.toString().orEmpty()

    private fun renderOtp(digits: String) {
        val formatted = if (digits.length <= 3) digits else digits.take(3) + "-" + digits.drop(3)
        viewBinding.otpInput.setText(formatted)
        viewBinding.otpInput.setSelection(formatted.length)
    }

    override fun onDestroy() {
        resendTimer?.cancel()
        resendTimer = null
        super.onDestroy()
    }

    companion object {
        private const val OTP_LENGTH = 6
        private const val COUNTDOWN_SECONDS = 30L
        private const val COUNTDOWN_MILLIS = COUNTDOWN_SECONDS * 1_000L
        private const val TICK_MILLIS = 1_000L
        private const val DISABLED_ALPHA = 0.55f
        private const val ENABLED_ALPHA = 1f
    }
}
