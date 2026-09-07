package com.lmt.global.base.presenter.profile

import android.os.Bundle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityProfileBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity

class ProfileActivity : WalletBaseActivity<ActivityProfileBinding>() {
    override fun provideLayout() = R.layout.activity_profile
    override fun initViews(savedInstanceState: Bundle?) = Unit
    override fun initListeners() { viewBinding.backButton.setOnClickListener { finish() } }
}
