package com.lmt.global.base.presenter.bill

import android.content.Intent
import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityBillListBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletVisuals

class BillListActivity : WalletBaseActivity<ActivityBillListBinding>() {
    override fun provideLayout() = R.layout.activity_bill_list
    override fun initViews(savedInstanceState: Bundle?) = Unit
    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        newBillerButton.setOnClickListener {
            startActivity(Intent(this@BillListActivity, NewBillerActivity::class.java))
        }
        billerButton.setOnClickListener {
            showBill("Electricity", WalletVisuals.BILL_ELECTRICITY, 13_232L)
        }
        waterBillerButton.setOnClickListener {
            showBill("Water", WalletVisuals.BILL_WATER, 3_221L)
        }
        phoneBillerButton.isEnabled = false
        phoneBillerButton.alpha = 0.55f
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH && searchInput.text.isNotBlank()) {
                val query = searchInput.text.toString()
                if (query.contains("water", ignoreCase = true)) {
                    showBill("Water", WalletVisuals.BILL_WATER, 3_221L)
                } else {
                    showBill("Electricity", WalletVisuals.BILL_ELECTRICITY, 13_232L)
                }
                true
            } else {
                false
            }
        }
    }

    private fun showBill(name: String, iconKey: String, amountMinor: Long) {
        BillConfirmationBottomSheet.newInstance(name, iconKey, amountMinor)
            .show(supportFragmentManager, "bill-confirmation")
    }
}
