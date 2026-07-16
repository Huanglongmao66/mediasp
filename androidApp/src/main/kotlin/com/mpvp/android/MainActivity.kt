package com.mpvp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mpvp.ui.theme.AppTheme
import com.mpvp.ui.theme.ThemeMode
import com.russhwolf.settings.Settings
import com.mpvp.platform.AndroidFileScanner
import com.mpvp.repository.AppDataStore
import com.mpvp.repository.VideoRepository
import com.mpvp.utils.NetworkUtils
import com.mpvp.viewmodel.PlayerViewModel
import com.mpvp.viewmodel.VideoListViewModel
import com.mpvp.ui.AppNavigation

/**
 * Android主Activity
 *
 * 应用入口点，负责初始化和显示Compose UI
 */
class MainActivity : ComponentActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        setContent {
            AppTheme(themeMode = ThemeMode.SYSTEM) {
                App()
            }
        }
    }
}

/**
 * 应用根组件
 *
 * 初始化依赖注入，显示主界面
 */
@Composable
fun App() {
    // 初始化依赖
    val context = androidx.compose.ui.platform.LocalContext.current
    val settings = remember { Settings() }
    val dataStore = remember { AppDataStore(settings) }
    val httpClient = remember { NetworkUtils.createHttpClient() }
    val fileScanner = remember { AndroidFileScanner(context) }
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
