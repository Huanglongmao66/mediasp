package com.mpvp.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

@Serializable
enum class PluginType(val displayName: String) {
    JSON("JSON API"),
    RSS("RSS订阅"),
    HTML("HTML解析"),
    CUSTOM("自定义脚本")
}

@Serializable
enum class HttpMethod(val displayName: String) {
    GET("GET"),
    POST("POST")
}

@Serializable
enum class ContentType(val displayName: String, val contentType: String) {
    JSON("JSON", "application/json"),
    FORM("表单", "application/x-www-form-urlencoded"),
    TEXT("文本", "text/plain")
}

@Serializable
data class SubscriptionPluginMeta(
    val id: String,
    val name: String,
    val version: String = "1.0.0",
    val author: String = "",
    val description: String = "",
    val type: PluginType,
    val mediaType: MediaType,
    val icon: String = "https://trae-api-cn.mchost.guru/api/ide/v1/text_to_image?prompt=plugin%20icon%20logo%20minimal&image_size=square",
    val tags: List<String> = emptyList(),
    val website: String = "",
    val supportUrl: String = ""
)

@Serializable
data class PluginConfigParam(
    val name: String,
    val displayName: String,
    val type: ParamType,
    val required: Boolean = false,
    val defaultValue: String = "",
    val placeholder: String = "",
    val options: List<String> = emptyList()
)

@Serializable
enum class ParamType(val displayName: String) {
    STRING("文本"),
    NUMBER("数字"),
    BOOLEAN("布尔值"),
    SELECT("选择")
}

@Serializable
data class PluginParseRule(
    val request: PluginRequestConfig,
    val response: PluginResponseConfig,
    val pagination: PluginPaginationConfig? = null
)

@Serializable
data class PluginRequestConfig(
    val urlTemplate: String,
    val method: HttpMethod = HttpMethod.GET,
    val headers: Map<String, String> = emptyMap(),
    val queryParams: List<PluginParamMapping> = emptyList(),
    val bodyParams: List<PluginParamMapping> = emptyList(),
    val contentType: ContentType = ContentType.JSON,
    val timeoutSeconds: Int = 30,
    val retryCount: Int = 2
)

@Serializable
data class PluginParamMapping(
    val key: String,
    val value: String,
    val isDynamic: Boolean = false
)

@Serializable
data class PluginResponseConfig(
    val dataPath: String = "",
    val itemPath: String = "",
    val fields: List<PluginFieldMapping>,
    val errorPath: String = "",
    val errorMessagePath: String = ""
)

@Serializable
data class PluginFieldMapping(
    val targetField: TargetField,
    val sourcePath: String,
    val transform: String = "",
    val defaultValue: String = ""
)

@Serializable
enum class TargetField(val displayName: String) {
    ID("ID"),
    TITLE("标题"),
    URL("链接"),
    COVER_URL("封面"),
    DURATION("时长"),
    SIZE("大小"),
    DESCRIPTION("描述"),
    AUTHOR("作者"),
    PUBLISH_TIME("发布时间"),
    VIEW_COUNT("播放量"),
    CATEGORY("分类"),
    MIME_TYPE("MIME类型")
}

@Serializable
data class PluginPaginationConfig(
    val enabled: Boolean = false,
    val pageParam: String = "page",
    val pageSizeParam: String = "page_size",
    val defaultPageSize: Int = 20,
    val totalPath: String = "",
    val hasMorePath: String = ""
)

@Serializable
data class SubscriptionPlugin(
    val meta: SubscriptionPluginMeta,
    val configParams: List<PluginConfigParam> = emptyList(),
    val parseRule: PluginParseRule,
    val customScript: String? = null,
    val exampleUrl: String? = null,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    fun toJson(): String {
        return Json.encodeToString(this)
    }

    companion object {
        fun fromJson(json: String): SubscriptionPlugin {
            return Json.decodeFromString(json)
        }
    }
}

@Serializable
data class SubscriptionPluginInstance(
    val pluginId: String,
    val instanceId: String,
    val name: String,
    val config: Map<String, String> = emptyMap(),
    val enabled: Boolean = true,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)