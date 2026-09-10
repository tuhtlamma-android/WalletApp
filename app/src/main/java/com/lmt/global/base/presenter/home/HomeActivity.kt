package com.lmt.global.base.presenter.home

import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.lmt.global.base.R
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityHomeBinding
import com.lmt.global.base.extension.collectLatestRepeatOnLifecycle
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : IActivity<ActivityHomeBinding, HomeViewModel>() {
    override fun provideViewModel() = viewModel<HomeViewModel>()
    override fun provideLayout() = R.layout.activity_home

    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        pager.adapter = HomePagerAdapter(this@HomeActivity)
        pager.isUserInputEnabled = false
        pager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                updateSelectedTab(position)
            }
        })
        updateSelectedTab(pager.currentItem)
    }

    override fun initListeners() = with(viewBinding) {
        bottomNavigation.navHome.setOnClickListener { showTab(HomePagerAdapter.HOME) }
        bottomNavigation.navHistory.setOnClickListener { showTab(HomePagerAdapter.HISTORY) }
        bottomNavigation.navCards.setOnClickListener { showTab(HomePagerAdapter.CARDS) }
        bottomNavigation.navMore.setOnClickListener { showTab(HomePagerAdapter.MORE) }
    }

    fun showTab(position: Int) {
        viewBinding.pager.setCurrentItem(position, false)
    }

    private fun updateSelectedTab(position: Int) = with(viewBinding) {
        mapOf<View, Int>(
            bottomNavigation.navHome to HomePagerAdapter.HOME,
            bottomNavigation.navHistory to HomePagerAdapter.HISTORY,
            bottomNavigation.navCards to HomePagerAdapter.CARDS,
            bottomNavigation.navMore to HomePagerAdapter.MORE
        ).forEach { (view, tab) -> view.isSelected = tab == position }

        if (position == HomePagerAdapter.HOME) {
            viewBinding.root.setBackgroundColor(
                ContextCompat.getColor(this@HomeActivity, R.color.wallet_purple_dark)
            )
            applyStatusBarStyle(R.color.wallet_purple_dark, darkIcons = false)
        } else {
            viewBinding.root.setBackgroundColor(
                ContextCompat.getColor(this@HomeActivity, R.color.white)
            )
            applyStatusBarStyle(R.color.white, darkIcons = true)
        }
    }

    override fun initObservers() {
        super.initObservers()
        viewBinding.bottomNavigation.navHome.setOnClickListener {
            viewModel.onState(HomeState.AddUser("dajshd"))
        }
    }

    override fun onHandleBackPressed(onBackPressed: (() -> Unit)?): Boolean {
        return super.onHandleBackPressed {
            if (viewBinding.pager.currentItem == HomePagerAdapter.HOME) {
                finish()
            } else {
                showTab(HomePagerAdapter.HOME)
            }
        }
    }
}
