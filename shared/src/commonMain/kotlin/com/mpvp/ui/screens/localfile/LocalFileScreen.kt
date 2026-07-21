package com.mpvp.ui.screens.localfile

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.CheckBox
import androidx.compose.material.icons.filled.CheckBoxOutlineBlank
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.ChevronRight
import androidx.compose.material.icons.filled.Folder
import androidx.compose.material.icons.filled.FolderOpen
import androidx.compose.material.icons.filled.Home
import androidx.compose.material.icons.filled.Movie
import androidx.compose.material.icons.filled.MoreVert
import androidx.compose.material.icons.filled.Refresh
import androidx.compose.material.icons.filled.SelectAll
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material.icons.outlined.Movie
import androidx.compose.material3.AlertDialog
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.HorizontalDivider
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.LinearProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Snackbar
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import com.mpvp.model.UiState
import com.mpvp.platform.FileInfo
import com.mpvp.ui.components.BreatheAnimation
import com.mpvp.ui.components.EmptyState
import com.mpvp.ui.components.ListItemAnimated
import com.mpvp.ui.components.PressableButton
import com.mpvp.utils.FileUtils
import com.mpvp.utils.TimeFormatter
import com.mpvp.viewmodel.LocalFileViewModel
import kotlinx.coroutines.launch

