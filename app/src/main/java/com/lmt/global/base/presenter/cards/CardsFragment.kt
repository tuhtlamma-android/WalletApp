package com.lmt.global.base.presenter.cards

import android.content.Intent
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IFragment
import com.lmt.global.base.databinding.FragmentCardsBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class CardsFragment : IFragment<FragmentCardsBinding, CommonViewModel>() {
    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.fragment_cards
    override fun initViews() = Unit
    override fun initListeners() = with(viewBinding) {
        lightCardButton.setOnClickListener { openCard(LIGHT_CARD_INDEX) }
        middleCardButton.setOnClickListener { openCard(MIDDLE_CARD_INDEX) }
        primaryCardButton.setOnClickListener { openCard(PRIMARY_CARD_INDEX) }
    }

    private fun openCard(index: Int) {
        startActivity(
            Intent(requireContext(), CardPaymentActivity::class.java)
                .putExtra(CardPaymentActivity.EXTRA_CARD_INDEX, index)
        )
    }

    companion object {
        private const val LIGHT_CARD_INDEX = 0
        private const val MIDDLE_CARD_INDEX = 1
        private const val PRIMARY_CARD_INDEX = 2

        fun newInstance() = CardsFragment()
    }
}
