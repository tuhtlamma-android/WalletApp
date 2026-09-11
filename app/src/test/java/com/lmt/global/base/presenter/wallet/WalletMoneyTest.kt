package com.lmt.global.base.presenter.wallet

import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class WalletMoneyTest {
    @Test
    fun parseToMinor_acceptsWholeAndDecimalAmounts() {
        assertEquals(100_000L, WalletMoney.parseToMinor("1000"))
        assertEquals(1_050L, WalletMoney.parseToMinor("10.50"))
        assertEquals(50L, WalletMoney.parseToMinor(".5"))
        assertEquals(50_000_000L, WalletMoney.parseToMinor("500,000"))
    }

    @Test
    fun parseToMinor_rejectsInvalidAmounts() {
        assertNull(WalletMoney.parseToMinor("0"))
        assertNull(WalletMoney.parseToMinor("-10"))
        assertNull(WalletMoney.parseToMinor("1.234"))
        assertNull(WalletMoney.parseToMinor("1,2"))
        assertNull(WalletMoney.parseToMinor("not money"))
    }

    @Test
    fun parseNonNegativeToMinor_acceptsZeroForCardBalance() {
        assertEquals(0L, WalletMoney.parseNonNegativeToMinor("0"))
        assertEquals(50_000_000L, WalletMoney.parseNonNegativeToMinor("500000"))
        assertNull(WalletMoney.parseNonNegativeToMinor("-1"))
    }

    @Test
    fun format_displaysZeroAsCurrency() {
        assertEquals("$0.00", WalletMoney.format(0L))
    }

    @Test
    fun maskCardNumber_onlyExposesLastFourDigits() {
        assertEquals("**** **** **** 5678", maskCardNumber("1234567812345678"))
    }
}
