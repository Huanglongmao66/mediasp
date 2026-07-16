package com.mpvp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mpvp.model.ThemeMode
import com.mpvp.model.VideoItem
import com.mpvp.ui.components.AddNetworkVideoDialog
import com.mpvp.ui.screens.favorite.FavoriteScreen
import com.mpvp.ui.screens.history.HistoryScreen
import com.mpvp.ui.screens.home.HomeScreen
import com.mpvp.ui.screens.image.ImageScreen
import com.mpvp.ui.screens.local.LocalVideoScreen
import com.mpvp.ui.screens.music.MusicScreen
import com.mpvp.ui.screens.novel.NovelScreen
import com.mpvp.ui.screens.player.PlayerScreen
import com.mpvp.ui.screens.radio.RadioScreen
import com.mpvp.ui.screens.search.SearchScreen
import com.mpvp.ui.screens.settings.SettingsScreen
import com.mpvp.ui.theme.AppTheme
import com.mpvp.viewmodel.ImageViewModel
import com.mpvp.viewmodel.MusicViewModel
import com.mpvp.viewmodel.NovelViewModel
import com.mpvp.viewmodel.PlayerViewModel
import com.mpvp.viewmodel.RadioViewModel
import com.mpvp.viewmodel.SettingsViewModel
import com.mpvp.viewmodel.VideoListViewModel

/**
 * 应用导航状态
 */
sealed class Screen {
    object Home : Screen()
    object Local : Screen()
    object Favorite : Screen()
    object History : Screen()
    object Search : Screen()
    object Settings : Screen()
    // 扩展模块页面
    object Music : Screen()
    object Image : Screen()
    object Novel : Screen()
    object Radio : Screen()
    data class Player(val video: VideoItem) : Screen()
}

/**
 * 应用导航组件
 *
 * 管理应用内页面导航，应用主题，持久化配置
 *
 * @param videoListViewModel 视频列表ViewModel
 * @param playerViewModel 播放器ViewModel
 * @param settingsViewModel 设置ViewModel
 * @param musicViewModel 音乐ViewModel
 * @param imageViewModel 图片ViewModel
 * @param novelViewModel 小说ViewModel
 * @param radioViewModel 电台ViewModel
 */
@Composable
fun AppNavigation(
    videoListViewModel: VideoListViewModel,
    playerViewModel: PlayerViewModel,
    settingsViewModel: SettingsViewModel,
    musicViewModel: MusicViewModel,
    imageViewModel: ImageViewModel,
    novelViewModel: NovelViewModel,
    radioViewModel: RadioViewModel
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showAddVideoDialog by remember { mutableStateOf(false) }
    val playerConfig by settingsViewModel.config.collectAsState()

    // 应用主题
    AppTheme(themeMode = playerConfig.themeMode) {
        // 显示添加网络视频对话框
        if (showAddVideoDialog) {
            AddNetworkVideoDialog(
                onConfirm = { url, title, coverUrl ->
                    videoListViewModel.addNetworkVideo(url, title, coverUrl)
                    showAddVideoDialog = false
                },
                onDismiss = { showAddVideoDialog = false }
            )
        }

        when (val screen = currentScreen) {
            is Screen.Home -> {
                HomeScreen(
                    viewModel = videoListViewModel,
                    onVideoClick = { video ->
                        currentScreen = Screen.Player(video)
                    },
                    onAddVideoClick = {
                        showAddVideoDialog = true
                    },
                    onSettingsClick = {
                        currentScreen = Screen.Settings
                    },
                    onHistoryClick = {
                        currentScreen = Screen.History
                    },
                    onFavoriteClick = {
                        currentScreen = Screen.Favorite
                    },
                    onSearchClick = {
                        currentScreen = Screen.Search
                    },
                    onLocalClick = {
                        currentScreen = Screen.Local
                    },
                    onMusicClick = { currentScreen = Screen.Music },
                    onImageClick = { currentScreen = Screen.Image },
                    onNovelClick = { currentScreen = Screen.Novel },
                    onRadioClick = { currentScreen = Screen.Radio }
                )
            }

            is Screen.Local -> {
                LocalVideoScreen(
                    viewModel = videoListViewModel,
                    onVideoClick = { video ->
                        currentScreen = Screen.Player(video)
                    }
                )
            }

            is Screen.Favorite -> {
                FavoriteScreen(
                    viewModel = videoListViewModel,
                    onVideoClick = { video ->
                        currentScreen = Screen.Player(video)
                    },
                    onBackClick = {
                        currentScreen = Screen.Home
                    }
                )
            }

            is Screen.History -> {
                HistoryScreen(
                    viewModel = videoListViewModel,
                    onVideoClick = { video ->
                        currentScreen = Screen.Player(video)
                    },
                    onBackClick = {
                        currentScreen = Screen.Home
                    }
                )
            }

            is Screen.Settings -> {
                SettingsScreen(
                    config = playerConfig,
                    onConfigChange = { settingsViewModel.updateConfig(it) },
                    onBackClick = {
                        currentScreen = Screen.Home
                    },
                    onClearCache = {
                        // TODO: 清除缓存
                    },
                    onClearHistory = {
                        videoListViewModel.clearHistory()
                    }
                )
            }

            is Screen.Player -> {
                PlayerScreen(
                    viewModel = playerViewModel,
                    video = screen.video,
                    onBackClick = {
                        currentScreen = Screen.Home
                    },
                    onToggleFavorite = { video ->
                        videoListViewModel.toggleFavorite(video)
                    }
                )
            }

            is Screen.Search -> {
                SearchScreen(
                    viewModel = videoListViewModel,
                    onVideoClick = { video ->
                        currentScreen = Screen.Player(video)
                    },
                    onBackClick = {
                        currentScreen = Screen.Home
                    }
                )
            }

            // 扩展模块页面路由
            is Screen.Music -> {
                MusicScreen(
                    viewModel = musicViewModel,
                    onMusicClick = {
                        // TODO: 接入音乐播放详情页
                    },
                    onBackClick = { currentScreen = Screen.Home }
                )
            }

            is Screen.Image -> {
                ImageScreen(
                    viewModel = imageViewModel,
                    onImageClick = {
                        // TODO: 接入图片大图查看页
                    },
                    onBackClick = { currentScreen = Screen.Home }
                )
            }

            is Screen.Novel -> {
                NovelScreen(
                    viewModel = novelViewModel,
                    onNovelClick = {
                        // TODO: 接入小说阅读详情页
                    },
                    onBackClick = { currentScreen = Screen.Home }
                )
            }

            is Screen.Radio -> {
                RadioScreen(
                    viewModel = radioViewModel,
                    onRadioClick = {
                        // TODO: 接入电台播放详情页
                    },
                    onBackClick = { currentScreen = Screen.Home }
                )
            }
        }
    }
}
