package com.lmt.global.base.presenter.cards

import android.os.Bundle
import android.widget.Toast
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.lmt.global.base.R
import com.lmt.global.base.databinding.ActivityAddCardBinding
import com.lmt.global.base.model.Card
import com.lmt.global.base.model.WalletActionResult
import com.lmt.global.base.presenter.wallet.WalletBaseActivity
import com.lmt.global.base.presenter.wallet.WalletMoney
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class AddCardActivity : WalletBaseActivity<ActivityAddCardBinding>() {
    private val addCardViewModel by viewModel<AddCardViewModel>()

    override fun provideLayout() = R.layout.activity_add_card
    override fun initViews(savedInstanceState: Bundle?) = Unit

    override fun initObservers() {
        super.initObservers()
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                addCardViewModel.results.collect(::handleAddCardResult)
            }
        }
    }

    override fun initListeners() = with(viewBinding) {
        backButton.setOnClickListener { finish() }
        addCardButton.setOnClickListener { submitCard() }
    }

    private fun submitCard() = with(viewBinding) {
        clearErrors()
        val cardId = cardIdInput.text.toString()
        val cardName = cardNameInput.text.toString()
        val cardNumber = cardNumberInput.text.toString()
        val rawBalance = balanceInput.text.toString()

        var valid = true
        if (cardId.isBlank()) {
            cardIdInput.error = getString(R.string.card_id_required)
            valid = false
        }
        if (cardName.isBlank()) {
            cardNameInput.error = getString(R.string.card_name_required)
            valid = false
        }
        val normalizedNumber = cardNumber.filter(Char::isDigit)
        val hasInvalidNumberCharacter = cardNumber.any { !it.isDigit() && !it.isWhitespace() }
        if (cardNumber.isBlank()) {
            cardNumberInput.error = getString(R.string.card_number_required)
            valid = false
        } else if (hasInvalidNumberCharacter || normalizedNumber.length != CARD_NUMBER_LENGTH) {
            cardNumberInput.error = getString(R.string.card_number_invalid)
            valid = false
        }
        val balanceMinor = when {
            rawBalance.isBlank() -> {
                balanceInput.error = getString(R.string.initial_balance_required)
                valid = false
                null
            }
            else -> WalletMoney.parseNonNegativeToMinor(rawBalance).also {
                if (it == null) {
                    balanceInput.error = getString(R.string.initial_balance_invalid)
                    valid = false
                }
            }
        }
        if (!valid || balanceMinor == null) return@with

        addCardButton.isEnabled = false
        addCardViewModel.onState(
            AddCardAction.Submit(
                Card(
                    id = cardId,
                    name = cardName,
                    cardNumber = normalizedNumber,
                    balanceMinor = balanceMinor
                )
            )
        )
    }

    private fun handleAddCardResult(result: WalletActionResult) = with(viewBinding) {
        when (result) {
            is WalletActionResult.Success -> {
                Toast.makeText(this@AddCardActivity, R.string.card_added, Toast.LENGTH_SHORT).show()
                finish()
            }
            WalletActionResult.DuplicateCard -> {
                cardIdInput.error = getString(R.string.card_already_exists)
                addCardButton.isEnabled = true
            }
            else -> {
                Toast.makeText(
                    this@AddCardActivity,
                    R.string.invalid_transaction_message,
                    Toast.LENGTH_SHORT
                ).show()
                addCardButton.isEnabled = true
            }
        }
    }

    private fun ActivityAddCardBinding.clearErrors() {
        cardIdInput.error = null
        cardNameInput.error = null
        cardNumberInput.error = null
        balanceInput.error = null
    }

    private companion object {
        const val CARD_NUMBER_LENGTH = 16

    }
}
