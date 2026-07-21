package com.mpvp.ui.screens.image

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
import com.mpvp.model.ImageItem
import com.mpvp.ui.components.MediaGridContent
import com.mpvp.viewmodel.ImageViewModel

/**
 * 图片列表页面
 *
 * 展示图片列表，点击进入查看大图。
 *
 * @param viewModel 图片ViewModel
 * @param onImageClick 图片项点击回调
 * @param onBackClick 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ImageScreen(
    viewModel: ImageViewModel,
    onImageClick: (ImageItem) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("图片") },
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
            mediaType = com.mpvp.model.MediaType.IMAGE,
            onItemClick = onImageClick,
            onFavoriteClick = { viewModel.toggleFavorite(it.id) },
            subtitle = { item -> if (item.width > 0) "${item.width}×${item.height}" else "" },
            onRetry = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
