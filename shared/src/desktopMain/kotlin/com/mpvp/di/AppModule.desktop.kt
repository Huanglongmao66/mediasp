package com.mpvp.di

import com.mpvp.platform.DesktopFilePicker
import com.mpvp.platform.DesktopFileScanner
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.PreferencesSettings
import org.koin.core.module.Module
import org.koin.dsl.module
import java.util.prefs.Preferences

/**
 * Desktop 平台依赖注入模块
 *
 * 提供 Desktop 平台特定的依赖注入配置
 */
actual val appModule: Module = module {
    // 设置存储
    single<ObservableSettings> {
        PreferencesSettings(Preferences.userRoot().node("com.mpvp"))
    }

    // 文件扫描器
    single {
        DesktopFileScanner()
    }

    // 文件选择器
    single {
        DesktopFilePicker()
    }

    // 包含通用模块
    includes(networkModule, repositoryModule, viewModelModule)
}
