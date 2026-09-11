package com.lmt.global.base.presenter.history

import android.view.View
import androidx.core.widget.doAfterTextChanged
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lmt.global.base.R
import com.lmt.global.base.common.IFragment
import com.lmt.global.base.databinding.FragmentHistoryBinding
import com.lmt.global.base.presenter.wallet.TransactionAdapter
import com.lmt.global.base.presenter.wallet.toWalletTransaction
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HistoryFragment : IFragment<FragmentHistoryBinding, HistoryViewModel>() {
    private lateinit var transactionAdapter: TransactionAdapter

    override fun provideViewModel() = viewModel<HistoryViewModel>()
    override fun provideLayout() = R.layout.fragment_history

    override fun initViews() = with(viewBinding) {
        transactionList.layoutManager = LinearLayoutManager(requireContext())
        transactionAdapter = TransactionAdapter(
            showSections = true,
            onClick = { transaction ->
                TransactionDetailsBottomSheet.newInstance(transaction)
                    .show(parentFragmentManager, "transaction-details")
            },
            onListChanged = { count ->
                emptyState.visibility = if (count == 0) View.VISIBLE else View.GONE
            }
        )
        transactionList.adapter = transactionAdapter
        searchInput.doAfterTextChanged { transactionAdapter.filter(it?.toString().orEmpty()) }
        Unit
    }

    override fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    transactionAdapter.submitTransactions(
                        state.transactions.map { it.toWalletTransaction() }
                    )
                }
            }
        }
    }

    companion object { fun newInstance() = HistoryFragment() }
}
