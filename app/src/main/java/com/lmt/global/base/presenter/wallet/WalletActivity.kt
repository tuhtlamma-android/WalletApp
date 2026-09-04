package com.lmt.global.base.presenter.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.os.Bundle
import android.text.InputType
import android.util.TypedValue
import android.view.Gravity
import android.view.inputmethod.EditorInfo
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
import com.lmt.global.base.databinding.ActivityWalletBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.ArrayDeque
import java.text.NumberFormat
import java.util.Locale

class WalletActivity : IActivity<ActivityWalletBinding, CommonViewModel>() {

    private enum class Screen(@DrawableRes val frame: Int) {
        HOME(R.drawable.img_home),
        HISTORY(R.drawable.img_history),
        TRANSACTION_DETAILS(R.drawable.img_transaction_details),
        PROFILE(R.drawable.img_profile),
        ABOUT(R.drawable.img_about),
        MORE(R.drawable.img_more),
        CARDS(R.drawable.img_cards),
        CARD_PAYMENT(R.drawable.img_card_payment),
        BILLS(R.drawable.img_bill_list),
        BILL_CONFIRMATION(R.drawable.img_bill_confirmation),
        PAYMENT_SUCCESS(R.drawable.img_payment_success),
        TRANSFER(R.drawable.img_transfer_list),
        TRANSFER_AMOUNT(R.drawable.img_transfer_amount),
        TRANSFER_CONFIRM(R.drawable.img_transfer_confirm),
        TRANSFER_FAILURE(R.drawable.img_transfer_failure)
    }

    private val backStack = ArrayDeque<Screen>()
    private var currentScreen = Screen.HOME
    private var billSearch = ""
    private var contactSearch = ""
    private var transferAmount = ""
    private var amountView: TextView? = null

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.activity_wallet
    override fun usesDarkStatusBarIcons() = false

    override fun initViews(savedInstanceState: Bundle?) {
        show(Screen.HOME, rememberCurrent = false)
    }

    private fun show(screen: Screen, rememberCurrent: Boolean = true) {
        if (rememberCurrent && screen != currentScreen) backStack.addLast(currentScreen)
        currentScreen = screen
        when (screen) {
            Screen.HOME -> applyStatusBarStyle(R.color.wallet_purple_dark, darkIcons = false)
            Screen.TRANSACTION_DETAILS, Screen.BILL_CONFIRMATION ->
                applyStatusBarStyle(R.color.wallet_overlay, darkIcons = true)
            Screen.TRANSFER_FAILURE ->
                applyStatusBarStyle(R.color.wallet_error_soft, darkIcons = true)
            else -> applyStatusBarStyle(R.color.white, darkIcons = true)
        }
        ScreenRenderer.render(viewBinding.contentHost, screen.frame, hotspotsFor(screen))
        addInteractiveFields(screen)
    }

    private fun openRoot(screen: Screen) {
        backStack.clear()
        show(screen, rememberCurrent = false)
    }

