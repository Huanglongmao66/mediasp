package com.mpvp.ui.screens.history

import androidx.compose.foundation.ExperimentalFoundationApi
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Clear
import androidx.compose.material.icons.filled.Close
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material.icons.filled.DeleteForever
import androidx.compose.material.icons.filled.Edit
import androidx.compose.material.icons.filled.RadioButtonUnchecked
import androidx.compose.material.icons.filled.Search
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.Sort
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
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
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalSoftwareKeyboardController
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import com.mpvp.model.PlayHistory
import com.mpvp.model.VideoItem
import com.mpvp.model.VideoSourceType
import com.mpvp.ui.components.EmptyState
import com.mpvp.ui.components.NetworkImage
import com.mpvp.utils.TimeFormatter
import com.mpvp.viewmodel.VideoListViewModel

/**
 * 历史记录排序顺序枚举
 *
 * @property label 显示标签
 */
enum class HistorySortOrder(val label: String) {
    /** 最新优先（按播放时间倒序） */
    NEWEST_FIRST("最新优先"),

    /** 最旧优先（按播放时间正序） */
    OLDEST_FIRST("最旧优先")
}

/**
 * 历史记录筛选类型
 *
 * @property label 显示标签
 * @property sourceType 对应的视频来源类型，null 表示全部
 */
enum class HistoryFilter(val label: String, val sourceType: VideoSourceType?) {
    ALL("全部", null),
    LOCAL("本地", VideoSourceType.LOCAL),
    NETWORK("网络", VideoSourceType.NETWORK),
    LIVE("直播", VideoSourceType.LIVE)
}

/**
 * 播放历史记录页面
 *
 * 展示用户的播放历史记录，支持：
 * - 按标题搜索
 * - 按来源类型筛选（全部/本地/网络/直播）
 * - 批量选择与批量删除
 * - 按播放时间排序（最新优先/最旧优先）
 * - 继续播放和单条删除
 *
 * @param viewModel 视频列表ViewModel
 * @param onVideoClick 视频点击回调
 * @param onBackClick 返回回调
 */