/**
 * 本地文件管理页面
 *
 * 提供浏览本地目录、选择视频文件、导入到视频库等功能
 *
 * @param viewModel 本地文件ViewModel
 * @param onBackClick 返回回调
 * @param onImportComplete 导入完成回调
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LocalFileScreen(
    viewModel: LocalFileViewModel,
    onBackClick: () -> Unit,
    onImportComplete: (Int) -> Unit = {}
) {
    val state by viewModel.state.collectAsState()
    val scope = rememberCoroutineScope()
    val snackbarHostState = remember { SnackbarHostState() }
    var showMenu by remember { mutableStateOf(false) }

    // 监听导入错误
    LaunchedEffect(state.importProgress.errorMessage) {
        val error = state.importProgress.errorMessage
        if (error != null) {
            scope.launch {
                snackbarHostState.showSnackbar(error)
                viewModel.clearImportError()
            }
        }
    }

    // 监听导入完成
    LaunchedEffect(state.importProgress.isImporting, state.importProgress.current) {
        val progress = state.importProgress
        if (!progress.isImporting && progress.total > 0 && progress.current >= progress.total) {
            val successCount = progress.total - progress.failedCount
            onImportComplete(successCount)
            scope.launch {
                snackbarHostState.showSnackbar(
                    "导入完成：成功 $successCount 个，失败 ${progress.failedCount} 个"
                )
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("本地文件") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { viewModel.scanAllLocalVideos() }) {
                        Icon(Icons.Filled.Refresh, contentDescription = "扫描")
                    }
                    Box {
                        IconButton(onClick = { showMenu = true }) {
                            Icon(Icons.Filled.MoreVert, contentDescription = "更多")
                        }
                        DropdownMenu(
                            expanded = showMenu,
                            onDismissRequest = { showMenu = false }
                        ) {
                            DropdownMenuItem(
                                text = { Text("选择单个文件") },
                                leadingIcon = { Icon(Icons.Filled.UploadFile, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    viewModel.pickVideoFile()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("选择多个文件") },
                                leadingIcon = { Icon(Icons.Filled.SelectAll, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    viewModel.pickMultipleVideoFiles()
                                }
                            )
                            DropdownMenuItem(
                                text = { Text("选择目录") },
                                leadingIcon = { Icon(Icons.Filled.FolderOpen, contentDescription = null) },
                                onClick = {
                                    showMenu = false
                                    viewModel.pickDirectory()
                                }
                            )
                        }
                    }
                }
            )
        },
        snackbarHost = {
            SnackbarHost(snackbarHostState) { data ->
                Snackbar(snackbarData = data)
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            Column(modifier = Modifier.fillMaxSize()) {
                // 路径导航栏
                BreadcrumbBar(
                    navigationStack = state.navigationStack,
                    onNavigate = { index -> viewModel.navigateToLevel(index) }
                )

                HorizontalDivider()

                // 工具栏 - 显示选择数量
                if (state.selectedFiles.isNotEmpty()) {
                    SelectionToolbar(
                        selectedCount = state.selectedFiles.size,
                        totalVideoCount = state.fileList.count { !it.isDirectory && FileUtils.isVideoFile(it.name) },
                        onSelectAll = { viewModel.toggleSelectAll() },
                        onClearSelection = { viewModel.clearSelection() },
                        onImport = { viewModel.importSelectedFiles() }
                    )
                }

                // 导入进度条
                AnimatedVisibility(
                    visible = state.importProgress.isImporting,
                    enter = fadeIn(animationSpec = tween(durationMillis = 200)),
                    exit = fadeOut(animationSpec = tween(durationMillis = 200))
                ) {
                    ImportProgressBar(
                        current = state.importProgress.current,
                        total = state.importProgress.total,
                        failedCount = state.importProgress.failedCount
                    )
                }

                // 文件列表
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .weight(1f)
                ) {
                    when (val uiState = state.uiState) {
                        is UiState.Loading -> {
                            Box(
                                modifier = Modifier.fillMaxSize(),
                                contentAlignment = Alignment.Center
                            ) {
                                CircularProgressIndicator()
                            }
                        }
                        is UiState.Empty -> {
                            EmptyState(
                                message = "此目录为空",
                                icon = Icons.Filled.Folder
                            )
                        }
                        is UiState.Error -> {
                            EmptyState(
                                message = "加载失败: ${uiState.message}",
                                actionText = "重试",
                                onAction = { viewModel.loadDirectory(state.currentPath) }
                            )
                        }
                        is UiState.Success -> {
                            FileList(
                                files = state.fileList,
                                selectedFiles = state.selectedFiles,
                                onFileClick = { file ->
                                    if (file.isDirectory) {
                                        viewModel.enterDirectory(file.path)
                                    } else if (FileUtils.isVideoFile(file.name)) {
                                        viewModel.toggleFileSelection(file.path)
                                    }
                                },
                                onFileLongClick = { file ->
                                    if (!file.isDirectory && FileUtils.isVideoFile(file.name)) {
                                        viewModel.toggleFileSelection(file.path)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}

/**
 * 路径导航栏
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun BreadcrumbBar(
    navigationStack: List<String>,
    onNavigate: (Int) -> Unit
) {
    var sectionsVisible by remember { mutableStateOf(false) }
    LaunchedEffect(Unit) {
        sectionsVisible = true
    }

    androidx.compose.foundation.lazy.LazyRow(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.surface)
            .padding(horizontal = 8.dp, vertical = 4.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        items(navigationStack.size) { index ->
            AnimatedVisibility(
                visible = sectionsVisible,
                enter = fadeIn(animationSpec = tween(durationMillis = 200))
            ) {
                Row(
                    verticalAlignment = Alignment.CenterVertically,
                    modifier = Modifier
                        .clip(RoundedCornerShape(8.dp))
                        .clickable { onNavigate(index) }
                        .padding(horizontal = 8.dp, vertical = 6.dp)
                ) {
                    if (index == 0) {
                        Icon(
                            Icons.Filled.Home,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    } else {
                        Icon(
                            Icons.Filled.Folder,
                            contentDescription = null,
                            modifier = Modifier.size(16.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                    Spacer(Modifier.width(4.dp))
                    Text(
                        text = if (index == 0) "根目录" else FileUtils.getFileName(navigationStack[index]),
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.primary,
                        maxLines = 1,
                        overflow = TextOverflow.Ellipsis
                    )
                }
            }
            if (index < navigationStack.size - 1) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    modifier = Modifier.size(16.dp),
                    tint = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        }
    }
}

/**
 * 选择工具栏
 */
@Composable
private fun SelectionToolbar(
    selectedCount: Int,
    totalVideoCount: Int,
    onSelectAll: () -> Unit,
    onClearSelection: () -> Unit,
    onImport: () -> Unit
) {
    var isImportPressed by remember { mutableStateOf(false) }
    val importScale by animateFloatAsState(
        targetValue = if (isImportPressed) 0.95f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .background(MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.3f))
            .padding(horizontal = 16.dp, vertical = 8.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Text(
            text = "已选择 $selectedCount 个文件",
            style = MaterialTheme.typography.bodyMedium,
            modifier = Modifier.weight(1f)
        )

        TextButton(onClick = onSelectAll) {
            Text(if (selectedCount == totalVideoCount) "取消全选" else "全选")
        }

        Spacer(Modifier.width(4.dp))

        PressableButton(
            onClick = onImport,
            shape = RoundedCornerShape(8.dp)
        ) {
            Box(
                modifier = Modifier
                    .scale(importScale)
                    .background(MaterialTheme.colorScheme.primary, RoundedCornerShape(8.dp))
                    .padding(horizontal = 16.dp, vertical = 6.dp)
            ) {
                Text(
                    "导入",
                    color = MaterialTheme.colorScheme.onPrimary,
                    style = MaterialTheme.typography.labelLarge
                )
            }
        }
    }
}

