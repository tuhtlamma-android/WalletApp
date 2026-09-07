package com.lmt.global.base.presenter.wallet

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.content.Intent
import android.widget.Toast
import androidx.databinding.ViewDataBinding
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

abstract class WalletBaseActivity<VB : ViewDataBinding> : IActivity<VB, CommonViewModel>() {

    final override fun provideViewModel() = viewModel<CommonViewModel>()

    protected fun openRoot(target: Class<*>) {
        if (javaClass == target) return
        startActivity(Intent(this, target).apply {
            flags = Intent.FLAG_ACTIVITY_CLEAR_TOP or Intent.FLAG_ACTIVITY_SINGLE_TOP
        })
        finish()
    }

    protected fun openPage(target: Class<*>) {
        startActivity(Intent(this, target))
    }

    protected fun copyTransactionNumber() {
        val clipboard = getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        clipboard.setPrimaryClip(ClipData.newPlainText("Transaction number", TRANSACTION_NUMBER))
        Toast.makeText(this, R.string.transaction_copied, Toast.LENGTH_SHORT).show()
    }

    companion object {
        const val TRANSACTION_NUMBER = "23010412432431"
    }
}
