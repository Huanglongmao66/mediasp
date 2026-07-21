package com.mpvp.ui.screens.plugin

import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.mpvp.model.*
import com.mpvp.viewmodel.PluginViewModel
import kotlinx.coroutines.launch
import kotlinx.datetime.Clock
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

enum class EditorTab {
    BASIC_INFO,
    REQUEST_CONFIG,
    RESPONSE_CONFIG,
    FIELD_MAPPING,
    JSON_PREVIEW
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PluginEditorScreen(
    viewModel: PluginViewModel,
    plugin: SubscriptionPlugin?,
    onBackClick: () -> Unit,
    onSave: () -> Unit
) {
    val scope = rememberCoroutineScope()
    var selectedTab by remember { mutableStateOf(EditorTab.BASIC_INFO) }

    val initialMeta = plugin?.meta ?: SubscriptionPluginMeta(
        id = "plugin_${Clock.System.now().toEpochMilliseconds()}",
        name = "",
        version = "1.0.0",
        author = "",
        description = "",
        type = PluginType.JSON,
        mediaType = MediaType.VIDEO
    )

    val initialParseRule = plugin?.parseRule ?: PluginParseRule(
        request = PluginRequestConfig(urlTemplate = ""),
        response = PluginResponseConfig(fields = emptyList())
    )

    var meta by remember { mutableStateOf(initialMeta) }
    var configParams by remember { mutableStateOf(plugin?.configParams ?: emptyList()) }
    var parseRule by remember { mutableStateOf(initialParseRule) }
    var customScript by remember { mutableStateOf(plugin?.customScript ?: "") }
    var exampleUrl by remember { mutableStateOf(plugin?.exampleUrl ?: "") }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (plugin == null) "新建插件" else "编辑插件") },
                navigationIcon = {
                    IconButton(onClick = onBackClick) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "返回")
                    }
                },
                actions = {
                    IconButton(onClick = {
                        val newPlugin = SubscriptionPlugin(
                            meta = meta,
                            configParams = configParams,
                            parseRule = parseRule,
                            customScript = customScript.takeIf { it.isNotEmpty() },
                            exampleUrl = exampleUrl.takeIf { it.isNotEmpty() },
                            createdAt = plugin?.createdAt ?: Clock.System.now().toEpochMilliseconds(),
                            updatedAt = Clock.System.now().toEpochMilliseconds()
                        )
                        if (plugin == null) {
                            viewModel.addPlugin(newPlugin)
                        } else {
                            viewModel.updatePlugin(newPlugin)
                        }
                        onSave()
                    }) {
                        Icon(Icons.Filled.Save, contentDescription = "保存")
                    }
                }
            )
        }
    ) { paddingValues ->
        Column(modifier = Modifier.fillMaxSize()) {
            TabRow(selectedTabIndex = selectedTab.ordinal) {
                EditorTab.entries.forEach { tab ->
                    Tab(
                        selected = selectedTab == tab,
                        onClick = { selectedTab = tab },
                        text = { Text(tab.displayName) },
                        icon = { Icon(tab.icon, contentDescription = null) }
                    )
                }
            }

            Box(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(paddingValues)
                    .padding(16.dp)
            ) {
                when (selectedTab) {
                    EditorTab.BASIC_INFO -> BasicInfoTab(
                        meta = meta,
                        configParams = configParams,
                        customScript = customScript,
                        exampleUrl = exampleUrl,
                        onMetaChange = { meta = it },
                        onConfigParamsChange = { configParams = it },
                        onCustomScriptChange = { customScript = it },
                        onExampleUrlChange = { exampleUrl = it }
                    )
                    EditorTab.REQUEST_CONFIG -> RequestConfigTab(
                        requestConfig = parseRule.request,
                        onRequestConfigChange = { parseRule = parseRule.copy(request = it) }
                    )
                    EditorTab.RESPONSE_CONFIG -> ResponseConfigTab(
                        responseConfig = parseRule.response,
                        onResponseConfigChange = { parseRule = parseRule.copy(response = it) }
                    )
                    EditorTab.FIELD_MAPPING -> FieldMappingTab(
                        fields = parseRule.response.fields,
                        onFieldsChange = {
                            parseRule = parseRule.copy(response = parseRule.response.copy(fields = it))
                        }
                    )
                    EditorTab.JSON_PREVIEW -> JsonPreviewTab(
                        meta = meta,
                        configParams = configParams,
                        parseRule = parseRule,
                        customScript = customScript,
                        exampleUrl = exampleUrl,
                        onTest = {
                            scope.launch {
                                val config = configParams.associate { it.name to it.defaultValue }
                                val result = viewModel.testPlugin(meta.id, config)
                                viewModel.setTestResult(result)
                            }
                        }
                    )
                }
            }
        }
    }
}

