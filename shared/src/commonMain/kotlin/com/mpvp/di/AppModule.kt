package com.mpvp.di

import com.mpvp.repository.AppDataStore
import com.mpvp.repository.VideoRepository
import com.mpvp.utils.NetworkUtils
import com.mpvp.viewmodel.PlayerViewModel
import com.mpvp.viewmodel.VideoListViewModel
import org.koin.core.module.Module

/**
 * 应用依赖注入模块 - 公共部分
 *
 * 定义跨平台共享的依赖注入配置
 * 各平台可以通过 expect/actual 机制提供平台特定的依赖
 */
expect val appModule: Module

/**
 * 通用 ViewModel 模块
 *
 * 提供 ViewModel 层的依赖注入
 */
val viewModelModule = Module().apply {
    factory { params ->
        VideoListViewModel(get())
    }
    factory { params ->
        PlayerViewModel(get())
    }
}

/**
 * 通用 Repository 模块
 *
 * 提供数据仓库层的依赖注入
 */
val repositoryModule = Module().apply {
    single { params ->
        VideoRepository(
            fileScanner = get(),
            httpClient = get(),
            dataStore = get()
        )
    }
    single { params ->
        AppDataStore(settings = get())
    }
}

/**
 * 通用网络模块
 *
 * 提供网络相关的依赖注入
 */
val networkModule = Module().apply {
    single {
        NetworkUtils.createHttpClient()
    }
}
