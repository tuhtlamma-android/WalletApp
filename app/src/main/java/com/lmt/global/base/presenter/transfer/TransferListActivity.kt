package com.lmt.global.base.presenter.transfer

import android.os.Bundle
import android.content.Intent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityTransferListBinding
import com.lmt.global.base.presenter.wallet.ContactAdapter
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletContact
import com.lmt.global.base.presenter.wallet.showRecipientInputDialog
import com.lmt.global.base.presenter.wallet.toWalletContact
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class TransferListActivity : WalletBaseActivity<ActivityTransferListBinding>() {
    private val transferViewModel by viewModel<TransferViewModel>()
    private lateinit var contactAdapter: ContactAdapter

    override fun provideLayout() = R.layout.activity_transfer_list

    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        contactList.layoutManager = LinearLayoutManager(this@TransferListActivity)
        contactAdapter = ContactAdapter(
            onClick = ::openTransferAmount,
            onListChanged = { count -> emptyState.visibility = if (count == 0) View.VISIBLE else View.GONE }
        )
        contactList.adapter = contactAdapter
        searchInput.doAfterTextChanged { contactAdapter.filter(it?.toString().orEmpty()) }
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                transferViewModel.uiState.collect { state ->
                    contactAdapter.submitContacts(
                        state.recipients.map { it.toWalletContact() }
                    )
                }
            }
        }
        Unit
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        newContactButton.setOnClickListener {
            showRecipientInputDialog(this@TransferListActivity) { name, avatarKey ->
                openTransferAmount(WalletContact(0L, avatarKey, name))
            }
        }
    }

    private fun openTransferAmount(contact: WalletContact) {
        startActivity(Intent(this, TransferAmountActivity::class.java).apply {
            putExtra(TransferAmountActivity.EXTRA_RECIPIENT_NAME, contact.name)
            putExtra(TransferAmountActivity.EXTRA_AVATAR_KEY, contact.avatarKey)
        })
    }
}
