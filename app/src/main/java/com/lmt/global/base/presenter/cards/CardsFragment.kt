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
    override fun initListeners() {
        viewBinding.cardButton.setOnClickListener {
            startActivity(Intent(requireContext(), CardPaymentActivity::class.java))
        }
    }
    companion object { fun newInstance() = CardsFragment() }
}
