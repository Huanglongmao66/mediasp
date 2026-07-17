package com.mpvp.ui.screens.music

import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.mpvp.model.MusicItem
import com.mpvp.ui.components.MediaGridContent
import com.mpvp.viewmodel.MusicViewModel

/**
 * 音乐列表页面
 *
 * 展示音乐列表，点击进入播放详情。
 *
 * @param viewModel 音乐ViewModel
 * @param onMusicClick 音乐项点击回调
 * @param onBackClick 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MusicScreen(
    viewModel: MusicViewModel,
    onMusicClick: (MusicItem) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("音乐") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        MediaGridContent(
            state = state.uiState,
            mediaType = com.mpvp.model.MediaType.MUSIC,
            onItemClick = onMusicClick,
            onFavoriteClick = { viewModel.toggleFavorite(it.id) },
            subtitle = { item -> "${item.artist}${if (item.album.isNotEmpty()) " · ${item.album}" else ""}" },
            onRetry = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
