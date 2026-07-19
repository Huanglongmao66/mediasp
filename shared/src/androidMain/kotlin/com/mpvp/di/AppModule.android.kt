package com.mpvp.di

import android.content.Context
import com.mpvp.platform.AndroidFileScanner
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Android 平台依赖注入模块
 *
 * 提供 Android 平台特定的依赖注入配置
 */
actual val appModule: Module = module {
    // Android Context
    single<Context> { get() }

    // 设置存储
    single<ObservableSettings> {
        SharedPreferencesSettings(get<Context>().getSharedPreferences("app_prefs", Context.MODE_PRIVATE))
    }

    // 文件扫描器
    single {
        AndroidFileScanner(get())
    }

    // 包含通用模块
    includes(networkModule, repositoryModule, viewModelModule)
}
