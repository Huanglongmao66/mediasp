package com.mpvp.ui.screens.novel

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
import com.mpvp.model.NovelItem
import com.mpvp.ui.components.MediaGridContent
import com.mpvp.viewmodel.NovelViewModel

/**
 * 小说列表页面
 *
 * 展示小说列表，点击进入阅读详情。
 *
 * @param viewModel 小说ViewModel
 * @param onNovelClick 小说项点击回调
 * @param onBackClick 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NovelScreen(
    viewModel: NovelViewModel,
    onNovelClick: (NovelItem) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("小说") },
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
            mediaType = com.mpvp.model.MediaType.NOVEL,
            onItemClick = onNovelClick,
            onFavoriteClick = { viewModel.toggleFavorite(it.id) },
            subtitle = { item ->
                buildString {
                    append(item.author)
                    if (item.chapterCount > 0) append(" · ${item.chapterCount}章")
                }
            },
            onRetry = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
