package com.lmt.global.base.model

data class Account(
    val id: Long,
    val name: String,
    val email: String?,
    val phone: String?
)

sealed interface LoginResult {
    data class Success(val account: Account) : LoginResult
    data object AccountNotFound : LoginResult
    data object WrongPassword : LoginResult
    data object InvalidInput : LoginResult
}

sealed interface RegisterResult {
    data class Success(val account: Account) : RegisterResult
    data object AlreadyExists : RegisterResult
    data object InvalidInput : RegisterResult
}

sealed interface ChangePasswordResult {
    data object Success : ChangePasswordResult
    data object AccountNotFound : ChangePasswordResult
    data object WrongCurrentPassword : ChangePasswordResult
    data object SamePassword : ChangePasswordResult
    data object InvalidInput : ChangePasswordResult
}
