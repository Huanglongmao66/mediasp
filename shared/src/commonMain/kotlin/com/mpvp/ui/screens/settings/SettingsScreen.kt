package com.mpvp.ui.screens.settings

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Brightness6
import androidx.compose.material.icons.filled.Cached
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.CleaningServices
import androidx.compose.material.icons.filled.ClosedCaption
import androidx.compose.material.icons.filled.Comment
import androidx.compose.material.icons.filled.FastForward
import androidx.compose.material.icons.filled.GridView
import androidx.compose.material.icons.filled.Info
import androidx.compose.material.icons.filled.PlayCircle
import androidx.compose.material.icons.filled.RssFeed
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Storage
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Slider
import androidx.compose.material3.Switch
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.unit.dp
import com.mpvp.model.PlayerConfig
import com.mpvp.model.ThemeMode

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SettingsScreen(
    config: PlayerConfig,
    onConfigChange: (PlayerConfig) -> Unit,
    onBackClick: () -> Unit,
    onClearCache: () -> Unit = {},
    onClearHistory: () -> Unit = {},
    onSubscriptionClick: () -> Unit = {}
) {
    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("设置") },
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
                .verticalScroll(rememberScrollState())
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            SettingsSection(title = "播放设置") {
                SwitchSettingItem(
                    icon = Icons.Filled.PlayCircle,
                    title = "自动播放",
                    description = "打开视频时自动开始播放",
                    checked = config.autoPlay,
                    onCheckedChange = { onConfigChange(config.copy(autoPlay = it)) }
                )
                SwitchSettingItem(
                    icon = Icons.Filled.FastForward,
                    title = "自动播放下一集",
                    description = "视频播放完成后自动播放下一集",
                    checked = config.autoPlayNext,
                    onCheckedChange = { onConfigChange(config.copy(autoPlayNext = it)) }
                )
                SwitchSettingItem(
                    icon = Icons.Filled.Cached,
                    title = "记住播放位置",
                    description = "记录上次播放进度",
                    checked = config.rememberPlayPosition,
                    onCheckedChange = { onConfigChange(config.copy(rememberPlayPosition = it)) }
                )
                SwitchSettingItem(
                    icon = Icons.Filled.Settings,
                    title = "后台播放",
                    description = "切换到后台时继续播放",
                    checked = config.backgroundPlay,
                    onCheckedChange = { onConfigChange(config.copy(backgroundPlay = it)) }
                )
                SwitchSettingItem(
                    icon = Icons.Filled.Camera,
                    title = "硬件解码",
                    description = "使用硬件解码（更省电）",
                    checked = config.hardwareDecode,
                    onCheckedChange = { onConfigChange(config.copy(hardwareDecode = it)) }
                )
                PlaybackSpeedSelectorItem(
                    currentSpeed = config.defaultPlaybackSpeed,
                    onSpeedSelected = { onConfigChange(config.copy(defaultPlaybackSpeed = it)) }
                )
            }

            SettingsSection(title = "弹幕设置") {
                SwitchSettingItem(
                    icon = Icons.Filled.Comment,
                    title = "弹幕开关",
                    description = "显示视频弹幕",
                    checked = config.danmakuEnabled,
                    onCheckedChange = { onConfigChange(config.copy(danmakuEnabled = it)) }
                )
                SliderSettingItem(
                    icon = Icons.Filled.Comment,
                    title = "弹幕透明度",
                    value = config.danmakuOpacity,
                    valueRange = 0.1f..1f,
                    formatValue = { "${(it * 100).toInt()}%" },
                    onValueChange = { onConfigChange(config.copy(danmakuOpacity = it)) }
                )
                SliderSettingItem(
                    icon = Icons.Filled.FastForward,
                    title = "弹幕速度",
                    value = config.danmakuSpeed,
                    valueRange = 0.5f..3f,
                    formatValue = { "${it}x" },
                    onValueChange = { onConfigChange(config.copy(danmakuSpeed = it)) }
                )
            }

            SettingsSection(title = "字幕设置") {
                SwitchSettingItem(
                    icon = Icons.Filled.ClosedCaption,
                    title = "字幕开关",
                    description = "显示视频字幕",
                    checked = config.subtitleEnabled,
                    onCheckedChange = { onConfigChange(config.copy(subtitleEnabled = it)) }
                )
                SliderSettingItem(
                    icon = Icons.Filled.ClosedCaption,
                    title = "字幕大小",
                    value = config.subtitleFontSize.toFloat(),
                    valueRange = 12f..36f,
                    formatValue = { "${it.toInt()}sp" },
                    onValueChange = { onConfigChange(config.copy(subtitleFontSize = it.toInt())) }
                )
            }

            SettingsSection(title = "显示设置") {
                ThemeSelectorItem(
                    currentTheme = config.themeMode,
                    onThemeSelected = { onConfigChange(config.copy(themeMode = it)) }
                )
                SwitchSettingItem(
                    icon = Icons.Filled.PlayCircle,
                    title = "显示时长",
                    description = "在视频卡片上显示时长",
                    checked = config.showDuration,
                    onCheckedChange = { onConfigChange(config.copy(showDuration = it)) }
                )
                GridColumnsSelectorItem(
                    currentColumns = config.gridColumns,
                    onColumnsSelected = { onConfigChange(config.copy(gridColumns = it)) }
                )
            }

            SettingsSection(title = "内容订阅") {
                ActionSettingItem(
                    icon = Icons.Filled.RssFeed,
                    title = "订阅源管理",
                    description = "添加和管理内容订阅源",
                    onClick = onSubscriptionClick
                )
            }

            SettingsSection(title = "存储设置") {
                CacheSizeSelectorItem(
                    currentSizeMB = config.cacheSizeMB,
                    onSizeSelected = { onConfigChange(config.copy(cacheSizeMB = it)) }
                )
                ActionSettingItem(
                    icon = Icons.Filled.CleaningServices,
                    title = "清除缓存",
                    description = "清除播放缓存数据",
                    onClick = onClearCache
                )
                ActionSettingItem(
                    icon = Icons.Filled.CleaningServices,
                    title = "清除播放历史",
                    description = "清除所有播放历史记录",
                    onClick = onClearHistory
                )
            }

            SettingsSection(title = "关于") {
                ActionSettingItem(
                    icon = Icons.Filled.Info,
                    title = "版本信息",
                    description = "v1.0.0",
                    onClick = {}
                )
            }
        }
    }
}

