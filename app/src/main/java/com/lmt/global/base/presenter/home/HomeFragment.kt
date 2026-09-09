package com.lmt.global.base.presenter.home

import android.content.Intent
import android.view.View
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IFragment
import com.lmt.global.base.databinding.FragmentHomeBinding
import com.lmt.global.base.presenter.profile.ProfileActivity
import com.lmt.global.base.presenter.history.TransactionDetailsBottomSheet
import com.lmt.global.base.presenter.transfer.TransferAmountActivity
import com.lmt.global.base.presenter.transfer.TransferListActivity
import com.lmt.global.base.presenter.wallet.RecentRecipientAdapter
import com.lmt.global.base.presenter.wallet.TransactionAdapter
import com.lmt.global.base.presenter.wallet.WalletContact
import com.lmt.global.base.presenter.wallet.WalletMoney
import com.lmt.global.base.presenter.wallet.WalletViewModel
import com.lmt.global.base.presenter.wallet.showAddBalanceDialog
import com.lmt.global.base.presenter.wallet.showRecipientInputDialog
import com.lmt.global.base.presenter.wallet.toWalletContact
import com.lmt.global.base.presenter.wallet.toWalletTransaction
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class HomeFragment : IFragment<FragmentHomeBinding, CommonViewModel>() {
    private val walletViewModel by viewModel<WalletViewModel>()
    private lateinit var recentAdapter: RecentRecipientAdapter
    private lateinit var latestAdapter: TransactionAdapter

    override fun provideViewModel() = viewModel<CommonViewModel>()
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
                    walletViewModel.balance.collect { balance ->
                        viewBinding.balanceText.visibility =
                            if (balance == null) View.INVISIBLE else View.VISIBLE
                        viewBinding.balanceText.text =
                            balance?.let(WalletMoney::formatMainBalance) ?: ""
                    }
                }
                launch {
                    walletViewModel.recentRecipients.collect { recipients ->
                        recentAdapter.submitList(recipients.map { it.toWalletContact() })
                    }
                }
                launch {
                    walletViewModel.latestTransactions.collect { transactions ->
                        val items = transactions.map { it.toWalletTransaction() }
                        latestAdapter.submitTransactions(items)
                        viewBinding.latestEmptyState.visibility =
                            if (items.isEmpty()) View.VISIBLE else View.GONE
                    }
                }
            }
        }
    }

    override fun initListeners() = with(viewBinding) {
        val openProfile = android.view.View.OnClickListener {
            startActivity(Intent(requireContext(), ProfileActivity::class.java))
        }
        profileAvatarButton.setOnClickListener(openProfile)
        profileButton.setOnClickListener(openProfile)
        transferButton.setOnClickListener { openTransfer() }
        topUpButton.setOnClickListener {
            showAddBalanceDialog(requireContext()) { amountMinor ->
                viewLifecycleOwner.lifecycleScope.launch {
                    walletViewModel.addBalance(amountMinor)
                    Toast.makeText(requireContext(), R.string.balance_added, Toast.LENGTH_SHORT).show()
                }
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
