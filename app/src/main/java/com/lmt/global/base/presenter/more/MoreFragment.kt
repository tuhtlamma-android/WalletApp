package com.lmt.global.base.presenter.more

import android.content.Intent
import com.lmt.global.base.R
import com.lmt.global.base.common.IFragment
import com.lmt.global.base.databinding.FragmentMoreBinding
import com.lmt.global.base.presenter.about.AboutActivity
import com.lmt.global.base.presenter.bill.BillListActivity
import com.lmt.global.base.presenter.transfer.TransferListActivity
import com.lmt.global.base.presenter.auth.LoginActivity
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoreFragment : IFragment<FragmentMoreBinding, MoreViewModel>() {
    override fun provideViewModel() = viewModel<MoreViewModel>()
    override fun provideLayout() = R.layout.fragment_more
    override fun initViews() = Unit
    override fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.events.collect { event ->
                    if (event is MoreEvent.NavigateLogin) {
                        startActivity(Intent(requireContext(), LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        })
                    }
                }
            }
        }
    }
    override fun initListeners() = with(viewBinding) {
        payBillsButton.setOnClickListener { open(BillListActivity::class.java) }
        transferButton.setOnClickListener { open(TransferListActivity::class.java) }
        aboutButton.setOnClickListener { open(AboutActivity::class.java) }
        logoutButton.setOnClickListener { viewModel.onState(MoreAction.Logout) }
    }
    private fun open(target: Class<*>) = startActivity(Intent(requireContext(), target))
    companion object { fun newInstance() = MoreFragment() }
}