/**
 * 导入进度条
 */
@Composable
private fun ImportProgressBar(
    current: Int,
    total: Int,
    failedCount: Int
) {
    val progress = if (total > 0) current.toFloat() / total.toFloat() else 0f

    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            CircularProgressIndicator(
                modifier = Modifier.size(16.dp),
                strokeWidth = 2.dp
            )
            Spacer(Modifier.width(8.dp))
            Text(
                "正在导入 $current / $total",
                style = MaterialTheme.typography.bodySmall,
                modifier = Modifier.weight(1f)
            )
            if (failedCount > 0) {
                Text(
                    "失败 $failedCount",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.error
                )
            }
        }
        Spacer(Modifier.height(4.dp))
        LinearProgressIndicator(
            progress = { progress },
            modifier = Modifier
                .fillMaxWidth()
                .height(4.dp)
                .clip(RoundedCornerShape(2.dp))
        )
    }
}

/**
 * 文件列表
 */
@Composable
private fun FileList(
    files: List<FileInfo>,
    selectedFiles: Set<String>,
    onFileClick: (FileInfo) -> Unit,
    onFileLongClick: (FileInfo) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        contentPadding = PaddingValues(vertical = 8.dp),
        verticalArrangement = Arrangement.spacedBy(2.dp)
    ) {
        items(files.size) { index ->
            val file = files[index]
            ListItemAnimated(
                index = index,
                delayPerItem = 30,
                initialOffsetY = 12.dp
            ) {
                FileItem(
                    file = file,
                    isSelected = selectedFiles.contains(file.path),
                    onClick = { onFileClick(file) },
                    onLongClick = { onFileLongClick(file) }
                )
            }
        }
    }
}

/**
 * 文件项
 */
@Composable
private fun FileItem(
    file: FileInfo,
    isSelected: Boolean,
    onClick: () -> Unit,
    onLongClick: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100)
    )

    val isVideo = !file.isDirectory && FileUtils.isVideoFile(file.name)

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 8.dp, vertical = 2.dp)
            .scale(scale)
            .pointerInput(file.path) {
                detectTapGestures(
                    onPress = {
                        isPressed = true
                        tryAwaitRelease()
                        isPressed = false
                    },
                    onTap = { onClick() },
                    onLongPress = { onLongClick() }
                )
            },
        colors = CardDefaults.cardColors(
            containerColor = if (isSelected) {
                MaterialTheme.colorScheme.primaryContainer.copy(alpha = 0.5f)
            } else {
                MaterialTheme.colorScheme.surface
            }
        ),
        elevation = CardDefaults.cardElevation(
            defaultElevation = if (isSelected) 4.dp else 1.dp
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // 选择框或图标
            if (isVideo) {
                Icon(
                    imageVector = if (isSelected) Icons.Filled.CheckBox else Icons.Filled.CheckBoxOutlineBlank,
                    contentDescription = if (isSelected) "已选择" else "未选择",
                    tint = if (isSelected) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Icon(
                    imageVector = if (file.isDirectory) Icons.Filled.Folder else Icons.Outlined.Movie,
                    contentDescription = null,
                    tint = if (file.isDirectory) MaterialTheme.colorScheme.primary else MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(24.dp)
                )
            }

            Spacer(Modifier.width(12.dp))

            // 文件信息
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = file.name,
                    style = MaterialTheme.typography.bodyLarge,
                    color = MaterialTheme.colorScheme.onSurface,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
                Spacer(Modifier.height(2.dp))
                Text(
                    text = getFileSubtitle(file),
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant,
                    maxLines = 1,
                    overflow = TextOverflow.Ellipsis
                )
            }

            // 右侧箭头或视频标识
            if (file.isDirectory) {
                Icon(
                    Icons.Filled.ChevronRight,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.onSurfaceVariant,
                    modifier = Modifier.size(20.dp)
                )
            } else if (isVideo) {
                Icon(
                    Icons.Filled.CheckCircle,
                    contentDescription = "视频文件",
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(20.dp)
                )
            }
        }
    }
}

/**
 * 获取文件副标题
 */
private fun getFileSubtitle(file: FileInfo): String {
    return if (file.isDirectory) {
        "目录"
    } else {
        "${TimeFormatter.formatFileSize(file.size)} · ${FileUtils.getFileExtension(file.name).uppercase()}"
    }
}