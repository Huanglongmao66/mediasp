package com.mpvp.ui.screens.search

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
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
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Headphones
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Radio
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.Videocam
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mpvp.model.ImageItem
import com.mpvp.model.MediaItem
import com.mpvp.model.MediaType
import com.mpvp.model.MusicItem
import com.mpvp.model.NovelItem
import com.mpvp.model.RadioItem
import com.mpvp.model.VideoItem
import com.mpvp.ui.components.EmptyState
import com.mpvp.ui.components.NetworkImage
import com.mpvp.viewmodel.ImageViewModel
import com.mpvp.viewmodel.MusicViewModel
import com.mpvp.viewmodel.NovelViewModel
import com.mpvp.viewmodel.RadioViewModel
import com.mpvp.viewmodel.VideoListViewModel

@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun SearchScreen(
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

    var query by remember { mutableStateOf("") }
    val focusRequester = remember { FocusRequester() }
    val keyboardController = LocalSoftwareKeyboardController.current

    LaunchedEffect(Unit) {
        focusRequester.requestFocus()
    }

    val videoResults = remember(query, videoState.videos) {
        if (query.isBlank()) emptyList()
        else videoState.videos.filter { it.title.contains(query, ignoreCase = true) }
    }
    val musicResults = remember(query, musicState.items) {
        if (query.isBlank()) emptyList()
        else musicState.items.filter { it.title.contains(query, ignoreCase = true) }
    }
    val imageResults = remember(query, imageState.items) {
        if (query.isBlank()) emptyList()
        else imageState.items.filter { it.title.contains(query, ignoreCase = true) }
    }
    val novelResults = remember(query, novelState.items) {
        if (query.isBlank()) emptyList()
        else novelState.items.filter { it.title.contains(query, ignoreCase = true) }
    }
    val radioResults = remember(query, radioState.items) {
        if (query.isBlank()) emptyList()
        else radioState.items.filter { it.title.contains(query, ignoreCase = true) }
    }

    val hasResults = videoResults.isNotEmpty() || musicResults.isNotEmpty() ||
        imageResults.isNotEmpty() || novelResults.isNotEmpty() || radioResults.isNotEmpty()

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    OutlinedTextField(
                        value = query,
                        onValueChange = { query = it },
                        placeholder = { Text("搜索视频、音乐、图片、小说、电台") },
                        leadingIcon = {
                            Icon(Icons.Filled.Search, contentDescription = null)
                        },
                        trailingIcon = {
                            if (query.isNotEmpty()) {
                                IconButton(onClick = { query = "" }) {
                                    Icon(Icons.Filled.Clear, contentDescription = "清除")
                                }
                            }
                        },
                        singleLine = true,
                        keyboardOptions = KeyboardOptions(imeAction = ImeAction.Search),
                        keyboardActions = KeyboardActions(
                            onSearch = { keyboardController?.hide() }
                        ),
                        modifier = Modifier
                            .fillMaxWidth()
                            .focusRequester(focusRequester)
                    )
                },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            when {
                query.isBlank() -> {
                    EmptyState(message = "输入关键词搜索全站内容")
                }
                !hasResults -> {
                    EmptyState(message = "未找到相关内容")
                }
                else -> {
                    LazyColumn(
                        contentPadding = androidx.compose.foundation.layout.PaddingValues(16.dp),
                        verticalArrangement = Arrangement.spacedBy(8.dp)
                    ) {
                        if (videoResults.isNotEmpty()) {
                            stickyHeader {
                                SearchSectionHeader(
                                    title = "视频",
                                    icon = Icons.Filled.Videocam,
                                    count = videoResults.size
                                )
                            }
                            items(videoResults, key = { it.id }) { video ->
                                SearchResultItem(
                                    title = video.title,
                                    coverUrl = video.coverUrl,
                                    type = "视频",
                                    icon = Icons.Filled.Videocam,
                                    onClick = { onVideoClick(video) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        if (musicResults.isNotEmpty()) {
                            stickyHeader {
                                SearchSectionHeader(
                                    title = "音乐",
                                    icon = Icons.Filled.Headphones,
                                    count = musicResults.size
                                )
                            }
                            items(musicResults, key = { it.id }) { music ->
                                SearchResultItem(
                                    title = music.title,
                                    coverUrl = music.coverUrl,
                                    type = "音乐",
                                    icon = Icons.Filled.Headphones,
                                    onClick = { onMusicClick(music) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        if (imageResults.isNotEmpty()) {
                            stickyHeader {
                                SearchSectionHeader(
                                    title = "图片",
                                    icon = Icons.Filled.Image,
                                    count = imageResults.size
                                )
                            }
                            items(imageResults, key = { it.id }) { image ->
                                SearchResultItem(
                                    title = image.title,
                                    coverUrl = image.coverUrl,
                                    type = "图片",
                                    icon = Icons.Filled.Image,
                                    onClick = { onImageClick(image) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        if (novelResults.isNotEmpty()) {
                            stickyHeader {
                                SearchSectionHeader(
                                    title = "小说",
                                    icon = Icons.Filled.MenuBook,
                                    count = novelResults.size
                                )
                            }
                            items(novelResults, key = { it.id }) { novel ->
                                SearchResultItem(
                                    title = novel.title,
                                    coverUrl = novel.coverUrl,
                                    type = "小说",
                                    icon = Icons.Filled.MenuBook,
                                    onClick = { onNovelClick(novel) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }

                        if (radioResults.isNotEmpty()) {
                            stickyHeader {
                                SearchSectionHeader(
                                    title = "电台",
                                    icon = Icons.Filled.Radio,
                                    count = radioResults.size
                                )
                            }
                            items(radioResults, key = { it.id }) { radio ->
                                SearchResultItem(
                                    title = radio.title,
                                    coverUrl = radio.coverUrl,
                                    type = "电台",
                                    icon = Icons.Filled.Radio,
                                    onClick = { onRadioClick(radio) }
                                )
                            }
                            item { Spacer(modifier = Modifier.height(8.dp)) }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun SearchSectionHeader(
    title: String,
    icon: ImageVector,
    count: Int
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.width(20.dp)
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = title,
            style = MaterialTheme.typography.titleMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
        Spacer(modifier = Modifier.width(8.dp))
        Text(
            text = "($count)",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}

@Composable
private fun SearchResultItem(
    title: String,
    coverUrl: String?,
    type: String,
    icon: ImageVector,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(8.dp))
            .clickable { onClick() }
            .padding(8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .width(56.dp)
                .height(40.dp)
                .clip(RoundedCornerShape(6.dp))
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
                text = type,
                style = MaterialTheme.typography.labelSmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}
