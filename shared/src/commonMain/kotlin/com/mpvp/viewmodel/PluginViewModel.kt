package com.mpvp.viewmodel

import com.mpvp.model.MediaItem
import com.mpvp.model.SubscriptionPlugin
import com.mpvp.model.SubscriptionPluginInstance
import com.mpvp.plugin.PluginParser
import com.mpvp.repository.AppDataStore
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Clock
import kotlinx.serialization.builtins.ListSerializer

data class PluginState(
    val plugins: List<SubscriptionPlugin> = emptyList(),
    val instances: List<SubscriptionPluginInstance> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null,
    val selectedPlugin: SubscriptionPlugin? = null,
    val testResult: TestResult? = null
)

data class TestResult(
    val success: Boolean,
    val message: String,
    val items: List<MediaItem> = emptyList()
)

class PluginViewModel(
    private val appDataStore: AppDataStore,
    private val httpClient: HttpClient
) : StateViewModel<PluginState>(PluginState()) {

    private val pluginParser = PluginParser(httpClient)

    val pluginsFlow: Flow<List<SubscriptionPlugin>> = appDataStore.getSubscriptionPlugins()
    val instancesFlow: Flow<List<SubscriptionPluginInstance>> = appDataStore.getPluginInstances()

    init {
        launch {
            combine(pluginsFlow, instancesFlow) { plugins, instances ->
                updateState {
                    copy(plugins = plugins, instances = instances)
                }
            }.collect { }
        }
    }

    fun addPlugin(plugin: SubscriptionPlugin) {
        launch {
            appDataStore.addSubscriptionPlugin(plugin)
        }
    }

    fun updatePlugin(plugin: SubscriptionPlugin) {
        launch {
            val updated = plugin.copy(updatedAt = Clock.System.now().toEpochMilliseconds())
            appDataStore.addSubscriptionPlugin(updated)
        }
    }

    fun deletePlugin(pluginId: String) {
        launch {
            appDataStore.deleteSubscriptionPlugin(pluginId)
        }
    }

    fun importPlugin(json: String): Result<SubscriptionPlugin> {
        return try {
            val plugin = SubscriptionPlugin.fromJson(json)
            launch {
                appDataStore.addSubscriptionPlugin(plugin)
            }
            Result.success(plugin)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    fun exportPlugin(pluginId: String): String? {
        val plugin = state.value.plugins.find { it.meta.id == pluginId }
        return plugin?.toJson()
    }

    fun exportAllPlugins(): String {
        return kotlinx.serialization.json.Json.encodeToString(ListSerializer(SubscriptionPlugin.serializer()), state.value.plugins)
    }

    fun addInstance(instance: SubscriptionPluginInstance) {
        launch {
            appDataStore.addPluginInstance(instance)
        }
    }

    fun deleteInstance(instanceId: String) {
        launch {
            appDataStore.deletePluginInstance(instanceId)
        }
    }

    suspend fun testPlugin(pluginId: String, testConfig: Map<String, String>): TestResult {
        val plugin = appDataStore.getSubscriptionPluginById(pluginId)
        if (plugin == null) {
            return TestResult(false, "插件不存在")
        }

        val testInstance = SubscriptionPluginInstance(
            pluginId = pluginId,
            instanceId = "test_${Clock.System.now().toEpochMilliseconds()}",
            name = "测试实例",
            config = testConfig,
            enabled = true
        )

        return try {
            val result = pluginParser.detect(plugin, testInstance)
            if (result.isSuccess) {
                val itemsResult = pluginParser.parse(plugin, testInstance, 1)
                if (itemsResult.isSuccess) {
                    TestResult(
                        success = true,
                        message = "检测成功，获取到 ${itemsResult.getOrNull()?.size ?: 0} 条数据",
                        items = itemsResult.getOrNull() ?: emptyList()
                    )
                } else {
                    TestResult(
                        success = false,
                        message = "检测成功，但解析数据失败: ${itemsResult.exceptionOrNull()?.message}"
                    )
                }
            } else {
                TestResult(
                    success = false,
                    message = "检测失败: ${result.exceptionOrNull()?.message}"
                )
            }
        } catch (e: Exception) {
            TestResult(false, "测试异常: ${e.message}")
        }
    }

    fun setSelectedPlugin(plugin: SubscriptionPlugin?) {
        updateState {
            copy(selectedPlugin = plugin)
        }
    }

    fun setTestResult(result: TestResult?) {
        updateState {
            copy(testResult = result)
        }
    }

    fun clearError() {
        updateState {
            copy(error = null)
        }
    }
}