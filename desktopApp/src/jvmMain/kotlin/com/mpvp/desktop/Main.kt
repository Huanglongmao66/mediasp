package com.mpvp.desktop

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.window.Window
import androidx.compose.ui.window.application
import androidx.compose.ui.window.rememberWindowState
import com.mpvp.platform.DesktopFilePicker
import com.mpvp.platform.DesktopFileScanner
import com.mpvp.repository.AppDataStore
import com.mpvp.repository.VideoRepository
import com.mpvp.ui.theme.AppTheme
import com.mpvp.ui.theme.ThemeMode
import com.mpvp.utils.NetworkUtils
import com.mpvp.viewmodel.PlayerViewModel
import com.mpvp.viewmodel.VideoListViewModel
import com.mpvp.ui.AppNavigation
import com.russhwolf.settings.Settings
import androidx.compose.ui.unit.dp

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
        AppTheme(themeMode = ThemeMode.SYSTEM) {
            App()
        }
    }
}

/**
 * Desktop应用根组件
 */
@Composable
fun App() {
    // 初始化依赖
    val settings = remember { Settings() }
    val dataStore = remember { AppDataStore(settings) }
    val httpClient = remember { NetworkUtils.createHttpClient() }
    val fileScanner = remember { DesktopFileScanner() }
    val repository = remember { VideoRepository(fileScanner, httpClient, dataStore) }

    // 创建ViewModel
    val videoListViewModel = remember { VideoListViewModel(repository) }
    val playerViewModel = remember { PlayerViewModel(repository) }

    // 显示主界面
    AppNavigation(
        videoListViewModel = videoListViewModel,
        playerViewModel = playerViewModel
    )
}
