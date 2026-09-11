package com.lmt.global.base.di

import android.app.Application
import android.content.Context
import android.content.Context.MODE_PRIVATE
import android.content.SharedPreferences
import androidx.fragment.app.FragmentActivity
import androidx.room.Room
import com.lmt.global.base.common.CommonViewModel
import com.lmt.global.base.data.AppDatabase
import com.lmt.global.base.data.DatabaseMigrations
import com.lmt.global.base.data.repository.WalletRepository
import com.lmt.global.base.data.repository.WalletRepositoryImpl
import com.lmt.global.base.data.repository.AuthRepository
import com.lmt.global.base.data.repository.AuthRepositoryImpl
import com.lmt.global.base.data.security.PasswordHasher
import com.lmt.global.base.data.security.Pbkdf2PasswordHasher
import com.lmt.global.base.data.session.DataStoreSessionManager
import com.lmt.global.base.data.session.SessionManager
import com.lmt.global.base.helper.firebase.RemoteConfigManagement
import com.lmt.global.base.helper.permission.IPermission
import com.lmt.global.base.helper.permission.PermissionImpl
import com.lmt.global.base.helper.preferences.AppSharedPreferences
import com.lmt.global.base.presenter.bill.BillViewModel
import com.lmt.global.base.presenter.cards.AddCardViewModel
import com.lmt.global.base.presenter.cards.CardsViewModel
import com.lmt.global.base.presenter.history.HistoryViewModel
import com.lmt.global.base.presenter.home.HomeViewModel
import com.lmt.global.base.presenter.transfer.TransferViewModel
import com.lmt.global.base.presenter.auth.LoginViewModel
import com.lmt.global.base.presenter.auth.RegisterViewModel
import com.lmt.global.base.presenter.more.MoreViewModel
import com.lmt.global.base.presenter.splash.SplashViewModel
import com.google.firebase.Firebase
import com.google.firebase.remoteconfig.remoteConfig
import org.koin.core.module.Module
import org.koin.core.module.dsl.singleOf
import org.koin.core.module.dsl.viewModelOf
import org.koin.core.qualifier.named
import org.koin.dsl.module

interface Feature {
    fun name(): String
    fun diModule(): Module
}

fun allFeature() = listOf(
    coreFeature(), viewModels(), databaseFeature()
)

private fun coreFeature() = object : Feature {
    override fun name() = "core"
    override fun diModule() = module {
        single<Context> { get<Application>() }
        single<SharedPreferences>(named("app_prefs")) {
            provideSharedPreferences(get(), "shared-preferences-coinidentifier")
        }
        factory<IPermission> { (activity: FragmentActivity) ->
            PermissionImpl.with(activity)
        }
        // singleOf(::provideRemoteConfig)
        single { AppSharedPreferences(get<SharedPreferences>(named("app_prefs"))) }
        single<SessionManager> { DataStoreSessionManager(get()) }
        single<PasswordHasher> { Pbkdf2PasswordHasher() }
        singleOf(::RemoteConfigManagement)
    }

    private fun provideSharedPreferences(context: Context, prefName: String): SharedPreferences {
        return context.getSharedPreferences(prefName, MODE_PRIVATE)
    }

    private fun provideRemoteConfig() = Firebase.remoteConfig
}

private fun databaseFeature() = object : Feature {
    override fun name() = "database"
    override fun diModule() = module {
        single {
            Room.databaseBuilder(get(), AppDatabase::class.java, AppDatabase.DATABASE_NAME)
                .addMigrations(
                    DatabaseMigrations.MIGRATION_2_3,
                    DatabaseMigrations.MIGRATION_3_4
                )
                .build()
        }
        single { get<AppDatabase>().walletDao() }
        single { get<AppDatabase>().accountDao() }
        single<WalletRepository> {
            WalletRepositoryImpl(database = get(), walletDao = get())
        }
        single<AuthRepository> {
            AuthRepositoryImpl(database = get(), accountDao = get(), passwordHasher = get())
        }
    }
}

private fun viewModels() = object : Feature {
    override fun name() = "viewmodel"
    override fun diModule() = module {
        viewModelOf(::CommonViewModel)
        viewModelOf(::HomeViewModel)
        viewModelOf(::CardsViewModel)
        viewModelOf(::AddCardViewModel)
        viewModelOf(::HistoryViewModel)
        viewModelOf(::TransferViewModel)
        viewModelOf(::BillViewModel)
        viewModelOf(::LoginViewModel)
        viewModelOf(::RegisterViewModel)
        viewModelOf(::SplashViewModel)
        viewModelOf(::MoreViewModel)
    }
}
