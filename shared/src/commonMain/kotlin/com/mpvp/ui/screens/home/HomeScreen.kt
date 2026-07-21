package com.mpvp.ui.screens.home

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Apps
import androidx.compose.material.icons.filled.Book
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Download
import androidx.compose.material.icons.filled.Favorite
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MusicNote
import androidx.compose.material.icons.filled.QueueMusic
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.filled.VideoLibrary
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.ExtendedFloatingActionButton
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.TransformOrigin
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mpvp.model.UiState
import com.mpvp.model.VideoItem
import com.mpvp.ui.components.EmptyState
import com.mpvp.ui.components.VideoCard
import com.mpvp.viewmodel.VideoListViewModel
import com.mpvp.viewmodel.VideoTab

/**
 * 底部导航项
 */
enum class BottomNavItem(val label: String, val icon: ImageVector) {
    LOCAL("本地", Icons.Filled.VideoLibrary),
    FAVORITE("收藏", Icons.Filled.Favorite),
    HISTORY("历史", Icons.Filled.History),
    ONLINE("在线", Icons.Filled.Search)
}

/**
 * 扩展模块菜单项
 */
enum class ExtensionMenuItem(val label: String, val icon: ImageVector) {
    MUSIC("音乐", Icons.Filled.MusicNote),
    IMAGE("图片", Icons.Filled.Image),
    NOVEL("小说", Icons.Filled.Book),
    RADIO("电台", Icons.Filled.Radio),
    PLAYLIST("播放列表", Icons.Filled.QueueMusic),
    DOWNLOAD("下载管理", Icons.Filled.Download)
}

