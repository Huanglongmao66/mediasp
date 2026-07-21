package com.mpvp.ui.components

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.mpvp.model.MediaItem
import com.mpvp.model.MediaType
import com.mpvp.model.UiState

/**
 * 通用媒体网格内容
 *
 * 根据 UI 状态渲染加载/空/错误/网格列表，供各媒体模块列表页复用。
 *
 * @param state UI 状态
 * @param mediaType 媒体类型
 * @param onItemClick 列表项点击回调
 * @param onFavoriteClick 收藏点击回调
 * @param subtitle 提取副标题的函数
 * @param onRetry 重试回调（可选）
 * @param modifier 修饰符
 */
@Composable
fun <T : MediaItem> MediaGridContent(
    state: UiState<List<T>>,
    mediaType: MediaType,
    onItemClick: (T) -> Unit,
    onFavoriteClick: (T) -> Unit,
    subtitle: (T) -> String,
    onRetry: (() -> Unit)? = null,
    modifier: Modifier = Modifier
) {
    when (state) {
        is UiState.Loading -> {
            Box(
                modifier = modifier.fillMaxSize(),
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
                verticalArrangement = Arrangement.spacedBy(12.dp),
                modifier = modifier.fillMaxSize()
            ) {
                items(items = state.data, key = { it.id }) { item ->
                    MediaCard(
                        title = item.title,
                        subtitle = subtitle(item),
                        mediaType = mediaType,
                        isFavorite = item.isFavorite,
                        onClick = { onItemClick(item) },
                        onFavoriteClick = { onFavoriteClick(item) }
                    )
                }
            }
        }
        is UiState.Empty -> {
            EmptyState(
                message = "暂无${mediaType.displayName}内容",
                modifier = modifier
            )
        }
        is UiState.Error -> {
            EmptyState(
                message = "加载失败: ${state.message}",
                actionText = if (onRetry != null) "重试" else null,
                onAction = onRetry,
                modifier = modifier
            )
        }
    }
}
