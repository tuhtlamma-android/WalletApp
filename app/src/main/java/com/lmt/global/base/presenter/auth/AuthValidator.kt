package com.lmt.global.base.presenter.auth

internal object AuthValidator {
    private val emailPattern = Regex(
        pattern = "^[A-Z0-9_%+-]+(?:\\.[A-Z0-9_%+-]+)*@" +
            "[A-Z0-9](?:[A-Z0-9-]*[A-Z0-9])?" +
            "(?:\\.[A-Z0-9](?:[A-Z0-9-]*[A-Z0-9])?)*\\.com$",
        option = RegexOption.IGNORE_CASE
    )

    fun isEmailValid(email: String): Boolean = emailPattern.matches(email.trim())

    fun isPhoneValid(phone: String): Boolean = PHONE_PATTERN.matches(
        phone.trim().filterNot { it.isWhitespace() || it == '-' || it == '(' || it == ')' }
    )

    fun isIdentifierValid(identifier: String): Boolean =
        isEmailValid(identifier) || isPhoneValid(identifier)

    fun isPasswordValid(password: String): Boolean = password.length >= MIN_PASSWORD_LENGTH

    const val MIN_PASSWORD_LENGTH = 6
    private val PHONE_PATTERN = Regex("^\\+[1-9][0-9]{7,14}$")
}
