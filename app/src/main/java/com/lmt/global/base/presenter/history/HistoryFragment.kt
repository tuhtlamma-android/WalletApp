package com.lmt.global.base.presenter.history

import androidx.recyclerview.widget.LinearLayoutManager
import androidx.core.widget.doAfterTextChanged
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IFragment
import com.lmt.global.base.databinding.FragmentHistoryBinding
import com.lmt.global.base.presenter.wallet.TransactionAdapter
import com.lmt.global.base.presenter.wallet.WalletTransaction
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : IFragment<FragmentHistoryBinding, CommonViewModel>() {

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.fragment_history

    override fun initViews() = with(viewBinding) {
        transactionList.layoutManager = LinearLayoutManager(requireContext())
        val adapter = TransactionAdapter(transactions) {
            TransactionDetailsBottomSheet.newInstance()
                .show(parentFragmentManager, "transaction-details")
        }
        transactionList.adapter = adapter
        searchInput.doAfterTextChanged { adapter.filter(it?.toString().orEmpty()) }
        Unit
    }

    private val transactions = listOf(
        WalletTransaction(R.drawable.img_merchant_walmart, "Walmart", "Today 12:32", "-$35.23", sectionLabel = "Today"),
        WalletTransaction(R.drawable.img_merchant_top_up, "Top up", "Yesterday 02:12", "+$430.00", incoming = true),
        WalletTransaction(R.drawable.img_merchant_netflix, "Netflix", "Dec 24 12:32", "-$13.00"),
        WalletTransaction(R.drawable.img_merchant_amazon, "Amazon", "Today 12:32", "-$12.23", sectionLabel = "Yesterday"),
        WalletTransaction(R.drawable.img_merchant_nike, "Nike", "Yesterday 02:12", "-$50.23"),
        WalletTransaction(R.drawable.img_merchant_home_depot, "The Home Depot", "Dec 24 13:53", "-$129.00"),
        WalletTransaction(R.drawable.img_merchant_amazon, "Amazon", "Today 12:32", "-$35.23", sectionLabel = "Thursday\nDecember 29, 2022")
    )

    companion object { fun newInstance() = HistoryFragment() }
}
