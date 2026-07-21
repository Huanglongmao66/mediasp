package com.mpvp.ui

import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import com.mpvp.model.ImageItem
import com.mpvp.model.MusicItem
import com.mpvp.model.NovelItem
import com.mpvp.model.RadioItem
import com.mpvp.model.ThemeMode
import com.mpvp.model.VideoItem
import com.mpvp.ui.components.AddNetworkVideoDialog
import com.mpvp.ui.screens.download.DownloadScreen
import com.mpvp.ui.screens.favorite.FavoriteScreen
import com.mpvp.ui.screens.history.HistoryScreen
import com.mpvp.ui.screens.home.HomeScreen
import com.mpvp.ui.screens.image.ImageScreen
import com.mpvp.ui.screens.image.ImageViewerScreen
import com.mpvp.ui.screens.local.LocalVideoScreen
import com.mpvp.ui.screens.music.MusicPlayerScreen
import com.mpvp.ui.screens.music.MusicScreen
import com.mpvp.ui.screens.novel.NovelReaderScreen
import com.mpvp.ui.screens.novel.NovelScreen
import com.mpvp.ui.screens.player.PlayerScreen
import com.mpvp.ui.screens.playlist.PlaylistDetailScreen
import com.mpvp.ui.screens.playlist.PlaylistScreen
import com.mpvp.ui.screens.radio.RadioPlayerScreen
import com.mpvp.ui.screens.radio.RadioScreen
import com.mpvp.ui.screens.search.SearchScreen
import com.mpvp.ui.screens.settings.SettingsScreen
import com.mpvp.ui.screens.subscription.SubscriptionScreen
import com.mpvp.ui.screens.plugin.PluginEditorScreen
import com.mpvp.ui.screens.plugin.PluginManagerScreen
import com.mpvp.ui.theme.AppTheme
import com.mpvp.viewmodel.DownloadViewModel
import com.mpvp.viewmodel.ImageViewModel
import com.mpvp.viewmodel.MusicViewModel
import com.mpvp.viewmodel.NovelViewModel
import com.mpvp.viewmodel.PlayerViewModel
import com.mpvp.viewmodel.PlaylistViewModel
import com.mpvp.viewmodel.RadioViewModel
import com.mpvp.viewmodel.SettingsViewModel
import com.mpvp.viewmodel.SubscriptionViewModel
import com.mpvp.viewmodel.PluginViewModel
import com.mpvp.viewmodel.VideoListViewModel
import kotlinx.coroutines.launch

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
    // 订阅源与播放列表页面
    object Subscription : Screen()
    object Playlist : Screen()
    data class PlaylistDetail(val playlistId: String) : Screen()
    // 插件管理页面
    object PluginManager : Screen()
    data class PluginEditor(val pluginId: String?) : Screen()
    // 下载管理页面
    object Download : Screen()
    // 扩展模块详情页
    data class MusicPlayer(val music: MusicItem) : Screen()
    data class ImageViewer(val image: ImageItem) : Screen()
    data class NovelReader(val novel: NovelItem) : Screen()
    data class RadioPlayer(val radio: RadioItem) : Screen()
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
    radioViewModel: RadioViewModel,
    subscriptionViewModel: SubscriptionViewModel,
    playlistViewModel: PlaylistViewModel,
    downloadViewModel: DownloadViewModel,
    pluginViewModel: PluginViewModel
) {
    var currentScreen by remember { mutableStateOf<Screen>(Screen.Home) }
    var showAddVideoDialog by remember { mutableStateOf(false) }
    val playerConfig by settingsViewModel.config.collectAsState()
    val scope = rememberCoroutineScope()

    // 应用主题
    AppTheme(themeMode = playerConfig.themeMode, themeColor = playerConfig.themeColor) {
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
                    onRadioClick = { currentScreen = Screen.Radio },
                    onPlaylistClick = { currentScreen = Screen.Playlist },
                    onDownloadClick = { currentScreen = Screen.Download }
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
                    videoListViewModel = videoListViewModel,
                    musicViewModel = musicViewModel,
                    imageViewModel = imageViewModel,
                    novelViewModel = novelViewModel,
                    radioViewModel = radioViewModel,
                    onVideoClick = { video ->
                        currentScreen = Screen.Player(video)
                    },
                    onMusicClick = { music ->
                        currentScreen = Screen.MusicPlayer(music)
                    },
                    onImageClick = { image ->
                        currentScreen = Screen.ImageViewer(image)
                    },
                    onNovelClick = { novel ->
                        currentScreen = Screen.NovelReader(novel)
                    },
                    onRadioClick = { radio ->
                        currentScreen = Screen.RadioPlayer(radio)
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
                    viewModel = settingsViewModel,
                    onBackClick = {
                        currentScreen = Screen.Home
                    },
                    onClearCache = {
                        // TODO: 清除缓存
                    },
                    onClearHistory = {
                        videoListViewModel.clearHistory()
                    },
                    onSubscriptionClick = {
                        currentScreen = Screen.Subscription
                    },
                    onPluginManagerClick = {
                        currentScreen = Screen.PluginManager
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
                    videoListViewModel = videoListViewModel,
                    musicViewModel = musicViewModel,
                    imageViewModel = imageViewModel,
                    novelViewModel = novelViewModel,
                    radioViewModel = radioViewModel,
                    onVideoClick = { video ->
                        currentScreen = Screen.Player(video)
                    },
                    onMusicClick = { music ->
                        currentScreen = Screen.MusicPlayer(music)
                    },
                    onImageClick = { image ->
                        currentScreen = Screen.ImageViewer(image)
                    },
                    onNovelClick = { novel ->
                        currentScreen = Screen.NovelReader(novel)
                    },
                    onRadioClick = { radio ->
                        currentScreen = Screen.RadioPlayer(radio)
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
                    onMusicClick = { music ->
                        currentScreen = Screen.MusicPlayer(music)
                    },
                    onBackClick = { currentScreen = Screen.Home }
                )
            }

            is Screen.Image -> {
                ImageScreen(
                    viewModel = imageViewModel,
                    onImageClick = { image ->
                        currentScreen = Screen.ImageViewer(image)
                    },
                    onBackClick = { currentScreen = Screen.Home }
                )
            }

            is Screen.Novel -> {
                NovelScreen(
                    viewModel = novelViewModel,
                    onNovelClick = { novel ->
                        currentScreen = Screen.NovelReader(novel)
                    },
                    onBackClick = { currentScreen = Screen.Home }
                )
            }

            is Screen.Radio -> {
                RadioScreen(
                    viewModel = radioViewModel,
                    onRadioClick = { radio ->
                        currentScreen = Screen.RadioPlayer(radio)
                    },
                    onBackClick = { currentScreen = Screen.Home }
                )
            }

            // 扩展模块详情页路由
            is Screen.MusicPlayer -> {
                MusicPlayerScreen(
                    music = screen.music,
                    isFavorite = screen.music.isFavorite,
                    onBackClick = { currentScreen = Screen.Music },
                    onFavoriteClick = { musicViewModel.toggleFavorite(screen.music.id) }
                )
            }

            is Screen.ImageViewer -> {
                ImageViewerScreen(
                    image = screen.image,
                    isFavorite = screen.image.isFavorite,
                    onBackClick = { currentScreen = Screen.Image },
                    onFavoriteClick = { imageViewModel.toggleFavorite(screen.image.id) }
                )
            }

            is Screen.NovelReader -> {
                NovelReaderScreen(
                    novel = screen.novel,
                    isFavorite = screen.novel.isFavorite,
                    onBackClick = { currentScreen = Screen.Novel },
                    onFavoriteClick = { novelViewModel.toggleFavorite(screen.novel.id) }
                )
            }

            is Screen.RadioPlayer -> {
                RadioPlayerScreen(
                    radio = screen.radio,
                    isFavorite = screen.radio.isFavorite,
                    onBackClick = { currentScreen = Screen.Radio },
                    onFavoriteClick = { radioViewModel.toggleFavorite(screen.radio.id) }
                )
            }

            // 订阅源与播放列表路由
            is Screen.Subscription -> {
                SubscriptionScreen(
                    viewModel = subscriptionViewModel,
                    onBackClick = { currentScreen = Screen.Settings }
                )
            }

            is Screen.Playlist -> {
                PlaylistScreen(
                    viewModel = playlistViewModel,
                    onBackClick = { currentScreen = Screen.Home },
                    onPlaylistClick = { playlist ->
                        currentScreen = Screen.PlaylistDetail(playlist.id)
                    }
                )
            }

            is Screen.PlaylistDetail -> {
                PlaylistDetailScreen(
                    playlistId = screen.playlistId,
                    viewModel = playlistViewModel,
                    onBackClick = { currentScreen = Screen.Playlist },
                    onItemClick = { item ->
                        // 根据媒体类型跳转到对应详情页
                        scope.launch {
                            when (item.mediaType) {
                                com.mpvp.model.MediaType.VIDEO -> {
                                    // 视频需从 videoListViewModel 查找
                                }
                                com.mpvp.model.MediaType.MUSIC -> {
                                    musicViewModel.getById(item.mediaId)?.let { music ->
                                        currentScreen = Screen.MusicPlayer(music)
                                    }
                                }
                                com.mpvp.model.MediaType.IMAGE -> {
                                    imageViewModel.getById(item.mediaId)?.let { image ->
                                        currentScreen = Screen.ImageViewer(image)
                                    }
                                }
                                com.mpvp.model.MediaType.NOVEL -> {
                                    novelViewModel.getById(item.mediaId)?.let { novel ->
                                        currentScreen = Screen.NovelReader(novel)
                                    }
                                }
                                com.mpvp.model.MediaType.RADIO -> {
                                    radioViewModel.getById(item.mediaId)?.let { radio ->
                                        currentScreen = Screen.RadioPlayer(radio)
                                    }
                                }
                            }
                        }
                    }
                )
            }

            // 下载管理路由
            is Screen.Download -> {
                DownloadScreen(
                    viewModel = downloadViewModel,
                    onBackClick = { currentScreen = Screen.Home }
                )
            }

            // 插件管理路由
            is Screen.PluginManager -> {
                PluginManagerScreen(
                    viewModel = pluginViewModel,
                    onBackClick = { currentScreen = Screen.Settings },
                    onEditPlugin = { plugin ->
                        currentScreen = Screen.PluginEditor(plugin?.meta?.id)
                    }
                )
            }

            is Screen.PluginEditor -> {
                val plugin = screen.pluginId?.let { id ->
                    pluginViewModel.state.value.plugins.find { it.meta.id == id }
                }
                PluginEditorScreen(
                    viewModel = pluginViewModel,
                    plugin = plugin,
                    onBackClick = { currentScreen = Screen.PluginManager },
                    onSave = { currentScreen = Screen.PluginManager }
                )
            }
        }
    }
}
