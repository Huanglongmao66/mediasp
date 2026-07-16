package com.mpvp.di

import com.mpvp.platform.IOSFileScanner
import com.russhwolf.settings.Settings
import com.russhwolf.settings.NSUserDefaultsSettings
import org.koin.core.module.Module
import org.koin.dsl.module
import platform.Foundation.NSUserDefaults

/**
 * iOS 平台依赖注入模块
 *
 * 提供 iOS 平台特定的依赖注入配置
 */
actual val appModule: Module = module {
    // 设置存储
    single<Settings> {
        NSUserDefaultsSettings(NSUserDefaults.standardUserDefaults)
    }

    // 文件扫描器
    single {
        IOSFileScanner()
    }

    // 包含通用模块
    includes(networkModule, repositoryModule, viewModelModule)
}
