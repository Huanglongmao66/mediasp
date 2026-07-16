package com.mpvp.di

import com.mpvp.repository.AppDataStore
import com.mpvp.repository.ImageRepository
import com.mpvp.repository.MusicRepository
import com.mpvp.repository.NovelRepository
import com.mpvp.repository.RadioRepository
import com.mpvp.repository.VideoRepository
import com.mpvp.utils.NetworkUtils
import com.mpvp.viewmodel.ImageViewModel
import com.mpvp.viewmodel.MusicViewModel
import com.mpvp.viewmodel.NovelViewModel
import com.mpvp.viewmodel.PlayerViewModel
import com.mpvp.viewmodel.RadioViewModel
import com.mpvp.viewmodel.SettingsViewModel
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
    factory { params ->
        SettingsViewModel(get())
    }
    // 扩展模块 ViewModel（音乐 / 图片 / 小说 / 电台）
    factory { params ->
        MusicViewModel(get())
    }
    factory { params ->
        ImageViewModel(get())
    }
    factory { params ->
        NovelViewModel(get())
    }
    factory { params ->
        RadioViewModel(get())
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
    // 扩展模块 Repository（框架阶段使用内存示例数据，后续可替换为网络源）
    single { MusicRepository() }
    single { ImageRepository() }
    single { NovelRepository() }
    single { RadioRepository() }
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
