package com.mpvp.android

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.mpvp.player.MediaPlayerFactoryContext
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.SharedPreferencesSettings
import com.mpvp.platform.AndroidFileScanner
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
            App()
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

    // 初始化播放器工厂上下文
    androidx.compose.runtime.LaunchedEffect(Unit) {
        MediaPlayerFactoryContext.initialize(context)
    }

    val settings: ObservableSettings = remember {
        SharedPreferencesSettings(context.getSharedPreferences("app_prefs", android.content.Context.MODE_PRIVATE))
    }
    val dataStore = remember { AppDataStore(settings) }
    val httpClient = remember { NetworkUtils.createHttpClient() }
    val fileScanner = remember { AndroidFileScanner(context) }
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

    // 显示主界面
    AppNavigation(
        videoListViewModel = videoListViewModel,
        playerViewModel = playerViewModel,
        settingsViewModel = settingsViewModel,
        musicViewModel = musicViewModel,
        imageViewModel = imageViewModel,
        novelViewModel = novelViewModel,
        radioViewModel = radioViewModel
    )
}
