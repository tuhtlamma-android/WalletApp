package com.lmt.global.base.presenter.bill

import android.os.Bundle
import androidx.recyclerview.widget.GridLayoutManager
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityNewBillerBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class NewBillerActivity : WalletBaseActivity<ActivityNewBillerBinding>() {
    override fun provideLayout() = R.layout.activity_new_biller

    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        billerList.layoutManager = GridLayoutManager(this@NewBillerActivity, GRID_SPAN_COUNT)
        billerList.adapter = BillerAdapter(::openAmountScreen)
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
    }

    private fun openAmountScreen(biller: BillerOption) {
        startActivity(
            BillAmountActivity.createIntent(
                context = this,
                billerName = getString(biller.nameRes),
                iconKey = biller.type
            )
        )
    }

    private companion object {
        const val GRID_SPAN_COUNT = 2
    }
}
