package com.lmt.global.base.presenter.auth

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.text.method.PasswordTransformationMethod
import android.util.TypedValue
import android.view.Gravity
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.DrawableRes
import androidx.core.content.ContextCompat
import androidx.core.content.res.ResourcesCompat
import androidx.core.widget.doAfterTextChanged
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.ScreenRenderer
import com.lmt.global.base.common.ScreenRenderer.Hotspot
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityAuthBinding
import com.lmt.global.base.presenter.wallet.WalletActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class AuthActivity : IActivity<ActivityAuthBinding, CommonViewModel>() {

    private enum class AuthScreen(@DrawableRes val frame: Int) {
        PHONE(R.drawable.img_login),
        REGISTER(R.drawable.img_create_account),
        OTP_EMPTY(R.drawable.img_otp_empty),
        OTP_VALID(R.drawable.img_otp_valid),
        PASSWORD(R.drawable.img_password),
        FORGOT_EMAIL(R.drawable.img_forgot_email),
        FORGOT_MOBILE(R.drawable.img_forgot_mobile)
    }

    private var currentScreen = AuthScreen.PHONE
    private var phoneNumber = ""
    private var password = ""
    private var registerName = ""
    private var registerEmail = ""
    private var registerPassword = ""
    private var termsAccepted = false
    private var otpDigits = ""
    private var otpInput: EditText? = null
    private var termsView: TextView? = null

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.activity_auth
    override fun initViews(savedInstanceState: Bundle?) {
        show(AuthScreen.PHONE)
    }

    private fun show(screen: AuthScreen) {
        currentScreen = screen
        when (screen) {
            AuthScreen.PHONE, AuthScreen.PASSWORD ->
                applyStatusBarStyle(R.color.wallet_auth_background, darkIcons = true)
            AuthScreen.FORGOT_EMAIL, AuthScreen.FORGOT_MOBILE ->
                applyStatusBarStyle(R.color.wallet_overlay, darkIcons = true)
            else -> applyStatusBarStyle(R.color.white, darkIcons = true)
        }
        ScreenRenderer.render(viewBinding.contentHost, screen.frame, hotspotsFor(screen))
        addInteractiveFields(screen)
    }

    private fun hotspotsFor(screen: AuthScreen): List<Hotspot> = when (screen) {
        AuthScreen.PHONE -> listOf(
            spot(15, 596, 345, 660, "Continue") { continueWithPhone() },
            spot(15, 704, 345, 790, "Create an account") { show(AuthScreen.REGISTER) }
        )

        AuthScreen.REGISTER -> listOf(
            back { show(AuthScreen.PHONE) },
            spot(15, 558, 55, 600, "Accept terms") { toggleTerms() },
            spot(15, 626, 345, 694, "Create a new account") { continueRegistration() }
        )

        AuthScreen.OTP_EMPTY -> listOf(
            back { show(AuthScreen.REGISTER) },
            *otpKeypadHotspots().toTypedArray(),
            spot(15, 710, 345, 800, "Done") { submitOtp() }
        )

        AuthScreen.OTP_VALID -> listOf(
            back { show(AuthScreen.REGISTER) },
            *otpKeypadHotspots().toTypedArray(),
            spot(15, 710, 345, 800, "Done") { submitOtp() }
        )

        AuthScreen.PASSWORD -> listOf(
            back { show(AuthScreen.PHONE) },
            spot(200, 535, 348, 585, "Forgot password") { show(AuthScreen.FORGOT_EMAIL) },
            spot(15, 710, 345, 780, "Login") { login() }
        )

        AuthScreen.FORGOT_EMAIL -> listOf(
            spot(292, 490, 352, 540, "Done") { show(AuthScreen.PASSWORD) },
            spot(70, 714, 290, 790, "Use mobile instead") { show(AuthScreen.FORGOT_MOBILE) }
        )

        AuthScreen.FORGOT_MOBILE -> listOf(
            spot(292, 490, 352, 540, "Done") { show(AuthScreen.PASSWORD) },
            spot(70, 714, 290, 790, "Use email instead") { show(AuthScreen.FORGOT_EMAIL) }
        )
    }

    private fun back(action: () -> Unit) = spot(12, 42, 82, 78, "Back", action)

    private fun otpKeypadHotspots() = listOf(
        otpKey(0, 410, 120, 485, "1"),
        otpKey(120, 410, 240, 485, "2"),
        otpKey(240, 410, 360, 485, "3"),
        otpKey(0, 485, 120, 552, "4"),
        otpKey(120, 485, 240, 552, "5"),
        otpKey(240, 485, 360, 552, "6"),
        otpKey(0, 552, 120, 620, "7"),
        otpKey(120, 552, 240, 620, "8"),
        otpKey(240, 552, 360, 620, "9"),
        otpKey(120, 620, 240, 690, "0"),
        spot(240, 620, 360, 690, "Delete") { deleteOtpDigit() }
    )

    private fun otpKey(left: Int, top: Int, right: Int, bottom: Int, digit: String) =
        spot(left, top, right, bottom, digit) { appendOtpDigit(digit) }

    private fun addInteractiveFields(screen: AuthScreen) {
        when (screen) {
            AuthScreen.PHONE -> {
                addAuthField(
                    frame = screen.frame,
                    id = R.id.authPhoneInput,
                    value = phoneNumber,
                    hint = "7X-XXXXXXX",
                    inputType = InputType.TYPE_CLASS_PHONE,
                    left = 124f,
                    top = 512f,
                    right = 338f,
                    bottom = 555f,
                    onChanged = { phoneNumber = it }
                )
                addCreateAccountLink(screen.frame)
            }

            AuthScreen.PASSWORD -> addAuthField(
                frame = screen.frame,
                id = R.id.authPasswordInput,
                value = password,
                hint = "Enter your password",
                inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                left = 27f,
                top = 487f,
                right = 302f,
                bottom = 530f,
                passwordField = true,
                onChanged = { password = it }
            )

            AuthScreen.REGISTER -> {
                addAuthField(screen.frame, R.id.registerNameInput, registerName, "e.g. John Doe",
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_FLAG_CAP_WORDS,
                    27f, 334f, 337f, 377f, onChanged = { registerName = it })
                addAuthField(screen.frame, R.id.registerEmailInput, registerEmail,
                    "e.g. email@example.com",
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_EMAIL_ADDRESS,
                    27f, 417f, 337f, 460f, onChanged = { registerEmail = it })
                addAuthField(screen.frame, R.id.registerPasswordInput, registerPassword,
                    "Enter your password",
                    InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD,
                    27f, 500f, 302f, 543f, passwordField = true,
                    onChanged = { registerPassword = it })
                addTermsCheckbox(screen.frame)
            }

            AuthScreen.OTP_EMPTY, AuthScreen.OTP_VALID -> addOtpInput(screen.frame)
            else -> Unit
        }
    }

    private fun addAuthField(
        @DrawableRes frame: Int,
        id: Int,
        value: String,
        hint: String,
        inputType: Int,
        left: Float,
        top: Float,
        right: Float,
        bottom: Float,
        passwordField: Boolean = false,
        onChanged: (String) -> Unit
    ) {
        val input = EditText(this).apply {
            this.id = id
            setSingleLine(true)
            this.inputType = inputType
            setText(value)
            setSelection(text.length)
            this.hint = hint
            textSize = 14f
            gravity = Gravity.CENTER_VERTICAL
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(context, R.color.wallet_black))
            setHintTextColor(ContextCompat.getColor(context, R.color.wallet_input_hint))
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            setPadding(0, 0, 0, 0)
            typeface = ResourcesCompat.getFont(context, R.font.sora_variable)
            if (passwordField) transformationMethod = PasswordTransformationMethod.getInstance()
            doAfterTextChanged { onChanged(it?.toString().orEmpty()) }
        }
        ScreenRenderer.addOverlay(
            viewBinding.contentHost, frame, input, left, top, right, bottom
        )
    }

    private fun addCreateAccountLink(@DrawableRes frame: Int) {
        val link = TextView(this).apply {
            text = "Create account"
            gravity = Gravity.CENTER
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 12f)
            setTextColor(ContextCompat.getColor(context, R.color.wallet_blue))
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            typeface = ResourcesCompat.getFont(context, R.font.sora_variable)
            setOnClickListener { show(AuthScreen.REGISTER) }
            contentDescription = "Create account"
        }
        ScreenRenderer.addOverlay(
            viewBinding.contentHost, frame, link, 205f, 562f, 345f, 596f
        )
    }

    private fun addTermsCheckbox(@DrawableRes frame: Int) {
        val check = TextView(this).apply {
            text = if (termsAccepted) "✓" else ""
            gravity = Gravity.CENTER
            includeFontPadding = false
            textSize = 14f
            setTextColor(ContextCompat.getColor(context, R.color.wallet_purple))
            setBackgroundColor(android.graphics.Color.TRANSPARENT)
            setOnClickListener { toggleTerms() }
            contentDescription = "Accept terms and conditions"
        }
        termsView = check
        ScreenRenderer.addOverlay(
            viewBinding.contentHost, frame, check, 17f, 570f, 34f, 588f
        )
    }

    private fun addOtpInput(@DrawableRes frame: Int) {
        val input = EditText(this).apply {
            id = R.id.otpInput
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_NUMBER
            showSoftInputOnFocus = false
            setText(formattedOtp())
            hint = "XXX-XXX"
            gravity = Gravity.CENTER
            includeFontPadding = false
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 36f)
            setTextColor(ContextCompat.getColor(context, R.color.wallet_black))
            setHintTextColor(ContextCompat.getColor(context, R.color.wallet_input_hint))
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            setPadding(0, 0, 0, 0)
            typeface = ResourcesCompat.getFont(context, R.font.sora_variable)
        }
        otpInput = input
        ScreenRenderer.addOverlay(
            viewBinding.contentHost,
            frame,
            input,
            left = 80f,
            top = 183f,
            right = if (currentScreen == AuthScreen.OTP_VALID) 242f else 280f,
            bottom = 250f
        )
    }

    private fun continueWithPhone() {
        if (phoneNumber.isBlank()) {
            Toast.makeText(this, "Enter your mobile number", Toast.LENGTH_SHORT).show()
        } else {
            show(AuthScreen.PASSWORD)
        }
    }

    private fun login() {
        if (password.isBlank()) {
            Toast.makeText(this, "Enter your password", Toast.LENGTH_SHORT).show()
        } else {
            openWallet()
        }
    }

    private fun continueRegistration() {
        when {
            registerName.isBlank() -> Toast.makeText(this, "Enter your name", Toast.LENGTH_SHORT).show()
            registerEmail.isBlank() -> Toast.makeText(this, "Enter your email", Toast.LENGTH_SHORT).show()
            registerPassword.isBlank() -> Toast.makeText(this, "Enter a password", Toast.LENGTH_SHORT).show()
            !termsAccepted -> Toast.makeText(this, "Accept the terms and conditions", Toast.LENGTH_SHORT).show()
            else -> show(AuthScreen.OTP_EMPTY)
        }
    }

    private fun toggleTerms() {
        termsAccepted = !termsAccepted
        termsView?.text = if (termsAccepted) "✓" else ""
    }

    private fun appendOtpDigit(digit: String) {
        if (otpDigits.length >= 6) return
        otpDigits += digit
        otpInput?.setText(formattedOtp())
        if (otpDigits.length == 6 && currentScreen == AuthScreen.OTP_EMPTY) {
            show(AuthScreen.OTP_VALID)
        }
    }

    private fun deleteOtpDigit() {
        if (otpDigits.isNotEmpty()) otpDigits = otpDigits.dropLast(1)
        if (currentScreen == AuthScreen.OTP_VALID) show(AuthScreen.OTP_EMPTY)
        else otpInput?.setText(formattedOtp())
    }

    private fun formattedOtp(): String = when {
        otpDigits.isEmpty() -> ""
        otpDigits.length <= 3 -> otpDigits
        else -> otpDigits.take(3) + "-" + otpDigits.drop(3)
    }

    private fun submitOtp() {
        if (otpDigits.length == 6) openWallet()
        else Toast.makeText(this, "Enter the six-digit code", Toast.LENGTH_SHORT).show()
    }

    private fun spot(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        description: String,
        action: () -> Unit
    ) = Hotspot(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), description, action)

    private fun openWallet() {
        startActivity(Intent(this, WalletActivity::class.java))
        finish()
    }

    override fun onHandleBackPressed(onBackPressed: (() -> Unit)?): Boolean {
        return super.onHandleBackPressed {
            when (currentScreen) {
                AuthScreen.PHONE -> finish()
                AuthScreen.REGISTER, AuthScreen.PASSWORD -> show(AuthScreen.PHONE)
                AuthScreen.OTP_EMPTY, AuthScreen.OTP_VALID -> show(AuthScreen.REGISTER)
                AuthScreen.FORGOT_EMAIL, AuthScreen.FORGOT_MOBILE -> show(AuthScreen.PASSWORD)
            }
        }
    }
}
