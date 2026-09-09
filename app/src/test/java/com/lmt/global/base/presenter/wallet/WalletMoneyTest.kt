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
    }

    @Test
    fun parseToMinor_rejectsInvalidAmounts() {
        assertNull(WalletMoney.parseToMinor("0"))
        assertNull(WalletMoney.parseToMinor("-10"))
        assertNull(WalletMoney.parseToMinor("1.234"))
        assertNull(WalletMoney.parseToMinor("not money"))
    }
}