private val EditorTab.displayName: String
    get() = when (this) {
        EditorTab.BASIC_INFO -> "基本信息"
        EditorTab.REQUEST_CONFIG -> "请求配置"
        EditorTab.RESPONSE_CONFIG -> "响应配置"
        EditorTab.FIELD_MAPPING -> "字段映射"
        EditorTab.JSON_PREVIEW -> "JSON预览"
    }

private val EditorTab.icon: androidx.compose.material.icons.Icon
    get() = when (this) {
        EditorTab.BASIC_INFO -> Icons.Filled.Info
        EditorTab.REQUEST_CONFIG -> Icons.Filled.Send
        EditorTab.RESPONSE_CONFIG -> Icons.Filled.Receive
        EditorTab.FIELD_MAPPING -> Icons.Filled.Map
        EditorTab.JSON_PREVIEW -> Icons.Filled.Code
    }

@Composable
private fun BasicInfoTab(
    meta: SubscriptionPluginMeta,
    configParams: List<PluginConfigParam>,
    customScript: String,
    exampleUrl: String,
    onMetaChange: (SubscriptionPluginMeta) -> Unit,
    onConfigParamsChange: (List<PluginConfigParam>) -> Unit,
    onCustomScriptChange: (String) -> Unit,
    onExampleUrlChange: (String) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            OutlinedTextField(
                value = meta.id,
                onValueChange = { onMetaChange(meta.copy(id = it)) },
                label = { Text("插件ID") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = meta.name,
                onValueChange = { onMetaChange(meta.copy(name = it)) },
                label = { Text("插件名称") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = meta.version,
                onValueChange = { onMetaChange(meta.copy(version = it)) },
                label = { Text("版本号") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = meta.author,
                onValueChange = { onMetaChange(meta.copy(author = it)) },
                label = { Text("作者") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            OutlinedTextField(
                value = meta.description,
                onValueChange = { onMetaChange(meta.copy(description = it)) },
                label = { Text("描述") },
                modifier = Modifier.fillMaxWidth(),
                maxLines = 3
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("插件类型:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                DropdownMenuBox(
                    selected = meta.type,
                    options = PluginType.entries,
                    onSelect = { onMetaChange(meta.copy(type = it)) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("媒体类型:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                DropdownMenuBox(
                    selected = meta.mediaType,
                    options = MediaType.entries,
                    onSelect = { onMetaChange(meta.copy(mediaType = it)) }
                )
            }
        }

        item {
            OutlinedTextField(
                value = exampleUrl,
                onValueChange = onExampleUrlChange,
                label = { Text("示例URL") },
                modifier = Modifier.fillMaxWidth()
            )
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("配置参数:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        onConfigParamsChange(configParams + PluginConfigParam(
                            name = "param_${configParams.size + 1}",
                            displayName = "参数${configParams.size + 1}",
                            type = ParamType.STRING
                        ))
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "添加")
                    }
                }
                LazyColumn(
                    modifier = Modifier.height(200.dp),
                    verticalArrangement = Arrangement.spacedBy(8.dp)
                ) {
                    items(configParams) { param ->
                        ConfigParamItem(
                            param = param,
                            onParamChange = { newParam ->
                                onConfigParamsChange(configParams.map { if (it.name == param.name) newParam else it })
                            },
                            onRemove = { onConfigParamsChange(configParams - param) }
                        )
                    }
                }
            }
        }

        item {
            OutlinedTextField(
                value = customScript,
                onValueChange = onCustomScriptChange,
                label = { Text("自定义脚本（可选）") },
                modifier = Modifier
                    .fillMaxWidth()
                    .height(150.dp),
                maxLines = 8,
                singleLine = false
            )
        }
    }
}

