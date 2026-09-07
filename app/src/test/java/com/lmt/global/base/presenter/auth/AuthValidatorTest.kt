package com.lmt.global.base.presenter.auth

import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class AuthValidatorTest {

    @Test
    fun email_requiresValidAddressEndingInDotCom() {
        assertTrue(AuthValidator.isEmailValid("user@gmail.com"))
        assertTrue(AuthValidator.isEmailValid("name.surname@company.com"))
        assertFalse(AuthValidator.isEmailValid("user@gmail"))
        assertFalse(AuthValidator.isEmailValid("user@gmail.org"))
        assertFalse(AuthValidator.isEmailValid("@gmail.com"))
        assertFalse(AuthValidator.isEmailValid("user..name@gmail.com"))
        assertFalse(AuthValidator.isEmailValid("user@domain..com"))
    }

    @Test
    fun passwordRequiresAtLeastSixCharacters() {
        assertFalse(AuthValidator.isPasswordValid("12345"))
        assertTrue(AuthValidator.isPasswordValid("123456"))
        assertTrue(AuthValidator.isPasswordValid("secure-password"))
    }
}
