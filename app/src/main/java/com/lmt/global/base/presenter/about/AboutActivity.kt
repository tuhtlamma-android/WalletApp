package com.lmt.global.base.presenter.about

import android.os.Bundle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityAboutBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class AboutActivity : WalletBaseActivity<ActivityAboutBinding>() {
    override fun provideLayout() = R.layout.activity_about
    override fun initViews(savedInstanceState: Bundle?) = Unit
    override fun initListeners() {
        viewBinding.backButton.setOnClickListener { finish() }
    }
}
