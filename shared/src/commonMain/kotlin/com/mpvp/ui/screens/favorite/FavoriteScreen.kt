package com.mpvp.ui.screens.favorite

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
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
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.unit.dp
import com.mpvp.model.ImageItem
import com.mpvp.model.MusicItem
import com.mpvp.model.NovelItem
import com.mpvp.model.RadioItem
import com.mpvp.model.VideoItem
import com.mpvp.ui.components.EmptyState
import com.mpvp.ui.components.NetworkImage
import com.mpvp.ui.components.VideoCard
import com.mpvp.viewmodel.ImageViewModel
import com.mpvp.viewmodel.MusicViewModel
import com.mpvp.viewmodel.NovelViewModel
import com.mpvp.viewmodel.RadioViewModel
import com.mpvp.viewmodel.VideoListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun FavoriteScreen(
    videoListViewModel: VideoListViewModel,
    musicViewModel: MusicViewModel,
    imageViewModel: ImageViewModel,
    novelViewModel: NovelViewModel,
    radioViewModel: RadioViewModel,
    onVideoClick: (VideoItem) -> Unit,
    onMusicClick: (MusicItem) -> Unit,
    onImageClick: (ImageItem) -> Unit,
    onNovelClick: (NovelItem) -> Unit,
    onRadioClick: (RadioItem) -> Unit,
    onBackClick: () -> Unit
) {
    val videoState by videoListViewModel.state.collectAsState()
    val musicState by musicViewModel.state.collectAsState()
    val imageState by imageViewModel.state.collectAsState()
    val novelState by novelViewModel.state.collectAsState()
    val radioState by radioViewModel.state.collectAsState()

    var selectedTab by remember { mutableStateOf(0) }

    val tabs = listOf("视频", "音乐", "图片", "小说", "电台")

    val totalFavorites = videoState.favorites.size + musicState.favorites.size +
        imageState.favorites.size + novelState.favorites.size + radioState.favorites.size

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("我的收藏") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            if (totalFavorites == 0) {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    EmptyState(message = "暂无收藏内容")
                }
            } else {
                TabRow(
                    selectedTabIndex = selectedTab,
                    containerColor = MaterialTheme.colorScheme.surface
                ) {
                    tabs.forEachIndexed { index, title ->
                        val count = when (index) {
                            0 -> videoState.favorites.size
                            1 -> musicState.favorites.size
                            2 -> imageState.favorites.size
                            3 -> novelState.favorites.size
                            4 -> radioState.favorites.size
                            else -> 0
                        }
                        Tab(
                            selected = selectedTab == index,
                            onClick = { selectedTab = index },
                            text = {
                                Text("$title($count)")
                            }
                        )
                    }
                }

                Box(modifier = Modifier.fillMaxSize()) {
                    when (selectedTab) {
                        0 -> {
                            if (videoState.favorites.isEmpty()) {
                                EmptyState(message = "暂无收藏视频")
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 160.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(12.dp),
                                    verticalArrangement = Arrangement.spacedBy(12.dp)
                                ) {
                                    items(
                                        items = videoState.favorites,
                                        key = { it.id }
                                    ) { video ->
                                        VideoCard(
                                            video = video,
                                            onClick = { onVideoClick(video) }
                                        )
                                    }
                                }
                            }
                        }
                        1 -> {
                            if (musicState.favorites.isEmpty()) {
                                EmptyState(message = "暂无收藏音乐")
                            } else {
                                LazyColumn(
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = musicState.favorites,
                                        key = { it.id }
                                    ) { music ->
                                        FavoriteItem(
                                            title = music.title,
                                            subtitle = music.artist,
                                            coverUrl = music.coverUrl,
                                            icon = Icons.Filled.Headphones,
                                            onClick = { onMusicClick(music) }
                                        )
                                    }
                                }
                            }
                        }
                        2 -> {
                            if (imageState.favorites.isEmpty()) {
                                EmptyState(message = "暂无收藏图片")
                            } else {
                                LazyVerticalGrid(
                                    columns = GridCells.Adaptive(minSize = 120.dp),
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                    horizontalArrangement = Arrangement.spacedBy(8.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = imageState.favorites,
                                        key = { it.id }
                                    ) { image ->
                                        Box(
                                            modifier = Modifier
                                                .fillMaxWidth()
                                                .aspectRatio(1f)
                                                .clip(RoundedCornerShape(8.dp))
                                                .clickable { onImageClick(image) }
                                                .background(MaterialTheme.colorScheme.surfaceVariant),
                                            contentAlignment = Alignment.Center
                                        ) {
                                            if (image.coverUrl != null) {
                                                NetworkImage(
                                                    url = image.coverUrl,
                                                    contentDescription = image.title,
                                                    contentScale = ContentScale.Crop,
                                                    modifier = Modifier.fillMaxSize()
                                                )
                                            } else {
                                                Icon(
                                                    imageVector = Icons.Filled.Image,
                                                    contentDescription = null,
                                                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                                                )
                                            }
                                        }
                                    }
                                }
                            }
                        }
                        3 -> {
                            if (novelState.favorites.isEmpty()) {
                                EmptyState(message = "暂无收藏小说")
                            } else {
                                LazyColumn(
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = novelState.favorites,
                                        key = { it.id }
                                    ) { novel ->
                                        FavoriteItem(
                                            title = novel.title,
                                            subtitle = novel.author,
                                            coverUrl = novel.coverUrl,
                                            icon = Icons.Filled.MenuBook,
                                            onClick = { onNovelClick(novel) }
                                        )
                                    }
                                }
                            }
                        }
                        4 -> {
                            if (radioState.favorites.isEmpty()) {
                                EmptyState(message = "暂无收藏电台")
                            } else {
                                LazyColumn(
                                    contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                                    verticalArrangement = Arrangement.spacedBy(8.dp)
                                ) {
                                    items(
                                        items = radioState.favorites,
                                        key = { it.id }
                                    ) { radio ->
                                        FavoriteItem(
                                            title = radio.title,
                                            subtitle = radio.frequency,
                                            coverUrl = radio.coverUrl,
                                            icon = Icons.Filled.Radio,
                                            onClick = { onRadioClick(radio) }
                                        )
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun FavoriteItem(
    title: String,
    subtitle: String,
    coverUrl: String?,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(48.dp)
                .height(48.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant),
            contentAlignment = Alignment.Center
        ) {
            if (coverUrl != null) {
                NetworkImage(
                    url = coverUrl,
                    contentDescription = title,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Icon(
                    imageVector = icon,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
        Spacer(modifier = Modifier.width(12.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 1
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
                text = subtitle,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant,
                maxLines = 1
            )
        }
    }
}
