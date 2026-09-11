package com.lmt.global.base.presenter.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.lmt.global.base.R
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivitySplashBinding
import com.lmt.global.base.presenter.auth.LoginActivity
import com.lmt.global.base.presenter.home.HomeActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : IActivity<ActivitySplashBinding, SplashViewModel>() {

    override fun provideViewModel() = viewModel<SplashViewModel>()
    override fun provideLayout() = R.layout.activity_splash
    override fun initViews(savedInstanceState: Bundle?) = Unit

    override fun initObservers() {
        super.initObservers()
        lifecycleScope.launch {
            viewModel.destinations.collect { destination ->
                delay(SPLASH_DURATION_MS)
                val target = when (destination) {
                    SplashDestination.Home -> HomeActivity::class.java
                    SplashDestination.Login -> LoginActivity::class.java
                }
                startActivity(Intent(this@SplashActivity, target))
                finish()
            }
        }
    }

    override fun isHideSystemBars() = true

    companion object {
        private const val SPLASH_DURATION_MS = 900L
    }
}
