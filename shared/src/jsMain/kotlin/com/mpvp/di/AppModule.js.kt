package com.mpvp.di

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
        // Web平台使用JsSettings，需要引入multiplatform-settings-js
        // 这里暂时留空，实际项目中需要根据具体依赖配置
        com.russhwolf.settings.Settings()
    }

    // 包含通用模块
    includes(networkModule, repositoryModule, viewModelModule)
}
