package com.lmt.global.base.presenter.home

import android.content.Intent
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IFragment
import com.lmt.global.base.databinding.FragmentHomeBinding
import com.lmt.global.base.presenter.profile.ProfileActivity
import com.lmt.global.base.presenter.transfer.TransferListActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : IFragment<FragmentHomeBinding, CommonViewModel>() {

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.fragment_home
    override fun initViews() = Unit

    override fun initListeners() = with(viewBinding) {
        val openProfile = android.view.View.OnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }
        profileAvatarButton.setOnClickListener(openProfile)
        profileButton.setOnClickListener(openProfile)
        transferButton.setOnClickListener { openTransfer() }
        addContactButton.setOnClickListener { openTransfer() }
        viewAllButton.setOnClickListener {
            (requireActivity() as HomeActivity).showTab(HomePagerAdapter.HISTORY)
        }
    }

    private fun openTransfer() {
        startActivity(Intent(requireContext(), TransferListActivity::class.java))
    }

    companion object { fun newInstance() = HomeFragment() }
}
