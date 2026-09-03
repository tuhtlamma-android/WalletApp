package com.lmt.global.base.presenter.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.res.ColorStateList
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.widget.TextView
import android.widget.Toast
import androidx.annotation.LayoutRes
import androidx.core.content.ContextCompat
import androidx.core.widget.TextViewCompat
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityWalletBinding
import org.koin.androidx.viewmodel.ext.android.viewModel
import java.util.ArrayDeque

class WalletActivity : IActivity<ActivityWalletBinding, CommonViewModel>() {

    private enum class Screen {
        HOME, HISTORY, MORE, PROFILE, ABOUT, CARDS, CARD_PAYMENT,
        BILLS, PAYMENT_SUCCESS, TRANSFER, TRANSFER_AMOUNT, TRANSFER_CONFIRM, TRANSFER_FAILURE
    }

    private val backStack = ArrayDeque<Screen>()
    private var currentScreen = Screen.HOME

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.activity_wallet

    override fun initViews(savedInstanceState: Bundle?) {
        setupBottomNavigation()
        show(Screen.HOME, rememberCurrent = false)
    }

    private fun setupBottomNavigation() {
        viewBinding.navHome.setOnClickListener { openRoot(Screen.HOME) }
        viewBinding.navHistory.setOnClickListener { openRoot(Screen.HISTORY) }
        viewBinding.navCards.setOnClickListener { openRoot(Screen.CARDS) }
        viewBinding.navMore.setOnClickListener { openRoot(Screen.MORE) }
    }

    private fun openRoot(screen: Screen) {
        backStack.clear()
        show(screen, rememberCurrent = false)
    }

    private fun show(screen: Screen, rememberCurrent: Boolean = true) {
        if (rememberCurrent && screen != currentScreen) backStack.addLast(currentScreen)
        currentScreen = screen
        val view = LayoutInflater.from(this).inflate(screen.layoutRes, viewBinding.contentHost, false)
        viewBinding.contentHost.removeAllViews()
        viewBinding.contentHost.addView(view)
        viewBinding.bottomNavigation.visibility = if (screen.isRoot) View.VISIBLE else View.GONE
        updateNavigation(screen)
        bindActions(view, screen)
    }

    private fun bindActions(view: View, screen: Screen) {
        view.findViewById<View>(R.id.backButton)?.setOnClickListener { navigateBack() }
        view.findViewById<View>(R.id.viewAllButton)?.setOnClickListener { show(Screen.HISTORY) }
        view.findViewById<View>(R.id.transactionButton)?.setOnClickListener { showTransactionDetails() }
        view.findViewById<View>(R.id.payBillsButton)?.setOnClickListener { show(Screen.BILLS) }
        view.findViewById<View>(R.id.transferButton)?.setOnClickListener { show(Screen.TRANSFER) }
        view.findViewById<View>(R.id.profileButton)?.setOnClickListener { show(Screen.PROFILE) }
        view.findViewById<View>(R.id.aboutButton)?.setOnClickListener { show(Screen.ABOUT) }
        view.findViewById<View>(R.id.cardButton)?.setOnClickListener { show(Screen.CARD_PAYMENT) }
        view.findViewById<View>(R.id.billerButton)?.setOnClickListener { showBillConfirmation() }
        view.findViewById<View>(R.id.contactButton)?.setOnClickListener { show(Screen.TRANSFER_AMOUNT) }
        view.findViewById<View>(R.id.doneButton)?.setOnClickListener {
            when (screen) {
                Screen.TRANSFER_AMOUNT -> show(Screen.TRANSFER_CONFIRM)
                else -> Unit
            }
        }
        view.findViewById<View>(R.id.securePaymentButton)?.setOnClickListener {
            when (screen) {
                Screen.TRANSFER_CONFIRM -> show(Screen.TRANSFER_FAILURE)
                else -> Unit
            }
        }
        view.findViewById<View>(R.id.backToWalletButton)?.setOnClickListener { openRoot(Screen.HOME) }
        view.findViewById<View>(R.id.copyButton)?.setOnClickListener { copyTransactionNumber() }
    }

