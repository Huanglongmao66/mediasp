package com.mpvp.ui.screens.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.ScrollableTabRow
import androidx.compose.material3.Tab
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mpvp.model.UiState
import com.mpvp.model.VideoItem
import com.mpvp.ui.components.EmptyState
import com.mpvp.ui.components.VideoCard
import com.mpvp.viewmodel.VideoListViewModel
import com.mpvp.viewmodel.VideoTab

/**
 * 首页视频列表页面
 *
 * 展示视频列表，支持本地、收藏、历史、在线标签切换
 *
 * @param viewModel 视频列表ViewModel
 * @param onVideoClick 视频点击回调
 * @param onAddVideoClick 添加视频点击回调
 * @param onSettingsClick 设置点击回调
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
    onLocalClick: () -> Unit = {}
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("视频播放器") },
                actions = {
                    IconButton(onClick = onSearchClick) {
                        Icon(Icons.Filled.Search, contentDescription = "搜索")
                    }
                    IconButton(onClick = onSettingsClick) {
                        Icon(Icons.Filled.Settings, contentDescription = "设置")
                    }
                }
            )
        },
        floatingActionButton = {
            FloatingActionButton(onClick = onAddVideoClick) {
                Icon(Icons.Filled.Add, contentDescription = "添加视频")
            }
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            // 标签栏
            ScrollableTabRow(
                selectedTabIndex = state.currentTab.ordinal,
                edgePadding = 16.dp
            ) {
                VideoTab.values().forEach { tab ->
                    Tab(
                        selected = state.currentTab == tab,
                        onClick = {
                            when (tab) {
                                VideoTab.LOCAL -> onLocalClick()
                                VideoTab.FAVORITE -> onFavoriteClick()
                                VideoTab.HISTORY -> onHistoryClick()
                                else -> viewModel.switchTab(tab)
                            }
                        },
                        text = { Text(tab.title) }
                    )
                }
            }

            // 内容区域
            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp)
            ) {
                when (state.currentTab) {
                    VideoTab.LOCAL -> VideoGridContent(
                        state = state.uiState,
                        isScanning = state.isScanning,
                        videos = state.videos,
                        onVideoClick = onVideoClick,
                        onScanClick = { viewModel.scanLocalVideos() }
                    )
                    VideoTab.FAVORITE, VideoTab.HISTORY -> {
                        // 这些标签通过导航跳转到独立页面
                    }
                    VideoTab.ONLINE -> VideoGridContent(
                        state = state.uiState,
                        isScanning = false,
                        videos = state.videos.filter { !it.isLocalVideo() },
                        onVideoClick = onVideoClick
                    )
                }
            }
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

/**
 * 历史记录内容
 */
@Composable
private fun HistoryContent(
    history: List<com.mpvp.model.PlayHistory>,
    onVideoClick: (VideoItem) -> Unit,
    onClearHistory: () -> Unit
) {
    // 历史记录页面已独立为HistoryScreen，此函数保留供未来内联使用
}