/**
 * 首页视频列表页面
 *
 * 展示视频列表，支持本地、收藏、历史、在线标签切换
 * 底部导航栏快速切换页面，顶部"扩展"菜单进入音乐/图片/小说/电台模块
 *
 * @param viewModel 视频列表ViewModel
 * @param onVideoClick 视频点击回调
 * @param onAddVideoClick 添加视频点击回调
 * @param onSettingsClick 设置点击回调
 * @param onHistoryClick 历史页跳转
 * @param onFavoriteClick 收藏页跳转
 * @param onSearchClick 搜索页跳转
 * @param onLocalClick 本地扫描页跳转
 * @param onMusicClick 音乐模块跳转
 * @param onImageClick 图片模块跳转
 * @param onNovelClick 小说模块跳转
 * @param onRadioClick 电台模块跳转
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun HomeScreen(
    viewModel: VideoListViewModel,
    onVideoClick: (VideoItem) -> Unit,
    onAddVideoClick: () -> Unit,
    onSettingsClick: () -> Unit,
    onHistoryClick: () -> Unit,
    onFavoriteClick: () -> Unit,
    onSearchClick: () -> Unit,
    onLocalClick: () -> Unit = {},
    onLocalFileClick: () -> Unit = {},
    onMusicClick: () -> Unit = {},
    onImageClick: () -> Unit = {},
    onNovelClick: () -> Unit = {},
    onRadioClick: () -> Unit = {},
    onPlaylistClick: () -> Unit = {},
    onDownloadClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    var showExtensionMenu by remember { mutableStateOf(false) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("视频播放器") },
                actions = {
                    // 扩展模块入口（音乐/图片/小说/电台）
                    Box {
                        IconButton(onClick = { showExtensionMenu = true }) {
                            Icon(Icons.Filled.Apps, contentDescription = "扩展模块")
                        }
                        DropdownMenu(
                            expanded = showExtensionMenu,
                            onDismissRequest = { showExtensionMenu = false }
                        ) {
                            ExtensionMenuItem.entries.forEach { item ->
                                DropdownMenuItem(
                                    text = { Text(item.label) },
                                    leadingIcon = { Icon(item.icon, contentDescription = null) },
                                    onClick = {
                                        showExtensionMenu = false
                                        when (item) {
                                            ExtensionMenuItem.MUSIC -> onMusicClick()
                                            ExtensionMenuItem.IMAGE -> onImageClick()
                                            ExtensionMenuItem.NOVEL -> onNovelClick()
                                            ExtensionMenuItem.RADIO -> onRadioClick()
                                            ExtensionMenuItem.PLAYLIST -> onPlaylistClick()
                                            ExtensionMenuItem.DOWNLOAD -> onDownloadClick()
                                        }
                                    }
                                )
                            }
                        }
                    }
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        bottomBar = {
            NavigationBar {
                BottomNavItem.entries.forEach { item ->
                    NavigationBarItem(
                        selected = state.currentTab.name == item.name,
                        onClick = {
                            when (item) {
                                BottomNavItem.LOCAL -> onLocalClick()
                                BottomNavItem.FAVORITE -> onFavoriteClick()
                                BottomNavItem.HISTORY -> onHistoryClick()
                                BottomNavItem.ONLINE -> viewModel.switchTab(VideoTab.ONLINE)
                            }
                        },
                        icon = { Icon(item.icon, contentDescription = item.label) },
                        label = { Text(item.label) }
                    )
                }
            }
        },
        floatingActionButton = {
            var fabExpanded by remember { mutableStateOf(false) }
            Column(horizontalAlignment = Alignment.End) {
                AnimatedVisibility(
                    visible = fabExpanded,
                    enter = fadeIn(animationSpec = tween(durationMillis = 200)) + scaleIn(
                        initialScale = 0.5f,
                        animationSpec = tween(durationMillis = 200),
                        transformOrigin = TransformOrigin.Center
                    ),
                    exit = fadeOut(animationSpec = tween(durationMillis = 150)) + scaleOut(
                        targetScale = 0.5f,
                        animationSpec = tween(durationMillis = 150),
                        transformOrigin = TransformOrigin.Center
                    )
                ) {
                    ExtendedFloatingActionButton(
                        onClick = {
                            fabExpanded = false
                            onLocalFileClick()
                        },
                        icon = { Icon(Icons.Filled.UploadFile, contentDescription = null) },
                        text = { Text("本地文件") },
                        modifier = Modifier.padding(bottom = 8.dp)
                    )
                }
                FloatingActionButton(onClick = { fabExpanded = !fabExpanded }) {
                    AnimatedContent(
                        targetState = fabExpanded,
                        transitionSpec = {
                            (scaleIn(animationSpec = tween(durationMillis = 150)) togetherWith
                                scaleOut(animationSpec = tween(durationMillis = 150)))
                        },
                        label = "fab_icon"
                    ) { expanded ->
                        Icon(
                            imageVector = if (expanded) Icons.Filled.Close else Icons.Filled.Add,
                            contentDescription = if (expanded) "关闭" else "添加"
                        )
                    }
                }
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp)
        ) {
            VideoGridContent(
                state = state.uiState,
                isScanning = state.isScanning,
                videos = if (state.currentTab == VideoTab.ONLINE) {
                    state.videos.filter { !it.isLocalVideo() }
                } else {
                    state.videos
                },
                onVideoClick = onVideoClick,
                onScanClick = { viewModel.scanLocalVideos() }
            )
        }
    }
}

/**
 * 视频网格内容
 */
@Composable
private fun VideoGridContent(
    state: UiState<List<VideoItem>>,
    isScanning: Boolean,
    videos: List<VideoItem>,
    onVideoClick: (VideoItem) -> Unit,
    onScanClick: (() -> Unit)? = null
) {
    when (state) {
        is UiState.Loading -> {
            Box(
                modifier = Modifier.fillMaxSize(),
                contentAlignment = Alignment.Center
            ) {
                CircularProgressIndicator()
            }
        }
        is UiState.Success -> {
            LazyVerticalGrid(
                columns = GridCells.Adaptive(minSize = 160.dp),
                contentPadding = PaddingValues(vertical = 16.dp),
                horizontalArrangement = Arrangement.spacedBy(12.dp),
                verticalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                items(
                    items = videos,
                    key = { it.id }
                ) { video ->
                    VideoCard(
                        video = video,
                        onClick = { onVideoClick(video) }
                    )
                }
            }
        }
        is UiState.Empty -> {
            EmptyState(
                message = if (isScanning) "正在扫描视频..." else "暂无视频",
                actionText = if (onScanClick != null) "扫描本地视频" else null,
                onAction = onScanClick
            )
        }
        is UiState.Error -> {
            EmptyState(
                message = "加载失败: ${state.message}",
                actionText = "重试",
                onAction = onScanClick
            )
        }
    }
}
