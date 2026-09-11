package com.lmt.global.base.data

import android.content.Context
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.lmt.global.base.data.session.DataStoreSessionManager
import com.lmt.global.base.data.session.SessionManager
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.runBlocking
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith

@RunWith(AndroidJUnit4::class)
class SessionManagerTest {
    private lateinit var sessionManager: SessionManager

    @Before
    fun setUp() = runBlocking {
        val context = ApplicationProvider.getApplicationContext<Context>()
        sessionManager = DataStoreSessionManager(context)
        sessionManager.clearSession()
    }

    @After
    fun tearDown() = runBlocking { sessionManager.clearSession() }

    @Test
    fun saveRestoreAndClearAccountId() = runBlocking {
        sessionManager.saveAccountId(42L)
        assertEquals(42L, sessionManager.currentAccountId.first())

        sessionManager.clearSession()
        assertNull(sessionManager.currentAccountId.first())
    }
}
