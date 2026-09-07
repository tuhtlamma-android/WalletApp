package com.lmt.global.base.presenter.bill

import android.os.Bundle
import android.view.inputmethod.EditorInfo
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityBillListBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class BillListActivity : WalletBaseActivity<ActivityBillListBinding>() {
    override fun provideLayout() = R.layout.activity_bill_list
    override fun initViews(savedInstanceState: Bundle?) = Unit
    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        billerButton.setOnClickListener {
            BillConfirmationBottomSheet.newInstance().show(supportFragmentManager, "bill-confirmation")
        }
        searchInput.setOnEditorActionListener { _, actionId, _ ->
            if (actionId == EditorInfo.IME_ACTION_SEARCH && searchInput.text.isNotBlank()) {
                BillConfirmationBottomSheet.newInstance().show(supportFragmentManager, "bill-confirmation")
                true
            } else {
                false
            }
        }
    }
}
