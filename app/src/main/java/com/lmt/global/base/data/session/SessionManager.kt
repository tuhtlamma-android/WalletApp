package com.lmt.global.base.data.session

import android.content.Context
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

interface SessionManager {
    val currentAccountId: Flow<Long?>
    suspend fun saveAccountId(id: Long)
    suspend fun clearSession()
}

private val Context.authSessionDataStore by preferencesDataStore(name = "auth_session")

class DataStoreSessionManager(context: Context) : SessionManager {
    private val dataStore = context.applicationContext.authSessionDataStore

    override val currentAccountId: Flow<Long?> = dataStore.data.map { preferences ->
        preferences[CURRENT_ACCOUNT_ID]
    }

    override suspend fun saveAccountId(id: Long) {
        require(id > 0L)
        dataStore.edit { it[CURRENT_ACCOUNT_ID] = id }
    }

    override suspend fun clearSession() {
        dataStore.edit { it.remove(CURRENT_ACCOUNT_ID) }
    }

    private companion object {
        val CURRENT_ACCOUNT_ID = longPreferencesKey("current_account_id")
    }
}
