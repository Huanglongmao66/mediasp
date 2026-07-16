package com.mpvp.ui.screens.radio

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
import com.mpvp.model.RadioItem
import com.mpvp.ui.components.MediaGridContent
import com.mpvp.viewmodel.RadioViewModel

/**
 * 电台列表页面
 *
 * 展示电台列表，点击进入播放详情。
 *
 * @param viewModel 电台ViewModel
 * @param onRadioClick 电台项点击回调
 * @param onBackClick 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RadioScreen(
    viewModel: RadioViewModel,
    onRadioClick: (RadioItem) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("电台") },
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
            mediaType = com.mpvp.model.MediaType.RADIO,
            onItemClick = onRadioClick,
            onFavoriteClick = { viewModel.toggleFavorite(it.id) },
            subtitle = { item ->
                buildString {
                    if (item.frequency.isNotEmpty()) append(item.frequency)
                    if (item.category.isNotEmpty()) {
                        if (isNotEmpty()) append(" · ")
                        append(item.category)
                    }
                    if (item.isLive) {
                        if (isNotEmpty()) append(" · ")
                        append("直播")
                    }
                }
            },
            onRetry = { viewModel.refresh() },
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        )
    }
}
