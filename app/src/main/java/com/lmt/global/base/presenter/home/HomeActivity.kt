package com.lmt.global.base.presenter.home

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.viewpager2.widget.ViewPager2
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityHomeBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeActivity : IActivity<ActivityHomeBinding, CommonViewModel>() {
    override fun provideViewModel() = viewModel<CommonViewModel>()
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
        showRequestedTab(intent)
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

    override fun onNewIntent(intent: Intent) {
        super.onNewIntent(intent)
        setIntent(intent)
        showRequestedTab(intent)
    }

    private fun showRequestedTab(intent: Intent) {
        val requestedTab = intent.getIntExtra(EXTRA_SELECTED_TAB, NO_SELECTED_TAB)
        if (requestedTab in 0 until HomePagerAdapter.TAB_COUNT) {
            showTab(requestedTab)
        }
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

    override fun onHandleBackPressed(onBackPressed: (() -> Unit)?): Boolean {
        return super.onHandleBackPressed {
            if (viewBinding.pager.currentItem == HomePagerAdapter.HOME) {
                finish()
            } else {
                showTab(HomePagerAdapter.HOME)
            }
        }
    }

    companion object {
        const val EXTRA_SELECTED_TAB = "selected_home_tab"
        private const val NO_SELECTED_TAB = -1
    }
}
