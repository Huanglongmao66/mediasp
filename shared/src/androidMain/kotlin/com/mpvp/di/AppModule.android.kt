package com.mpvp.di

import android.content.Context
import com.mpvp.platform.AndroidFileScanner
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android 平台依赖注入模块
 */
actual val appModule: Module = module {
    single<Context> { get() }

    single<ObservableSettings> {
        SharedPreferencesSettings(get<Context>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
    }

    single {
        AndroidFileScanner(get())
    }

    includes(networkModule, repositoryModule, viewModelModule)
}