    private fun hotspotsFor(screen: Screen): List<Hotspot> {
        val screenActions = when (screen) {
            Screen.HOME -> listOf(
                spot(8, 43, 62, 104, "Profile") { show(Screen.PROFILE) },
                spot(323, 43, 358, 92, "Profile settings") { show(Screen.PROFILE) },
                spot(160, 166, 247, 236, "Transfer") { show(Screen.TRANSFER) },
                spot(278, 346, 360, 397, "View all history") { show(Screen.HISTORY) }
            )

            Screen.HISTORY -> listOf(
                spot(0, 145, 360, 700, "Transaction details") { show(Screen.TRANSACTION_DETAILS) }
            )

            Screen.TRANSACTION_DETAILS -> listOf(
                spot(292, 300, 360, 355, "Done") { navigateBack() },
                spot(300, 480, 352, 535, "Copy transaction number") { copyTransactionNumber() }
            )

            Screen.PROFILE -> listOf(back { navigateBack() })
            Screen.ABOUT -> listOf(back { navigateBack() })

            Screen.MORE -> listOf(
                spot(0, 112, 360, 157, "Pay bills") { show(Screen.BILLS) },
                spot(0, 157, 360, 214, "Transfer") { show(Screen.TRANSFER) },
                spot(0, 528, 360, 594, "About") { show(Screen.ABOUT) }
            )

            Screen.CARDS -> listOf(
                spot(12, 70, 348, 460, "Open card") { show(Screen.CARD_PAYMENT) }
            )

            Screen.CARD_PAYMENT -> listOf(back { navigateBack() })

            Screen.BILLS -> listOf(
                back { navigateBack() },
                spot(0, 367, 360, 490, "Electricity bill") { show(Screen.BILL_CONFIRMATION) }
            )

            Screen.BILL_CONFIRMATION -> listOf(
                spot(292, 450, 360, 500, "Done") { navigateBack() },
                spot(15, 705, 345, 780, "Secure payment") { show(Screen.PAYMENT_SUCCESS) }
            )

            Screen.PAYMENT_SUCCESS -> listOf(
                spot(15, 730, 345, 790, "Back to wallet") { openRoot(Screen.HOME) },
                spot(295, 600, 350, 660, "Copy transaction number") { copyTransactionNumber() }
            )

            Screen.TRANSFER -> listOf(
                back { navigateBack() },
                spot(0, 135, 360, 215, "New contact") { show(Screen.TRANSFER_AMOUNT) },
                spot(0, 300, 360, 790, "Select beneficiary") { show(Screen.TRANSFER_AMOUNT) }
            )

            Screen.TRANSFER_AMOUNT -> listOf(
                back { navigateBack() },
                keypadSpot(0, 410, 120, 485, "1"),
                keypadSpot(120, 410, 240, 485, "2"),
                keypadSpot(240, 410, 360, 485, "3"),
                keypadSpot(0, 485, 120, 552, "4"),
                keypadSpot(120, 485, 240, 552, "5"),
                keypadSpot(240, 485, 360, 552, "6"),
                keypadSpot(0, 552, 120, 620, "7"),
                keypadSpot(120, 552, 240, 620, "8"),
                keypadSpot(240, 552, 360, 620, "9"),
                keypadSpot(0, 620, 120, 690, "."),
                keypadSpot(120, 620, 240, 690, "0"),
                spot(240, 620, 360, 690, "Delete") { deleteAmountDigit() },
                spot(15, 710, 345, 795, "Done") { confirmTransferAmount() }
            )

            Screen.TRANSFER_CONFIRM -> listOf(
                back { navigateBack() },
                spot(15, 730, 345, 795, "Secure payment") { show(Screen.TRANSFER_FAILURE) }
            )

            Screen.TRANSFER_FAILURE -> listOf(
                spot(15, 730, 345, 795, "Back to wallet") { openRoot(Screen.HOME) }
            )
        }

        return if (screen.isRoot) screenActions + bottomNavigation() else screenActions
    }

    private fun bottomNavigation() = listOf(
        spot(0, 730, 90, 800, "Home") { openRoot(Screen.HOME) },
        spot(90, 730, 180, 800, "History") { openRoot(Screen.HISTORY) },
        spot(180, 730, 270, 800, "Cards") { openRoot(Screen.CARDS) },
        spot(270, 730, 360, 800, "More") { openRoot(Screen.MORE) }
    )

    private fun back(action: () -> Unit) = spot(5, 38, 86, 85, "Back", action)

    private fun keypadSpot(left: Int, top: Int, right: Int, bottom: Int, value: String) =
        spot(left, top, right, bottom, value) { appendAmount(value) }

    private fun addInteractiveFields(screen: Screen) {
        when (screen) {
            Screen.BILLS -> addSearchField(
                frame = screen.frame,
                hint = "Search biller",
                value = billSearch,
                onChanged = { billSearch = it },
                onSubmit = { show(Screen.BILL_CONFIRMATION) }
            )

            Screen.TRANSFER -> addSearchField(
                frame = screen.frame,
                hint = "Search contact",
                value = contactSearch,
                onChanged = { contactSearch = it },
                onSubmit = { show(Screen.TRANSFER_AMOUNT) }
            )

            Screen.TRANSFER_AMOUNT -> addAmountOverlay(screen.frame, top = 272f, bottom = 341f)
            Screen.TRANSFER_CONFIRM -> addAmountOverlay(screen.frame, top = 367f, bottom = 435f)
            else -> amountView = null
        }
    }

