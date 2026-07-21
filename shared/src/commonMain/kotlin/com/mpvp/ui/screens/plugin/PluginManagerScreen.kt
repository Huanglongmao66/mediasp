package com.mpvp.ui.screens.plugin

import androidx.compose.animation.AnimatedVisibility
import androidx.compose.animation.animateColorAsState
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.scaleIn
import androidx.compose.animation.scaleOut
import androidx.compose.foundation.clickable
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.input.pointer.pointerInput
import androidx.compose.ui.unit.dp
import com.mpvp.model.*
import com.mpvp.ui.components.BreatheAnimation
import com.mpvp.ui.components.ListItemAnimated
import com.mpvp.ui.components.PressableButton
import com.mpvp.viewmodel.PluginViewModel
import kotlinx.coroutines.launch

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginManagerScreen(
    viewModel: PluginViewModel,
    onBackClick: () -> Unit,
    onEditPlugin: (SubscriptionPlugin?) -> Unit
) {
    val state by viewModel.state.collectAsState()
    var showImportDialog by remember { mutableStateOf(false) }
    var importJson by remember { mutableStateOf("") }
    var importError by remember { mutableStateOf("") }
    var showExportDialog by remember { mutableStateOf(false) }
    var selectedPluginId by remember { mutableStateOf<String?>(null) }
    var showDeleteDialog by remember { mutableStateOf(false) }
    val scope = rememberCoroutineScope()

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("插件管理") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = { showExportDialog = true }) {
                        Icon(Icons.Filled.Download, contentDescription = "导出")
                    }
                    IconButton(onClick = { showImportDialog = true }) {
                        Icon(Icons.Filled.Upload, contentDescription = "导入")
                    }
                }
            )
        },
        floatingActionButton = {
            var fabPressed by remember { mutableStateOf(false) }
            val fabScale by animateFloatAsState(
                targetValue = if (fabPressed) 0.9f else 1f,
                animationSpec = tween(durationMillis = 150)
            )
            FloatingActionButton(
                onClick = { onEditPlugin(null) },
                modifier = Modifier.scale(fabScale)
            ) {
                Icon(Icons.Filled.Add, contentDescription = "添加插件")
            }
            LaunchedEffect(Unit) {
                fabPressed = false
            }
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
        ) {
            AnimatedVisibility(
                visible = state.plugins.isEmpty(),
                enter = fadeIn(animationSpec = tween(durationMillis = 300)),
                exit = fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                Column(
                    modifier = Modifier.fillMaxSize(),
                    horizontalAlignment = Alignment.CenterHorizontally,
                    verticalArrangement = Arrangement.Center
                ) {
                    BreatheAnimation(
                        icon = Icons.Filled.Extension,
                        contentDescription = null,
                        size = 64.dp
                    )
                    Spacer(Modifier.height(16.dp))
                    Text(
                        "暂无插件",
                        style = MaterialTheme.typography.bodyLarge,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                    Spacer(Modifier.height(8.dp))
                    Text(
                        "点击右下角按钮创建或导入插件",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            AnimatedVisibility(
                visible = state.plugins.isNotEmpty(),
                enter = fadeIn(animationSpec = tween(durationMillis = 300)) + scaleIn(
                    initialScale = 0.98f,
                    animationSpec = tween(durationMillis = 300)
                ),
                exit = fadeOut(animationSpec = tween(durationMillis = 200))
            ) {
                LazyColumn(
                    modifier = Modifier.fillMaxSize(),
                    contentPadding = PaddingValues(16.dp),
                    verticalArrangement = Arrangement.spacedBy(12.dp)
                ) {
                    items(state.plugins, key = { it.meta.id }) { plugin ->
                        ListItemAnimated(
                            index = state.plugins.indexOf(plugin),
                            delayPerItem = 60
                        ) {
                            PluginCard(
                                plugin = plugin,
                                onEdit = { onEditPlugin(plugin) },
                                onDelete = {
                                    selectedPluginId = plugin.meta.id
                                    showDeleteDialog = true
                                },
                                onTest = {
                                    scope.launch {
                                        val config = plugin.configParams.associate { it.name to it.defaultValue }
                                        val result = viewModel.testPlugin(plugin.meta.id, config)
                                        viewModel.setTestResult(result)
                                    }
                                }
                            )
                        }
                    }
                }
            }
        }
    }

    AnimatedVisibility(
        visible = showImportDialog,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)) + scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(durationMillis = 200)
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 150)) + scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(durationMillis = 150)
        )
    ) {
        ImportPluginDialog(
            json = importJson,
            onJsonChange = {
                importJson = it
                importError = ""
            },
            onConfirm = {
                val result = viewModel.importPlugin(importJson)
                if (result.isSuccess) {
                    showImportDialog = false
                    importJson = ""
                } else {
                    importError = result.exceptionOrNull()?.message ?: "导入失败"
                }
            },
            onDismiss = {
                showImportDialog = false
                importJson = ""
                importError = ""
            },
            error = importError
        )
    }

    AnimatedVisibility(
        visible = showExportDialog,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)) + scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(durationMillis = 200)
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 150)) + scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(durationMillis = 150)
        )
    ) {
        ExportPluginDialog(
            plugins = state.plugins,
            onExportAll = {
                val json = viewModel.exportAllPlugins()
                println("Exported all plugins: $json")
                showExportDialog = false
            },
            onExportSingle = { pluginId ->
                val json = viewModel.exportPlugin(pluginId)
                println("Exported plugin: $json")
                showExportDialog = false
            },
            onDismiss = { showExportDialog = false }
        )
    }

    AnimatedVisibility(
        visible = showDeleteDialog && selectedPluginId != null,
        enter = fadeIn(animationSpec = tween(durationMillis = 200)) + scaleIn(
            initialScale = 0.9f,
            animationSpec = tween(durationMillis = 200)
        ),
        exit = fadeOut(animationSpec = tween(durationMillis = 150)) + scaleOut(
            targetScale = 0.9f,
            animationSpec = tween(durationMillis = 150)
        )
    ) {
        AlertDialog(
            onDismissRequest = { showDeleteDialog = false },
            title = { Text("确认删除") },
            text = { Text("确定要删除这个插件吗？此操作无法撤销。") },
            confirmButton = {
                TextButton(onClick = {
                    viewModel.deletePlugin(selectedPluginId!!)
                    showDeleteDialog = false
                    selectedPluginId = null
                }) {
                    Text("删除")
                }
            },
            dismissButton = {
                TextButton(onClick = {
                    showDeleteDialog = false
                    selectedPluginId = null
                }) {
                    Text("取消")
                }
            }
        )
    }
}