@Composable
private fun SettingsSection(
    title: String,
    content: @Composable () -> Unit
) {
    Column {
        Text(
            text = title,
            style = MaterialTheme.typography.titleSmall,
            color = MaterialTheme.colorScheme.primary,
            modifier = Modifier.padding(bottom = 8.dp)
        )
        Card(
            modifier = Modifier.fillMaxWidth(),
            colors = CardDefaults.cardColors(
                containerColor = MaterialTheme.colorScheme.surface
            )
        ) {
            Column {
                content()
            }
        }
    }
}

@Composable
private fun SwitchSettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    checked: Boolean,
    onCheckedChange: (Boolean) -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        Switch(
            checked = checked,
            onCheckedChange = onCheckedChange
        )
    }
}

@Composable
private fun SliderSettingItem(
    icon: ImageVector,
    title: String,
    value: Float,
    valueRange: ClosedFloatingPointRange<Float>,
    formatValue: (Float) -> String,
    onValueChange: (Float) -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp)
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(
                imageVector = icon,
                contentDescription = null,
                tint = MaterialTheme.colorScheme.primary,
                modifier = Modifier.size(24.dp)
            )
            Spacer(modifier = Modifier.size(16.dp))
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge,
                modifier = Modifier.weight(1f)
            )
            Text(
                text = formatValue(value),
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.primary
            )
        }
        Spacer(modifier = Modifier.height(8.dp))
        Slider(
            value = value,
            onValueChange = onValueChange,
            valueRange = valueRange,
            modifier = Modifier.fillMaxWidth()
        )
    }
}

@Composable
private fun ActionSettingItem(
    icon: ImageVector,
    title: String,
    description: String,
    onClick: () -> Unit
) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .clickable(onClick = onClick)
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = icon,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = description,
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
    }
}

@Composable
private fun PlaybackSpeedSelectorItem(
    currentSpeed: Float,
    onSpeedSelected: (Float) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val speeds = listOf(0.5f, 0.75f, 1.0f, 1.25f, 1.5f, 2.0f, 3.0f)

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.FastForward,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "默认播放速度",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = "${currentSpeed}x",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { expanded = true }) {
            Text("选择")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            speeds.forEach { speed ->
                DropdownMenuItem(
                    text = { Text("${speed}x") },
                    onClick = {
                        onSpeedSelected(speed)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun ThemeSelectorItem(
    currentTheme: ThemeMode,
    onThemeSelected: (ThemeMode) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Brightness6,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "主题模式",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = when (currentTheme) {
                    ThemeMode.LIGHT -> "亮色主题"
                    ThemeMode.DARK -> "暗色主题"
                    ThemeMode.SYSTEM -> "跟随系统"
                },
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { expanded = true }) {
            Text("选择")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            DropdownMenuItem(
                text = { Text("亮色主题") },
                onClick = {
                    onThemeSelected(ThemeMode.LIGHT)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("暗色主题") },
                onClick = {
                    onThemeSelected(ThemeMode.DARK)
                    expanded = false
                }
            )
            DropdownMenuItem(
                text = { Text("跟随系统") },
                onClick = {
                    onThemeSelected(ThemeMode.SYSTEM)
                    expanded = false
                }
            )
        }
    }
}

@Composable
private fun GridColumnsSelectorItem(
    currentColumns: Int,
    onColumnsSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(0 to "自适应", 2 to "2列", 3 to "3列", 4 to "4列")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.GridView,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "网格列数",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = options.firstOrNull { it.first == currentColumns }?.second ?: "自适应",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { expanded = true }) {
            Text("选择")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (columns, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onColumnsSelected(columns)
                        expanded = false
                    }
                )
            }
        }
    }
}

@Composable
private fun CacheSizeSelectorItem(
    currentSizeMB: Int,
    onSizeSelected: (Int) -> Unit
) {
    var expanded by remember { mutableStateOf(false) }
    val options = listOf(100 to "100 MB", 200 to "200 MB", 500 to "500 MB", 1000 to "1 GB", 2000 to "2 GB")

    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        Icon(
            imageVector = Icons.Filled.Storage,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier.size(24.dp)
        )
        Spacer(modifier = Modifier.size(16.dp))
        Column(
            modifier = Modifier.weight(1f)
        ) {
            Text(
                text = "缓存大小",
                style = MaterialTheme.typography.bodyLarge
            )
            Text(
                text = options.firstOrNull { it.first == currentSizeMB }?.second ?: "500 MB",
                style = MaterialTheme.typography.bodySmall,
                color = MaterialTheme.colorScheme.onSurfaceVariant
            )
        }
        IconButton(onClick = { expanded = true }) {
            Text("选择")
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { (size, label) ->
                DropdownMenuItem(
                    text = { Text(label) },
                    onClick = {
                        onSizeSelected(size)
                        expanded = false
                    }
                )
            }
        }
    }
}