    private fun addSearchField(
        @DrawableRes frame: Int,
        hint: String,
        value: String,
        onChanged: (String) -> Unit,
        onSubmit: () -> Unit
    ) {
        val field = EditText(this).apply {
            id = if (hint == "Search biller") R.id.billSearchInput else R.id.contactSearchInput
            setSingleLine(true)
            inputType = InputType.TYPE_CLASS_TEXT
            imeOptions = EditorInfo.IME_ACTION_SEARCH
            setText(value)
            setSelection(text.length)
            this.hint = hint
            textSize = 12f
            gravity = Gravity.CENTER_VERTICAL
            includeFontPadding = false
            setTextColor(ContextCompat.getColor(context, R.color.wallet_black))
            setHintTextColor(ContextCompat.getColor(context, R.color.wallet_input_hint))
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            setPadding(dp(2), 0, dp(2), 0)
            typeface = ResourcesCompat.getFont(context, R.font.sora_variable)
            doAfterTextChanged { onChanged(it?.toString().orEmpty()) }
            setOnEditorActionListener { _, actionId, _ ->
                if (actionId == EditorInfo.IME_ACTION_SEARCH && text.isNotBlank()) {
                    onSubmit()
                    true
                } else {
                    false
                }
            }
        }
        ScreenRenderer.addOverlay(
            viewBinding.contentHost,
            frame,
            field,
            left = 48f,
            top = 298f,
            right = 337f,
            bottom = 346f
        )
    }

    private fun addAmountOverlay(@DrawableRes frame: Int, top: Float, bottom: Float) {
        val amount = TextView(this).apply {
            id = R.id.transferAmountText
            text = formattedTransferAmount()
            gravity = Gravity.CENTER
            setTextColor(ContextCompat.getColor(context, R.color.wallet_black))
            setBackgroundColor(ContextCompat.getColor(context, R.color.white))
            setTextSize(TypedValue.COMPLEX_UNIT_SP, 36f)
            typeface = ResourcesCompat.getFont(context, R.font.sora_variable)
            includeFontPadding = false
            maxLines = 1
        }
        amountView = amount
        ScreenRenderer.addOverlay(
            viewBinding.contentHost,
            frame,
            amount,
            left = 82f,
            top = top,
            right = 279f,
            bottom = bottom
        )
    }

    private fun appendAmount(value: String) {
        if (value == ".") {
            if (transferAmount.isEmpty()) transferAmount = "0."
            else if (!transferAmount.contains('.')) transferAmount += "."
        } else {
            val decimalDigits = transferAmount.substringAfter('.', "").length
            if (!transferAmount.contains('.') || decimalDigits < 2) {
                transferAmount += value
            }
        }
        amountView?.text = formattedTransferAmount()
    }

    private fun deleteAmountDigit() {
        if (transferAmount.isNotEmpty()) transferAmount = transferAmount.dropLast(1)
        amountView?.text = formattedTransferAmount()
    }

    private fun confirmTransferAmount() {
        val amount = transferAmount.toDoubleOrNull() ?: 0.0
        if (amount <= 0.0) {
            Toast.makeText(this, "Enter an amount", Toast.LENGTH_SHORT).show()
        } else {
            show(Screen.TRANSFER_CONFIRM)
        }
    }

    private fun formattedTransferAmount(): String {
        val amount = transferAmount.toDoubleOrNull() ?: 0.0
        return NumberFormat.getCurrencyInstance(Locale.US).format(amount)
    }

    private fun dp(value: Int) = (value * resources.displayMetrics.density).toInt()

    private fun spot(
        left: Int,
        top: Int,
        right: Int,
        bottom: Int,
        description: String,
        action: () -> Unit
    ) = Hotspot(left.toFloat(), top.toFloat(), right.toFloat(), bottom.toFloat(), description, action)

    private fun navigateBack() {
        if (backStack.isEmpty()) {
            if (currentScreen == Screen.HOME) finish() else openRoot(Screen.HOME)
        } else {
            show(backStack.removeLast(), rememberCurrent = false)
        }
    }

    private fun copyTransactionNumber() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Transaction number", "23010412432431"))
        Toast.makeText(this, "Transaction number copied", Toast.LENGTH_SHORT).show()
    }

    override fun onHandleBackPressed(onBackPressed: (() -> Unit)?): Boolean {
        return super.onHandleBackPressed { navigateBack() }
    }

    private val Screen.isRoot: Boolean
        get() = this == Screen.HOME || this == Screen.HISTORY ||
            this == Screen.CARDS || this == Screen.MORE
}
