package com.mpvp.di

import com.mpvp.platform.WebFileScanner
import com.russhwolf.settings.Settings
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Web 平台依赖注入模块
 *
 * 提供 Web 平台特定的依赖注入配置
 */
actual val appModule: Module = module {
    // 设置存储 - Web平台使用LocalStorage
    single<Settings> {
        com.russhwolf.settings.Settings()
    }

    // 文件扫描器
    single {
        WebFileScanner()
    }

    // 包含通用模块
    includes(networkModule, repositoryModule, viewModelModule)
}