    private fun showTransactionDetails() {
        val dialog = BottomSheetDialog(this)
        val content = layoutInflater.inflate(R.layout.sheet_transaction_details, null)
        content.findViewById<View>(R.id.copyButton).setOnClickListener { copyTransactionNumber() }
        content.findViewById<View>(R.id.doneButton).setOnClickListener { dialog.dismiss() }
        dialog.setContentView(content)
        dialog.show()
    }

    private fun showBillConfirmation() {
        val dialog = BottomSheetDialog(this)
        val content = layoutInflater.inflate(R.layout.sheet_bill_payment, null)
        content.findViewById<View>(R.id.doneButton).setOnClickListener { dialog.dismiss() }
        content.findViewById<View>(R.id.securePaymentButton).setOnClickListener {
            dialog.dismiss()
            show(Screen.PAYMENT_SUCCESS)
        }
        dialog.setContentView(content)
        dialog.show()
    }

    private fun copyTransactionNumber() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Transaction number", "23010412432431"))
        Toast.makeText(this, "Transaction number copied", Toast.LENGTH_SHORT).show()
    }

    private fun navigateBack() {
        if (backStack.isEmpty()) {
            if (currentScreen == Screen.HOME) finish() else openRoot(Screen.HOME)
        } else {
            val previous = backStack.removeLast()
            show(previous, rememberCurrent = false)
        }
    }

    private fun updateNavigation(screen: Screen) {
        val selected = ContextCompat.getColor(this, R.color.wallet_purple)
        val idle = ContextCompat.getColor(this, R.color.wallet_black_coral)
        listOf(viewBinding.navHome, viewBinding.navHistory, viewBinding.navCards, viewBinding.navMore)
            .forEach {
                it.setTextColor(idle)
                TextViewCompat.setCompoundDrawableTintList(it, ColorStateList.valueOf(idle))
                it.setTypeface(it.typeface, android.graphics.Typeface.NORMAL)
            }
        val target: TextView? = when (screen) {
            Screen.HOME -> viewBinding.navHome
            Screen.HISTORY -> viewBinding.navHistory
            Screen.CARDS -> viewBinding.navCards
            Screen.MORE -> viewBinding.navMore
            else -> null
        }
        target?.setTextColor(selected)
        target?.let { TextViewCompat.setCompoundDrawableTintList(it, ColorStateList.valueOf(selected)) }
        target?.setTypeface(target.typeface, android.graphics.Typeface.BOLD)
    }

    override fun onHandleBackPressed(onBackPressed: (() -> Unit)?): Boolean {
        return super.onHandleBackPressed { navigateBack() }
    }

    private val Screen.isRoot: Boolean
        get() = this == Screen.HOME || this == Screen.HISTORY || this == Screen.CARDS || this == Screen.MORE

    private val Screen.layoutRes: Int
        @LayoutRes get() = when (this) {
            Screen.HOME -> R.layout.screen_home
            Screen.HISTORY -> R.layout.screen_history
            Screen.MORE -> R.layout.screen_more
            Screen.PROFILE -> R.layout.screen_profile
            Screen.ABOUT -> R.layout.screen_about
            Screen.CARDS -> R.layout.screen_cards
            Screen.CARD_PAYMENT -> R.layout.screen_card_payment
            Screen.BILLS -> R.layout.screen_bill_list
            Screen.PAYMENT_SUCCESS -> R.layout.screen_payment_success
            Screen.TRANSFER -> R.layout.screen_transfer_list
            Screen.TRANSFER_AMOUNT -> R.layout.screen_transfer_amount
            Screen.TRANSFER_CONFIRM -> R.layout.screen_transfer_confirm
            Screen.TRANSFER_FAILURE -> R.layout.screen_transfer_failure
        }
}
