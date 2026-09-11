package com.lmt.global.base.presenter.profile

import android.os.Bundle
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityProfileBinding
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class ProfileActivity : WalletBaseActivity<ActivityProfileBinding>() {
    private val profileViewModel by viewModel<ProfileViewModel>()

    override fun provideLayout() = R.layout.activity_profile
    override fun initViews(savedInstanceState: Bundle?) = Unit
    override fun initObservers() {
        super.initObservers()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                profileViewModel.account.collect { account ->
                    account ?: return@collect
                    with(viewBinding) {
                        profileNameText.text = account.name
                        fullNameText.text = account.name
                        mobileText.text = account.phone ?: getString(R.string.not_provided)
                        emailText.text = account.email ?: getString(R.string.not_provided)
                    }
                }
            }
        }
    }
    override fun initListeners() { viewBinding.backButton.setOnClickListener { finish() } }
}
