package com.lmt.global.base.presenter.splash

import android.annotation.SuppressLint
import android.content.Intent
import android.os.Bundle
import androidx.lifecycle.lifecycleScope
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivitySplashBinding
import com.lmt.global.base.presenter.auth.AuthActivity
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

@SuppressLint("CustomSplashScreen")
class SplashActivity : IActivity<ActivitySplashBinding, CommonViewModel>() {

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.activity_splash
    override fun initViews(savedInstanceState: Bundle?) {
        lifecycleScope.launch {
            delay(SPLASH_DURATION_MS)
            startActivity(Intent(this@SplashActivity, AuthActivity::class.java))
            finish()
        }
    }

    override fun isHideSystemBars() = true

    companion object {
        private const val SPLASH_DURATION_MS = 900L
    }
}