@OptIn(ExperimentalMaterial3Api::class, ExperimentalFoundationApi::class)
@Composable
fun HistoryScreen(
    viewModel: VideoListViewModel,
    onVideoClick: (VideoItem) -> Unit,
    onBackClick: () -> Unit
) {
    val state by viewModel.state.collectAsState()
    val listState = rememberLazyListState()
    val keyboardController = LocalSoftwareKeyboardController.current

    // 搜索关键词（标题匹配）
    var searchQuery by remember { mutableStateOf("") }
    // 选中的筛选条件
    var selectedFilter by remember { mutableStateOf(HistoryFilter.ALL) }
    // 排序顺序
    var sortOrder by remember { mutableStateOf(HistorySortOrder.NEWEST_FIRST) }
    // 是否处于批量管理模式
    var isBatchMode by remember { mutableStateOf(false) }
    // 批量模式下选中的历史记录ID集合
    var selectedIds by remember { mutableStateOf(setOf<String>()) }

    var showClearDialog by remember { mutableStateOf(false) }
    var showBatchDeleteDialog by remember { mutableStateOf(false) }

    // 处理后的展示列表：应用搜索、筛选、排序
    val displayedHistory = remember(
        state.history, searchQuery, selectedFilter, sortOrder
    ) {
        var result = state.history

        // 按标题搜索
        if (searchQuery.isNotBlank()) {
            result = result.filter {
                it.videoTitle.contains(searchQuery, ignoreCase = true)
            }
        }

        // 按来源类型筛选
        if (selectedFilter.sourceType != null) {
            result = result.filter { it.sourceType == selectedFilter.sourceType }
        }

        // 按播放时间排序
        result = when (sortOrder) {
            HistorySortOrder.NEWEST_FIRST -> result.sortedByDescending { it.playTime }
            HistorySortOrder.OLDEST_FIRST -> result.sortedBy { it.playTime }
        }

        result
    }

    // 是否所有展示项已被选中
    val allSelected = displayedHistory.isNotEmpty() &&
        selectedIds.size == displayedHistory.size &&
        displayedHistory.all { it.id in selectedIds }

    Scaffold(
        topBar = {
            TopAppBar(
                title = {
                    Text(
                        if (isBatchMode && selectedIds.isNotEmpty()) {
                            "已选择 ${selectedIds.size} 项"
                        } else {
                            "播放历史"
                        }
                    )
                },
                navigationIcon = {
                    IconButton(onClick = {
                        if (isBatchMode) {
                            // 退出批量模式并清空选择
                            isBatchMode = false
                            selectedIds = emptySet()
                        } else {
                            onBackClick()
                        }
                    }) {
                        Icon(
                            if (isBatchMode) Icons.Filled.Close
                            else Icons.AutoMirrored.Filled.ArrowBack,
                            contentDescription = if (isBatchMode) "退出批量模式" else "返回"
                        )
                    }
                },
                actions = {
                    if (isBatchMode) {
                        // 全选/取消全选按钮
                        IconButton(onClick = {
                            selectedIds = if (allSelected) {
                                emptySet()
                            } else {
                                displayedHistory.map { it.id }.toSet()
                            }
                        }) {
                            Icon(
                                Icons.Filled.SelectAll,
                                contentDescription = if (allSelected) "取消全选" else "全选"
                            )
                        }
                        // 批量删除按钮（仅当有选中项时可用）
                        if (selectedIds.isNotEmpty()) {
                            IconButton(onClick = { showBatchDeleteDialog = true }) {
                                Icon(
                                    Icons.Filled.Delete,
                                    contentDescription = "批量删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }
                    } else {
                        // 排序按钮：切换最新/最旧优先
                        IconButton(onClick = {
                            sortOrder = if (sortOrder == HistorySortOrder.NEWEST_FIRST) {
                                HistorySortOrder.OLDEST_FIRST
                            } else {
                                HistorySortOrder.NEWEST_FIRST
                            }
                        }) {
                            Icon(
                                Icons.Filled.Sort,
                                contentDescription = "排序（当前：${sortOrder.label}）"
                            )
                        }
                        // 批量管理按钮
                        if (state.history.isNotEmpty()) {
                            IconButton(onClick = { isBatchMode = true }) {
                                Icon(Icons.Filled.Edit, contentDescription = "批量管理")
                            }
                        }
                        // 清空全部历史按钮
                        if (state.history.isNotEmpty()) {
                            IconButton(onClick = { showClearDialog = true }) {
                                Icon(Icons.Filled.DeleteForever, contentDescription = "清空历史")
                            }
                        }
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
            // 搜索栏与筛选 Chips（仅在有历史数据时显示）
            if (state.history.isNotEmpty()) {
                OutlinedTextField(
                    value = searchQuery,
                    onValueChange = { searchQuery = it },
                    placeholder = { Text("搜索历史记录") },
                    leadingIcon = {
                        Icon(Icons.Filled.Search, contentDescription = null)
                    },
                    trailingIcon = {
                        if (searchQuery.isNotEmpty()) {
                            IconButton(onClick = { searchQuery = "" }) {
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
                        .padding(horizontal = 16.dp, vertical = 8.dp)
                )

                // 筛选 Chips 行
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(horizontal = 16.dp, vertical = 4.dp),
                    horizontalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    HistoryFilter.values().forEach { filter ->
                        FilterChip(
                            selected = selectedFilter == filter,
                            onClick = { selectedFilter = filter },
                            label = { Text(filter.label) }
                        )
                    }
                }
            }

            // 主列表区域
            Box(modifier = Modifier.fillMaxSize()) {
                when {
                    state.history.isEmpty() -> {
                        EmptyState(message = "暂无播放历史")
                    }
                    displayedHistory.isEmpty() -> {
                        EmptyState(message = "未找到匹配的历史记录")
                    }
                    else -> {
                        LazyColumn(
                            state = listState,
                            contentPadding = PaddingValues(16.dp),
                            verticalArrangement = Arrangement.spacedBy(12.dp)
                        ) {
                            items(
                                items = displayedHistory,
                                key = { it.id }
                            ) { history ->
                                HistoryItem(
                                    history = history,
                                    isSelected = history.id in selectedIds,
                                    isBatchMode = isBatchMode,
                                    onClick = {
                                        if (isBatchMode) {
                                            // 批量模式：切换选中状态
                                            selectedIds = if (history.id in selectedIds) {
                                                selectedIds - history.id
                                            } else {
                                                selectedIds + history.id
                                            }
                                        } else {
                                            onVideoClick(history.toVideoItem())
                                        }
                                    },
                                    onDeleteClick = { viewModel.deleteHistory(history.id) }
                                )
                            }
                        }
                    }
                }
            }
        }
    }

    // 清空全部历史确认对话框
    if (showClearDialog) {
        AlertDialog(
            title = { Text("确认清空") },
            text = { Text("确定要清空所有播放历史吗？此操作不可恢复。") },
            onDismissRequest = { showClearDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.clearHistory()
                    showClearDialog = false
                }) {
                    Text("确定")
                }
            },
            dismissButton = {
                TextButton(onClick = { showClearDialog = false }) {
                    Text("取消")
                }
            }
        )
    }

    // 批量删除确认对话框
    if (showBatchDeleteDialog) {
        AlertDialog(
            title = { Text("确认删除") },
            text = { Text("确定要删除选中的 ${selectedIds.size} 条历史记录吗？此操作不可恢复。") },
            onDismissRequest = { showBatchDeleteDialog = false },
            confirmButton = {
                TextButton(onClick = {
                    selectedIds.forEach { viewModel.deleteHistory(it) }
                    selectedIds = emptySet()
                    showBatchDeleteDialog = false
                    isBatchMode = false
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = { showBatchDeleteDialog = false }) {
                    Text("取消")
                }
            }
        )
    }
}

/**
 * 历史记录条目组件
 *
 * 在批量模式下会显示选中状态指示器，并隐藏单条删除按钮
 *
 * @param history 历史记录对象
 * @param isSelected 是否处于选中状态
 * @param isBatchMode 是否处于批量管理模式
 * @param onClick 点击回调（批量模式下切换选中状态，否则进入播放）
 * @param onDeleteClick 删除回调
 */
@Composable
private fun HistoryItem(
    history: PlayHistory,
    isSelected: Boolean,
    isBatchMode: Boolean,
    onClick: () -> Unit,
    onDeleteClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clip(RoundedCornerShape(12.dp))
            .background(
                if (isSelected) MaterialTheme.colorScheme.primaryContainer
                else MaterialTheme.colorScheme.surface
            )
            .clickable { onClick() }
            .padding(12.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        // 批量模式下的选中指示器
        if (isBatchMode) {
            Icon(
                imageVector = if (isSelected) Icons.Filled.CheckCircle
                else Icons.Filled.RadioButtonUnchecked,
                contentDescription = if (isSelected) "已选中" else "未选中",
                tint = if (isSelected) MaterialTheme.colorScheme.primary
                else MaterialTheme.colorScheme.onSurfaceVariant,
                modifier = Modifier.padding(end = 12.dp)
            )
        }

        // 封面图区域（含播放进度条）
        Box(
            modifier = Modifier
                .width(100.dp)
                .height(56.dp)
                .clip(RoundedCornerShape(8.dp))
                .background(MaterialTheme.colorScheme.surfaceVariant)
        ) {
            if (history.coverUrl != null) {
                NetworkImage(
                    url = history.coverUrl,
                    contentDescription = history.videoTitle,
                    contentScale = ContentScale.Crop,
                    modifier = Modifier.fillMaxSize()
                )
            } else {
                Box(
                    modifier = Modifier.fillMaxSize(),
                    contentAlignment = Alignment.Center
                ) {
                    Text(
                        text = history.videoTitle.take(6),
                        style = MaterialTheme.typography.labelSmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            // 播放进度条
            Box(
                modifier = Modifier
                    .align(Alignment.BottomStart)
                    .fillMaxWidth()
                    .height(2.dp)
                    .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.5f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(history.playProgress)
                        .fillMaxHeight()
                        .background(MaterialTheme.colorScheme.primary)
                )
            }
        }

        // 标题与时长信息
        Column(
            modifier = Modifier
                .weight(1f)
                .padding(start = 12.dp),
            verticalArrangement = Arrangement.Center
        ) {
            Text(
                text = history.videoTitle,
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface,
                maxLines = 2
            )
            Row(
                modifier = Modifier.padding(top = 4.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Text(
                    text = "${history.getFormattedPosition()} / ${history.getFormattedDuration()}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Text(
                    text = TimeFormatter.formatTimeAgo(history.playTime),
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }

        // 单条删除按钮（仅非批量模式显示）
        if (!isBatchMode) {
            IconButton(
                onClick = { onDeleteClick() },
                modifier = Modifier.padding(start = 8.dp)
            ) {
                Icon(
                    Icons.Filled.Delete,
                    contentDescription = "删除",
                    tint = MaterialTheme.colorScheme.error
                )
            }
        }
    }
}
