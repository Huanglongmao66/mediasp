package com.mpvp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mpvp.model.PlayerConfig
import com.mpvp.model.VideoItem
import com.mpvp.ui.components.AddNetworkVideoDialog
import com.mpvp.ui.screens.favorite.FavoriteScreen
import com.mpvp.ui.screens.home.HomeScreen
import com.mpvp.ui.screens.local.LocalVideoScreen
import com.mpvp.ui.screens.player.PlayerScreen
import com.mpvp.ui.screens.settings.SettingsScreen
import com.mpvp.viewmodel.PlayerViewModel
import com.mpvp.viewmodel.VideoListViewModel

/**
 * 应用导航状态
 */
sealed class Screen {
    object Home : Screen()
    object Local : Screen()
    object Favorite : Screen()
    object Settings : Screen()
    data class Player(val video: VideoItem) : Screen()
}

/**
 * 应用导航组件
 *
 * 管理应用内页面导航
 *
 * @param videoListViewModel 视频列表ViewModel
 * @param playerViewModel 播放器ViewModel
 */
@Composable
fun AppNavigation(
    videoListViewModel: VideoListViewModel,
    playerViewModel: PlayerViewModel
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showAddVideoDialog by remember { mutableStateOf(false) }
    var playerConfig by remember { mutableStateOf(PlayerConfig()) }

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
                }
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

        is Screen.Settings -> {
            SettingsScreen(
                config = playerConfig,
                onConfigChange = { playerConfig = it },
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
    }
}
