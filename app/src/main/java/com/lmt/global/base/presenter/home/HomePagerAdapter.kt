package com.lmt.global.base.presenter.home

import androidx.fragment.app.Fragment
import androidx.fragment.app.FragmentActivity
import androidx.viewpager2.adapter.FragmentStateAdapter
import com.lmt.global.base.presenter.cards.CardsFragment
import com.lmt.global.base.presenter.history.HistoryFragment
import com.lmt.global.base.presenter.more.MoreFragment

class HomePagerAdapter(activity: FragmentActivity) : FragmentStateAdapter(activity) {

    override fun createFragment(position: Int): Fragment = when (position) {
        HISTORY -> HistoryFragment.newInstance()
        CARDS -> CardsFragment.newInstance()
        MORE -> MoreFragment.newInstance()
        else -> HomeFragment.newInstance()
    }

    override fun getItemCount() = TAB_COUNT

    companion object {
        const val HOME = 0
        const val HISTORY = 1
        const val CARDS = 2
        const val MORE = 3
        const val TAB_COUNT = 4
    }
}
