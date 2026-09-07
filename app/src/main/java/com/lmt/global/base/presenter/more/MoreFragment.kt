package com.lmt.global.base.presenter.more

import android.content.Intent
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IFragment
import com.lmt.global.base.databinding.FragmentMoreBinding
import com.lmt.global.base.presenter.about.AboutActivity
import com.lmt.global.base.presenter.bill.BillListActivity
import com.lmt.global.base.presenter.transfer.TransferListActivity
import org.koin.androidx.viewmodel.ext.android.viewModel

class MoreFragment : IFragment<FragmentMoreBinding, CommonViewModel>() {
    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.fragment_more
    override fun initViews() = Unit
    override fun initListeners() = with(viewBinding) {
        payBillsButton.setOnClickListener { open(BillListActivity::class.java) }
        transferButton.setOnClickListener { open(TransferListActivity::class.java) }
        aboutButton.setOnClickListener { open(AboutActivity::class.java) }
    }
    private fun open(target: Class<*>) = startActivity(Intent(requireContext(), target))
    companion object { fun newInstance() = MoreFragment() }
}