@Composable
private fun PluginCard(
    plugin: SubscriptionPlugin,
    onEdit: () -> Unit,
    onDelete: () -> Unit,
    onTest: () -> Unit
) {
    var isPressed by remember { mutableStateOf(false) }
    val scale by animateFloatAsState(
        targetValue = if (isPressed) 0.98f else 1f,
        animationSpec = tween(durationMillis = 100)
    )
    val elevation by animateFloatAsState(
        targetValue = if (isPressed) 6f else 2f,
        animationSpec = tween(durationMillis = 100)
    )
    
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .scale(scale),
        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surface),
        elevation = CardDefaults.cardElevation(defaultElevation = elevation.dp)
    ) {
        Column(
            modifier = Modifier.padding(16.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Icon(
                    Icons.Filled.Extension,
                    contentDescription = null,
                    tint = MaterialTheme.colorScheme.primary,
                    modifier = Modifier.size(40.dp)
                )
                Spacer(Modifier.size(16.dp))
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = plugin.meta.name,
                        style = MaterialTheme.typography.titleMedium
                    )
                    Text(
                        text = "${plugin.meta.type.displayName} · ${plugin.meta.mediaType.displayName}",
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                }
            }

            if (plugin.meta.description.isNotEmpty()) {
                Spacer(Modifier.height(8.dp))
                Text(
                    text = plugin.meta.description,
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }

            Spacer(Modifier.height(12.dp))
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.End,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text(
                    text = "v${plugin.meta.version}",
                    style = MaterialTheme.typography.labelSmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
                Spacer(Modifier.weight(1f))
                IconButton(onClick = onTest) {
                    Icon(Icons.Filled.CheckCircle, contentDescription = "测试", tint = MaterialTheme.colorScheme.secondary)
                }
                IconButton(onClick = onEdit) {
                    Icon(Icons.Filled.Edit, contentDescription = "编辑", tint = MaterialTheme.colorScheme.primary)
                }
                IconButton(onClick = onDelete) {
                    Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ImportPluginDialog(
    json: String,
    onJsonChange: (String) -> Unit,
    onConfirm: () -> Unit,
    onDismiss: () -> Unit,
    error: String
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导入插件") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(12.dp)) {
                OutlinedTextField(
                    value = json,
                    onValueChange = onJsonChange,
                    label = { Text("插件JSON") },
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(200.dp),
                    maxLines = 10,
                    singleLine = false,
                    isError = error.isNotEmpty()
                )
                if (error.isNotEmpty()) {
                    Text(error, color = MaterialTheme.colorScheme.error, style = MaterialTheme.typography.bodySmall)
                }
                Text(
                    "请粘贴插件的JSON配置",
                    style = MaterialTheme.typography.bodySmall,
                    color = MaterialTheme.colorScheme.onSurfaceVariant
                )
            }
        },
        confirmButton = {
            TextButton(onClick = onConfirm, enabled = json.isNotBlank()) {
                Text("导入")
            }
        },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun ExportPluginDialog(
    plugins: List<SubscriptionPlugin>,
    onExportAll: () -> Unit,
    onExportSingle: (String) -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        title = { Text("导出插件") },
        text = {
            Column(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                Text(
                    "选择要导出的插件：",
                    style = MaterialTheme.typography.bodyMedium
                )
                Button(
                    onClick = onExportAll,
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text("导出全部 (${plugins.size}个)")
                }
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(4.dp)
                ) {
                    items(plugins, key = { it.meta.id }) { plugin ->
                        Button(
                            onClick = { onExportSingle(plugin.meta.id) },
                            modifier = Modifier.fillMaxWidth(),
                            contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp)
                        ) {
                            Text(plugin.meta.name, modifier = Modifier.weight(1f))
                            Text("v${plugin.meta.version}")
                        }
                    }
                }
            }
        },
        confirmButton = { },
        dismissButton = {
            TextButton(onClick = onDismiss) {
                Text("取消")
            }
        }
    )
}