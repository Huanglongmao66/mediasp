package com.mpvp.di

import com.mpvp.platform.WebFilePicker
import com.mpvp.platform.WebFileScanner
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.StorageSettings
import org.koin.core.module.Module
import org.koin.dsl.module

/**
 * Web 平台依赖注入模块
 *
 * 提供 Web 平台特定的依赖注入配置
 */
actual val appModule: Module = module {
    // 设置存储 - Web平台使用LocalStorage
    single<ObservableSettings> {
        JsObservableSettings(StorageSettings())
    }

    // 文件扫描器
    single {
        WebFileScanner()
    }

    // 文件选择器
    single {
        WebFilePicker()
    }

    // 包含通用模块
    includes(networkModule, repositoryModule, viewModelModule)
}
