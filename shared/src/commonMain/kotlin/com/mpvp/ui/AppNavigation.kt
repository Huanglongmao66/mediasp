package com.mpvp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import com.mpvp.model.VideoItem
import com.mpvp.ui.screens.favorite.FavoriteScreen
import com.mpvp.ui.screens.home.HomeScreen
import com.mpvp.ui.screens.local.LocalVideoScreen
import com.mpvp.ui.screens.player.PlayerScreen
import com.mpvp.viewmodel.PlayerViewModel
import com.mpvp.viewmodel.VideoListViewModel

/**
 * 应用导航状态
 */
sealed class Screen {
    object Home : Screen()
    object Local : Screen()
    object Favorite : Screen()
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

    when (val screen = currentScreen) {
        is Screen.Home -> {
            HomeScreen(
                viewModel = videoListViewModel,
                onVideoClick = { video ->
                    currentScreen = Screen.Player(video)
                },
                onAddVideoClick = {
                    currentScreen = Screen.Local
                },
                onSettingsClick = {
                    // TODO: 导航到设置页面
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
