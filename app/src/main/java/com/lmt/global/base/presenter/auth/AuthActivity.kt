package com.lmt.global.base.presenter.auth

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.annotation.LayoutRes
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityAuthBinding
import com.lmt.global.base.presenter.wallet.WalletActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthActivity : IActivity<ActivityAuthBinding, CommonViewModel>() {

    private enum class AuthScreen { PHONE, PASSWORD, REGISTER, OTP }

    private var currentScreen = AuthScreen.PHONE

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.activity_auth

    override fun initViews(savedInstanceState: Bundle?) {
        show(AuthScreen.PHONE)
    }

    private fun show(screen: AuthScreen) {
        currentScreen = screen
        val view = LayoutInflater.from(this).inflate(screen.layoutRes, viewBinding.contentHost, false)
        viewBinding.contentHost.removeAllViews()
        viewBinding.contentHost.addView(view)

        view.findViewById<View>(R.id.backButton)?.setOnClickListener {
            when (screen) {
                AuthScreen.PHONE -> finish()
                AuthScreen.PASSWORD, AuthScreen.REGISTER -> show(AuthScreen.PHONE)
                AuthScreen.OTP -> show(AuthScreen.REGISTER)
            }
        }
        view.findViewById<View>(R.id.continueButton)?.setOnClickListener { show(AuthScreen.PASSWORD) }
        view.findViewById<View>(R.id.registerButton)?.setOnClickListener { show(AuthScreen.OTP) }
        view.findViewById<View>(R.id.loginButton)?.setOnClickListener { openWallet() }
        view.findViewById<View>(R.id.doneButton)?.setOnClickListener { openWallet() }
        view.findViewById<View>(R.id.forgotButton)?.setOnClickListener { showForgotPassword() }
    }

    private fun showForgotPassword() {
        val dialog = BottomSheetDialog(this)
        val content = layoutInflater.inflate(R.layout.sheet_forgot_password, null)
        content.findViewById<View>(R.id.sendResetButton).setOnClickListener { dialog.dismiss() }
        content.findViewById<View>(R.id.switchResetMethodButton).setOnClickListener { dialog.dismiss() }
        dialog.setContentView(content)
        dialog.show()
    }

    private fun openWallet() {
        startActivity(Intent(this, WalletActivity::class.java))
        finish()
    }

    override fun onHandleBackPressed(onBackPressed: (() -> Unit)?): Boolean {
        return super.onHandleBackPressed {
            when (currentScreen) {
                AuthScreen.PHONE -> finish()
                AuthScreen.PASSWORD, AuthScreen.REGISTER -> show(AuthScreen.PHONE)
                AuthScreen.OTP -> show(AuthScreen.REGISTER)
            }
        }
    }

    private val AuthScreen.layoutRes: Int
        @LayoutRes get() = when (this) {
            AuthScreen.PHONE -> R.layout.screen_auth_phone
            AuthScreen.PASSWORD -> R.layout.screen_auth_password
            AuthScreen.REGISTER -> R.layout.screen_auth_register
            AuthScreen.OTP -> R.layout.screen_auth_otp
        }
}