@Composable
private fun ConfigParamItem(
    param: PluginConfigParam,
    onParamChange: (PluginConfigParam) -> Unit,
    onRemove: () -> Unit
) {
    Card {
        Column(modifier = Modifier.padding(12.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                OutlinedTextField(
                    value = param.name,
                    onValueChange = { onParamChange(param.copy(name = it)) },
                    label = { Text("参数名") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = param.displayName,
                    onValueChange = { onParamChange(param.copy(displayName = it)) },
                    label = { Text("显示名") },
                    modifier = Modifier.weight(1f),
                    singleLine = true
                )
                IconButton(onClick = onRemove) {
                    Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                }
            }
            Spacer(Modifier.height(8.dp))
            Row(verticalAlignment = Alignment.CenterVertically) {
                DropdownMenuBox(
                    selected = param.type,
                    options = ParamType.entries,
                    onSelect = { onParamChange(param.copy(type = it)) },
                    modifier = Modifier.weight(1f)
                )
                Spacer(Modifier.width(8.dp))
                Switch(
                    checked = param.required,
                    onCheckedChange = { onParamChange(param.copy(required = it)) }
                )
                Text(if (param.required) "必填" else "可选")
            }
            OutlinedTextField(
                value = param.defaultValue,
                onValueChange = { onParamChange(param.copy(defaultValue = it)) },
                label = { Text("默认值") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }
    }
}

@Composable
private fun RequestConfigTab(
    requestConfig: PluginRequestConfig,
    onRequestConfigChange: (PluginRequestConfig) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            OutlinedTextField(
                value = requestConfig.urlTemplate,
                onValueChange = { onRequestConfigChange(requestConfig.copy(urlTemplate = it)) },
                label = { Text("URL模板") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("https://api.example.com/videos?page={{page}}") }
            )
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("请求方法:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                DropdownMenuBox(
                    selected = requestConfig.method,
                    options = HttpMethod.entries,
                    onSelect = { onRequestConfigChange(requestConfig.copy(method = it)) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("内容类型:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.weight(1f))
                DropdownMenuBox(
                    selected = requestConfig.contentType,
                    options = ContentType.entries,
                    onSelect = { onRequestConfigChange(requestConfig.copy(contentType = it)) }
                )
            }
        }

        item {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("超时时间:", style = MaterialTheme.typography.bodyMedium)
                Spacer(Modifier.width(8.dp))
                OutlinedTextField(
                    value = requestConfig.timeoutSeconds.toString(),
                    onValueChange = { onRequestConfigChange(requestConfig.copy(timeoutSeconds = it.toIntOrNull() ?: 30)) },
                    label = { Text("秒") },
                    modifier = Modifier.width(100.dp),
                    keyboardType = KeyboardType.Number,
                    singleLine = true
                )
            }
        }

        item {
            Column {
                Row(
                    modifier = Modifier.fillMaxWidth(),
                    verticalAlignment = Alignment.CenterVertically
                ) {
                    Text("请求头:", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.weight(1f))
                    IconButton(onClick = {
                        onRequestConfigChange(requestConfig.copy(
                            headers = requestConfig.headers + ("X-Custom-Header" to "")
                        ))
                    }) {
                        Icon(Icons.Filled.Add, contentDescription = "添加")
                    }
                }
                LazyColumn(verticalArrangement = Arrangement.spacedBy(8.dp)) {
                    items(requestConfig.headers.entries.toList()) { (key, value) ->
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = key,
                                onValueChange = { newKey ->
                                    val newHeaders = requestConfig.headers - key + (newKey to value)
                                    onRequestConfigChange(requestConfig.copy(headers = newHeaders))
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("键") }
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = value,
                                onValueChange = { newValue ->
                                    val newHeaders = requestConfig.headers + (key to newValue)
                                    onRequestConfigChange(requestConfig.copy(headers = newHeaders))
                                },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                label = { Text("值") }
                            )
                            IconButton(onClick = {
                                onRequestConfigChange(requestConfig.copy(headers = requestConfig.headers - key))
                            }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun ResponseConfigTab(
    responseConfig: PluginResponseConfig,
    onResponseConfigChange: (PluginResponseConfig) -> Unit
) {
    LazyColumn(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        item {
            OutlinedTextField(
                value = responseConfig.dataPath,
                onValueChange = { onResponseConfigChange(responseConfig.copy(dataPath = it)) },
                label = { Text("数据路径") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("data.items") }
            )
        }

        item {
            OutlinedTextField(
                value = responseConfig.itemPath,
                onValueChange = { onResponseConfigChange(responseConfig.copy(itemPath = it)) },
                label = { Text("列表路径") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("list") }
            )
        }

        item {
            OutlinedTextField(
                value = responseConfig.errorPath,
                onValueChange = { onResponseConfigChange(responseConfig.copy(errorPath = it)) },
                label = { Text("错误路径") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("error") }
            )
        }

        item {
            OutlinedTextField(
                value = responseConfig.errorMessagePath,
                onValueChange = { onResponseConfigChange(responseConfig.copy(errorMessagePath = it)) },
                label = { Text("错误消息路径") },
                modifier = Modifier.fillMaxWidth(),
                placeholder = { Text("message") }
            )
        }
    }
}

@Composable
private fun FieldMappingTab(
    fields: List<PluginFieldMapping>,
    onFieldsChange: (List<PluginFieldMapping>) -> Unit
) {
    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text("字段映射:", style = MaterialTheme.typography.bodyMedium)
            Spacer(Modifier.weight(1f))
            IconButton(onClick = {
                onFieldsChange(fields + PluginFieldMapping(
                    targetField = TargetField.ID,
                    sourcePath = ""
                ))
            }) {
                Icon(Icons.Filled.Add, contentDescription = "添加")
            }
        }

        LazyColumn(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            items(fields) { field ->
                Card {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            DropdownMenuBox(
                                selected = field.targetField,
                                options = TargetField.entries,
                                onSelect = { newField ->
                                    onFieldsChange(fields.map { if (it == field) field.copy(targetField = newField) else it })
                                },
                                modifier = Modifier.weight(1f)
                            )
                            IconButton(onClick = { onFieldsChange(fields - field) }) {
                                Icon(Icons.Filled.Delete, contentDescription = "删除", tint = MaterialTheme.colorScheme.error)
                            }
                        }
                        Spacer(Modifier.height(8.dp))
                        OutlinedTextField(
                            value = field.sourcePath,
                            onValueChange = {
                                onFieldsChange(fields.map { if (it == field) field.copy(sourcePath = it) else it })
                            },
                            label = { Text("源路径") },
                            modifier = Modifier.fillMaxWidth(),
                            placeholder = { Text("id") }
                        )
                        Spacer(Modifier.height(8.dp))
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            OutlinedTextField(
                                value = field.transform,
                                onValueChange = {
                                    onFieldsChange(fields.map { if (it == field) field.copy(transform = it) else it })
                                },
                                label = { Text("转换") },
                                modifier = Modifier.weight(1f),
                                placeholder = { Text("trim, lowercase") },
                                singleLine = true
                            )
                            Spacer(Modifier.width(8.dp))
                            OutlinedTextField(
                                value = field.defaultValue,
                                onValueChange = {
                                    onFieldsChange(fields.map { if (it == field) field.copy(defaultValue = it) else it })
                                },
                                label = { Text("默认值") },
                                modifier = Modifier.weight(1f),
                                singleLine = true
                            )
                        }
                    }
                }
            }
        }
    }
}

@Composable
private fun JsonPreviewTab(
    meta: SubscriptionPluginMeta,
    configParams: List<PluginConfigParam>,
    parseRule: PluginParseRule,
    customScript: String,
    exampleUrl: String,
    onTest: () -> Unit
) {
    val plugin = SubscriptionPlugin(
        meta = meta,
        configParams = configParams,
        parseRule = parseRule,
        customScript = customScript.takeIf { it.isNotEmpty() },
        exampleUrl = exampleUrl.takeIf { it.isNotEmpty() }
    )
    val json = Json { prettyPrint = true }.encodeToString(plugin)

    Column(verticalArrangement = Arrangement.spacedBy(16.dp)) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.End
        ) {
            Button(onClick = onTest) {
                Icon(Icons.Filled.CheckCircle, contentDescription = "测试")
                Spacer(Modifier.width(8.dp))
                Text("测试插件")
            }
        }

        Card {
            Column(modifier = Modifier.padding(16.dp)) {
                Text("插件JSON配置:", style = MaterialTheme.typography.titleSmall)
                Spacer(Modifier.height(8.dp))
                Box(
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(300.dp)
                        .background(MaterialTheme.colorScheme.surfaceVariant)
                        .padding(12.dp)
                ) {
                    Text(
                        text = json,
                        style = MaterialTheme.typography.bodySmall,
                        color = MaterialTheme.colorScheme.onSurfaceVariant,
                        modifier = Modifier.verticalScroll(rememberScrollState())
                    )
                }
            }
        }
    }
}

@Composable
private fun <T : Enum<T>> DropdownMenuBox(
    selected: T,
    options: List<T>,
    onSelect: (T) -> Unit,
    modifier: Modifier = Modifier
) {
    var expanded by remember { mutableStateOf(false) }

    Box(modifier = modifier) {
        Button(onClick = { expanded = true }) {
            Text(selected.name)
        }
        DropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            options.forEach { option ->
                DropdownMenuItem(
                    text = { Text(option.name) },
                    onClick = {
                        onSelect(option)
                        expanded = false
                    }
                )
            }
        }
    }
}