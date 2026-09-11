package com.lmt.global.base.presenter.cards

import android.content.Intent
import android.view.View
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.recyclerview.widget.LinearLayoutManager
import com.lmt.global.base.R
import com.lmt.global.base.common.IFragment
import com.lmt.global.base.databinding.FragmentCardsBinding
import com.lmt.global.base.model.Card
import kotlinx.coroutines.launch
import org.koin.androidx.viewmodel.ext.android.viewModel

class CardsFragment : IFragment<FragmentCardsBinding, CardsViewModel>() {
    private lateinit var cardAdapter: CardAdapter

    override fun provideViewModel() = viewModel<CardsViewModel>()
    override fun provideLayout() = R.layout.fragment_cards
    override fun initViews() = with(viewBinding) {
        cardAdapter = CardAdapter(::openCard)
        cardList.layoutManager = LinearLayoutManager(requireContext())
        cardList.adapter = cardAdapter
    }

    override fun initObservers() {
        viewLifecycleOwner.lifecycleScope.launch {
            viewLifecycleOwner.repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect { state ->
                    val cards = state.cards
                    cardAdapter.submitList(cards)
                    viewBinding.myCardsText.text = getString(R.string.my_cards_count, cards.size)
                    viewBinding.emptyState.visibility = if (cards.isEmpty()) View.VISIBLE else View.GONE
                }
            }
        }
    }
    override fun initListeners() = with(viewBinding) {
        addCardButton.setOnClickListener {
            startActivity(Intent(requireContext(), AddCardActivity::class.java))
        }
    }

    private fun openCard(card: Card) {
        startActivity(
            Intent(requireContext(), CardPaymentActivity::class.java)
                .putExtra(CardPaymentActivity.EXTRA_CARD_ID, card.id)
        )
    }

    companion object {
        fun newInstance() = CardsFragment()
    }
}
