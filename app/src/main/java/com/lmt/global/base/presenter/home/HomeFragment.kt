package com.lmt.global.base.presenter.home

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lmt.global.base.R
import com.lmt.global.base.common.IFragment
import com.lmt.global.base.databinding.FragmentHomeBinding
import com.lmt.global.base.model.WalletActionResult
import com.lmt.global.base.presenter.profile.ProfileActivity
import com.lmt.global.base.presenter.history.TransactionDetailsBottomSheet
import com.lmt.global.base.presenter.transfer.TransferAmountActivity
import com.lmt.global.base.presenter.transfer.TransferListActivity
import com.lmt.global.base.presenter.wallet.RecentRecipientAdapter
import com.lmt.global.base.presenter.wallet.TransactionAdapter
import com.lmt.global.base.presenter.wallet.WalletContact
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.showAddBalanceDialog
import com.lmt.global.base.presenter.wallet.showRecipientInputDialog
import com.lmt.global.base.presenter.wallet.toWalletContact
import com.lmt.global.base.presenter.wallet.toWalletTransaction
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : IFragment<FragmentHomeBinding, HomeViewModel>() {
    private lateinit var recentAdapter: RecentRecipientAdapter
    private lateinit var latestAdapter: TransactionAdapter

    override fun provideViewModel() = viewModel<HomeViewModel>()
    override fun provideLayout() = R.layout.fragment_home
    override fun initViews() = with(viewBinding) {
        recentAdapter = RecentRecipientAdapter(::openTransferAmount)
        recentTransferList.layoutManager = LinearLayoutManager(
            requireContext(),
            LinearLayoutManager.HORIZONTAL,
            false
        )
        recentTransferList.adapter = recentAdapter

        latestAdapter = TransactionAdapter(onClick = { transaction ->
            TransactionDetailsBottomSheet.newInstance(transaction)
                .show(parentFragmentManager, "transaction-details")
        })
        latestTransactionList.adapter = latestAdapter
    }

    override fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                launch {
                    viewModel.uiState.collect { state ->
                        val balance = state.balance
                        viewBinding.balanceText.visibility = View.VISIBLE
                        viewBinding.balanceText.text = WalletMoney.formatMainBalance(balance)
                        recentAdapter.submitList(
                            state.recentRecipients.map { it.toWalletContact() }
                        )
                        val items = state.latestTransactions.map { it.toWalletTransaction() }
                        latestAdapter.submitTransactions(items)
                        viewBinding.latestEmptyState.visibility = if (items.isEmpty()) {
                            View.VISIBLE
                        } else {
                            View.GONE
                        }
                    }
                }
                launch {
                    viewModel.actionResults.collect { result ->
                        if (result is WalletActionResult.Success) {
                            Toast.makeText(
                                requireContext(),
                                R.string.balance_added,
                                Toast.LENGTH_SHORT
                            ).show()
                        }
                    }
                }
            }
        }
    }

    override fun initListeners() = with(viewBinding) {
        val openProfile = View.OnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }
        profileAvatarButton.setOnClickListener(openProfile)
        profileButton.setOnClickListener(openProfile)
        transferButton.setOnClickListener { openTransfer() }
        topUpButton.setOnClickListener {
            showAddBalanceDialog(requireContext()) { amountMinor ->
                viewModel.onState(HomeAction.AddBalance(amountMinor))
            }
        }
        addContactButton.setOnClickListener {
            showRecipientInputDialog(requireContext()) { name, avatarKey ->
                openTransferAmount(
                    WalletContact(
                        id = 0L,
                        name = name,
                        avatarKey = avatarKey
                    )
                )
            }
        }
        viewAllButton.setOnClickListener {
            (requireActivity() as HomeActivity).showTab(HomePagerAdapter.HISTORY)
        }
    }

    private fun openTransfer() {
        startActivity(Intent(requireContext(), TransferListActivity::class.java))
    }

    private fun openTransferAmount(contact: WalletContact) {
        startActivity(Intent(requireContext(), TransferAmountActivity::class.java).apply {
            putExtra(TransferAmountActivity.EXTRA_RECIPIENT_NAME, contact.name)
            putExtra(TransferAmountActivity.EXTRA_AVATAR_KEY, contact.avatarKey)
        })
    }

    companion object { fun newInstance() = HomeFragment() }
}
