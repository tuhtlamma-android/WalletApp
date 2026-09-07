package com.lmt.global.base.presenter.transfer

import android.os.Bundle
import androidx.core.widget.doAfterTextChanged
import androidx.recyclerview.widget.LinearLayoutManager
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityTransferListBinding
import com.lmt.global.base.presenter.wallet.ContactAdapter
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletContact

class TransferListActivity : WalletBaseActivity<ActivityTransferListBinding>() {
    private lateinit var contactAdapter: ContactAdapter

    override fun provideLayout() = R.layout.activity_transfer_list

    override fun initViews(savedInstanceState: Bundle?) = with(viewBinding) {
        contactList.layoutManager = LinearLayoutManager(this@TransferListActivity)
        contactAdapter = ContactAdapter(contacts) { openPage(TransferAmountActivity::class.java) }
        contactList.adapter = contactAdapter
        searchInput.doAfterTextChanged { contactAdapter.filter(it?.toString().orEmpty()) }
        Unit
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        newContactButton.setOnClickListener { openPage(TransferAmountActivity::class.java) }
    }

    private val contacts by lazy {
        listOf(
            WalletContact(R.drawable.img_avatar_ali, "Ali Ahmed", "+1-300-555-0161", "Frequent contacts"),
            WalletContact(R.drawable.img_avatar_steve, "Steve Gates", "+1-300-555-0119"),
            WalletContact(R.drawable.img_avatar_ahmed, "Elon Jobs", "+1-202-555-0171"),
            WalletContact(R.drawable.img_avatar_ali, "Ali Ahmed", "+1-300-555-0161", "All contacts"),
            WalletContact(R.drawable.img_avatar_steve, "Steve Gates", "+1-300-555-0119")
        )
    }
}
