package com.mpvp.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mpvp.platform.DesktopFilePicker
import com.mpvp.platform.DesktopFileScanner
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
import com.mpvp.viewmodel.PlaylistViewModel
import com.mpvp.viewmodel.RadioViewModel
import com.mpvp.viewmodel.SettingsViewModel
import com.mpvp.viewmodel.SubscriptionViewModel
import com.mpvp.viewmodel.VideoListViewModel
import com.mpvp.viewmodel.DownloadViewModel
import com.mpvp.ui.AppNavigation
import com.mpvp.repository.DownloadManager
import com.mpvp.repository.SimpleDownloadManager
import com.russhwolf.settings.PreferencesSettings
import java.util.prefs.Preferences
import androidx.compose.ui.unit.dp
import org.koin.java.KoinJavaComponent.getKoin

/**
 * Desktop应用入口
 *
 * 启动桌面应用窗口
 */
fun main() = application {
    val windowState = rememberWindowState(
        width = 1200.dp,
        height = 800.dp
    )

    Window(
        onCloseRequest = ::exitApplication,
        title = "MultiPlatform Video Player",
        state = windowState
    ) {
        App()
    }
}

/**
 * Desktop应用根组件
 */
@Composable
fun App() {
    // 初始化依赖
    val settings = remember {
        PreferencesSettings(Preferences.userRoot().node("com.mpvp"))
    }
    val dataStore = remember { AppDataStore(settings) }
    val httpClient = remember { NetworkUtils.createHttpClient() }
    val fileScanner = remember { DesktopFileScanner() }
    val repository = remember { VideoRepository(fileScanner, httpClient, dataStore) }

    // 视频模块 ViewModel
    val videoListViewModel = remember { VideoListViewModel(repository) }
    val playerViewModel = remember { PlayerViewModel(repository) }
    val settingsViewModel = remember { SettingsViewModel(dataStore) }

    // 扩展模块 ViewModel（音乐 / 图片 / 小说 / 电台）
    val musicViewModel = remember { MusicViewModel(MusicRepository()) }
    val imageViewModel = remember { ImageViewModel(ImageRepository()) }
    val novelViewModel = remember { NovelViewModel(NovelRepository()) }
    val radioViewModel = remember { RadioViewModel(RadioRepository()) }

    // 订阅源与播放列表 ViewModel
    val subscriptionViewModel = remember { SubscriptionViewModel(dataStore) }
    val playlistViewModel = remember { PlaylistViewModel(dataStore) }

    // 下载管理 ViewModel
    val downloadManager = remember { SimpleDownloadManager() }
    val downloadViewModel = remember { DownloadViewModel(downloadManager, dataStore) }

    // 显示主界面
    AppNavigation(
        videoListViewModel = videoListViewModel,
        playerViewModel = playerViewModel,
        settingsViewModel = settingsViewModel,
        musicViewModel = musicViewModel,
        imageViewModel = imageViewModel,
        novelViewModel = novelViewModel,
        radioViewModel = radioViewModel,
        subscriptionViewModel = subscriptionViewModel,
        playlistViewModel = playlistViewModel,
        downloadViewModel = downloadViewModel
    )
}
