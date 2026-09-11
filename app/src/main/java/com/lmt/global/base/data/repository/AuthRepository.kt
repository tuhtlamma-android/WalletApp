package com.lmt.global.base.data.repository

import com.lmt.global.base.model.Account
import com.lmt.global.base.model.LoginResult
import com.lmt.global.base.model.RegisterResult

interface AuthRepository {
    suspend fun login(identifier: String, password: String): LoginResult
    suspend fun register(name: String, identifier: String, password: String): RegisterResult
    suspend fun getAccount(id: Long): Account?
    suspend fun accountExists(identifier: String): Boolean
}
