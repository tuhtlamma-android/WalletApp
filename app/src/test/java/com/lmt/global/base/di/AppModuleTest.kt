package com.lmt.global.base.di

import android.app.Application
import androidx.fragment.app.FragmentActivity
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

class AppModuleTest {

    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun modules_haveResolvableConstructorDependencies() {
        module {
            includes(allFeature().map(Feature::diModule))
        }.verify(
            extraTypes = listOf(Application::class, FragmentActivity::class)
        )
    }
}
