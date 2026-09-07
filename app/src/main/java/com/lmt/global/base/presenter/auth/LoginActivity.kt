package com.lmt.global.base.presenter.auth

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.widget.PopupMenu
import android.widget.Toast
import com.lmt.global.base.R
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.common.IActivity
import com.lmt.global.base.databinding.ActivityLoginBinding
import org.koin.androidx.viewmodel.ext.android.viewModel

class LoginActivity : IActivity<ActivityLoginBinding, CommonViewModel>() {

    override fun provideViewModel() = viewModel<CommonViewModel>()
    override fun provideLayout() = R.layout.activity_login

    private val countries = listOf(
        Country("Jordan", "+962", R.drawable.img_flag_jordan),
        Country("Vietnam", "+84", R.drawable.ic_vietnamese),
        Country("United Kingdom", "+44", R.drawable.ic_english),
        Country("Japan", "+81", R.drawable.ic_japanese),
        Country("South Korea", "+82", R.drawable.ic_korean),
        Country("Germany", "+49", R.drawable.ic_german)
    )

    override fun initViews(savedInstanceState: Bundle?) {
        selectCountry(countries.first())
    }

    override fun initListeners() = with(viewBinding) {
        countrySelector.setOnClickListener { showCountryMenu() }
        continueButton.setOnClickListener {
            if (mobileInput.text.isNullOrBlank()) {
                mobileInput.requestFocus()
                Toast.makeText(this@LoginActivity, R.string.enter_phone_error, Toast.LENGTH_SHORT).show()
            } else {
                startActivity(Intent(this@LoginActivity, PasswordActivity::class.java))
            }
        }
        createAccountButton.setOnClickListener {
            startActivity(Intent(this@LoginActivity, CreateAccountActivity::class.java))
        }
    }

    private fun showCountryMenu() {
        PopupMenu(this, viewBinding.countrySelector).apply {
            countries.forEachIndexed { index, country ->
                menu.add(0, index, index, "${country.name}  ${country.dialCode}")
            }
            setOnMenuItemClickListener { item ->
                countries.getOrNull(item.itemId)?.let(::selectCountry) != null
            }
            show()
        }
    }

    private fun selectCountry(country: Country) = with(viewBinding) {
        countryFlag.setImageResource(country.flagRes)
        countryCode.text = country.dialCode
    }

    private data class Country(
        val name: String,
        val dialCode: String,
        val flagRes: Int
    )

}
