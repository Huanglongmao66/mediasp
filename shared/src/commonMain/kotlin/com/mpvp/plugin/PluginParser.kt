package com.mpvp.plugin

import com.mpvp.model.*
import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.request.HttpRequestBuilder
import io.ktor.client.request.get
import io.ktor.client.request.headers
import io.ktor.client.request.parameter
import io.ktor.client.request.post
import io.ktor.client.statement.HttpResponse
import io.ktor.http.ContentType
import io.ktor.http.HttpHeaders
import io.ktor.http.contentType
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.serialization.json.Json
import kotlinx.serialization.json.JsonArray
import kotlinx.serialization.json.JsonElement
import kotlinx.serialization.json.JsonObject
import kotlinx.serialization.json.JsonPrimitive
import kotlinx.serialization.json.booleanOrNull
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.floatOrNull
import kotlinx.serialization.json.intOrNull
import kotlinx.serialization.json.longOrNull
import kotlinx.serialization.json.jsonArray
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive

class PluginParser(private val httpClient: HttpClient) {

    private val json = Json {
        ignoreUnknownKeys = true
        isLenient = true
    }

    suspend fun parse(
        plugin: SubscriptionPlugin,
        instance: SubscriptionPluginInstance,
        page: Int = 1
    ): Result<List<MediaItem>> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = buildUrl(plugin, instance, page)
            val response = executeRequest(plugin, instance, url)
            val responseBody = response.body<String>()
            
            when (plugin.meta.type) {
                PluginType.JSON -> parseJsonResponse(plugin, responseBody)
                PluginType.RSS -> parseRssResponse(plugin, responseBody)
                PluginType.HTML -> parseHtmlResponse(plugin, responseBody)
                PluginType.CUSTOM -> parseCustomResponse(plugin, responseBody)
            }
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    suspend fun detect(plugin: SubscriptionPlugin, instance: SubscriptionPluginInstance): Result<Boolean> = withContext(Dispatchers.IO) {
        return@withContext try {
            val url = buildUrl(plugin, instance, 1)
            val response = executeRequest(plugin, instance, url)
            Result.success(response.status.value in 200..299)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun buildUrl(plugin: SubscriptionPlugin, instance: SubscriptionPluginInstance, page: Int): String {
        var url = plugin.parseRule.request.urlTemplate
        
        plugin.configParams.forEach { param ->
            val value = instance.config[param.name] ?: param.defaultValue
            url = url.replace("{{${param.name}}}", value)
        }
        
        if (plugin.parseRule.pagination?.enabled == true) {
            val pageSize = plugin.parseRule.pagination.defaultPageSize
            url = if (url.contains("?")) {
                "$url&${plugin.parseRule.pagination.pageParam}=$page&${plugin.parseRule.pagination.pageSizeParam}=$pageSize"
            } else {
                "$url?${plugin.parseRule.pagination.pageParam}=$page&${plugin.parseRule.pagination.pageSizeParam}=$pageSize"
            }
        }
        
        return url
    }

    private suspend fun executeRequest(plugin: SubscriptionPlugin, instance: SubscriptionPluginInstance, url: String): HttpResponse {
        val requestConfig = plugin.parseRule.request
        
        return when (requestConfig.method) {
            HttpMethod.GET -> {
                httpClient.get(url) {
                    configureRequest(requestConfig, instance)
                }
            }
            HttpMethod.POST -> {
                httpClient.post(url) {
                    configureRequest(requestConfig, instance)
                }
            }
        }
    }

    private fun HttpRequestBuilder.configureRequest(requestConfig: PluginRequestConfig, instance: SubscriptionPluginInstance) {
        headers {
            requestConfig.headers.forEach { (key, value) ->
                append(key, resolveValue(value, instance))
            }
            append(HttpHeaders.UserAgent, "MPVP-Plugin/1.0")
        }
        
        contentType(ContentType.parse(requestConfig.contentType.contentType))
        
        requestConfig.queryParams.forEach { param ->
            parameter(param.key, resolveValue(param.value, instance))
        }
    }

    private fun resolveValue(value: String, instance: SubscriptionPluginInstance): String {
        var result = value
        instance.config.forEach { (key, configValue) ->
            result = result.replace("{{${key}}}", configValue)
        }
        return result
    }

    private fun parseJsonResponse(plugin: SubscriptionPlugin, responseBody: String): Result<List<MediaItem>> {
        return try {
            val jsonElement = json.parseToJsonElement(responseBody)
            val dataElement = navigatePath(jsonElement, plugin.parseRule.response.dataPath)
            val itemsElement = navigatePath(dataElement, plugin.parseRule.response.itemPath)
            
            val items = when (itemsElement) {
                is JsonArray -> itemsElement.map { parseJsonItem(it, plugin) }
                is JsonObject -> listOf(parseJsonItem(itemsElement, plugin))
                else -> emptyList()
            }.filterNotNull()
            
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun navigatePath(element: JsonElement, path: String): JsonElement {
        if (path.isEmpty()) return element
        
        var current = element
        val parts = path.split(".").filter { it.isNotEmpty() }
        
        for (part in parts) {
            current = when (current) {
                is JsonObject -> current[part] ?: return JsonObject(emptyMap())
                is JsonArray -> {
                    val index = part.toIntOrNull() ?: 0
                    current.getOrNull(index) ?: return JsonArray(emptyList())
                }
                else -> return JsonObject(emptyMap())
            }
        }
        
        return current
    }

    private fun parseJsonItem(itemElement: JsonElement, plugin: SubscriptionPlugin): MediaItem? {
        val fields = mutableMapOf<TargetField, String>()
        
        plugin.parseRule.response.fields.forEach { mapping ->
            val value = when (itemElement) {
                is JsonObject -> {
                    val pathParts = mapping.sourcePath.split(".").filter { it.isNotEmpty() }
                    var current: JsonElement? = itemElement
                    
                    for (part in pathParts) {
                        current = when (current) {
                            is JsonObject -> current[part]
                            is JsonArray -> {
                                val index = part.toIntOrNull() ?: 0
                                current.getOrNull(index)
                            }
                            else -> null
                        }
                    }
                    
                    current?.jsonPrimitive?.contentOrNull ?: mapping.defaultValue
                }
                is JsonPrimitive -> itemElement.contentOrNull ?: mapping.defaultValue
                else -> mapping.defaultValue
            }
            
            fields[mapping.targetField] = applyTransform(value, mapping.transform)
        }
        
        return createMediaItem(fields, plugin.meta.mediaType)
    }

    private fun applyTransform(value: String, transform: String): String {
        if (transform.isEmpty()) return value
        
        return when (transform) {
            "trim" -> value.trim()
            "lowercase" -> value.lowercase()
            "uppercase" -> value.uppercase()
            else -> value
        }
    }

    private fun parseRssResponse(plugin: SubscriptionPlugin, responseBody: String): Result<List<MediaItem>> {
        return try {
            val items = mutableListOf<MediaItem>()
            val rssPattern = Regex("<item>(.*?)</item>", RegexOption.DOT_MATCHES_ALL)
            val matches = rssPattern.findAll(responseBody)
            
            matches.forEach { match ->
                val itemContent = match.groupValues[1]
                val fields = extractRssFields(itemContent, plugin)
                createMediaItem(fields, plugin.meta.mediaType)?.let { items.add(it) }
            }
            
            Result.success(items)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun extractRssFields(itemContent: String, plugin: SubscriptionPlugin): Map<TargetField, String> {
        val fields = mutableMapOf<TargetField, String>()
        
        plugin.parseRule.response.fields.forEach { mapping ->
            val tagName = mapping.sourcePath.replace("/", "")
            val pattern = Regex("<$tagName[^>]*>(.*?)</$tagName>", RegexOption.DOT_MATCHES_ALL)
            val match = pattern.find(itemContent)
            
            val value = match?.groupValues?.get(1)?.trim() ?: mapping.defaultValue
            fields[mapping.targetField] = value
        }
        
        return fields
    }

    private fun parseHtmlResponse(plugin: SubscriptionPlugin, responseBody: String): Result<List<MediaItem>> {
        return try {
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun parseCustomResponse(plugin: SubscriptionPlugin, responseBody: String): Result<List<MediaItem>> {
        return try {
            Result.success(emptyList())
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    private fun createMediaItem(fields: Map<TargetField, String>, mediaType: MediaType): MediaItem? {
        val id = fields[TargetField.ID] ?: return null
        val title = fields[TargetField.TITLE] ?: "未知标题"
        val url = fields[TargetField.URL] ?: return null
        val coverUrl = fields[TargetField.COVER_URL]
        
        val item: MediaItem = when (mediaType) {
            MediaType.VIDEO -> VideoItem(
                id = id,
                title = title,
                videoUrl = url,
                coverUrl = coverUrl,
                duration = fields[TargetField.DURATION]?.toLongOrNull() ?: 0L,
                fileSize = fields[TargetField.SIZE]?.toLongOrNull() ?: 0L
            )
            MediaType.MUSIC -> MusicItem(
                id = id,
                title = title,
                artist = fields[TargetField.AUTHOR] ?: "未知艺术家",
                duration = fields[TargetField.DURATION]?.toLongOrNull() ?: 0L,
                coverUrl = coverUrl,
                sourceUrl = url
            )
            MediaType.IMAGE -> ImageItem(
                id = id,
                title = title,
                coverUrl = coverUrl,
                sourceUrl = url
            )
            MediaType.NOVEL -> NovelItem(
                id = id,
                title = title,
                author = fields[TargetField.AUTHOR] ?: "未知作者",
                description = fields[TargetField.DESCRIPTION] ?: "",
                coverUrl = coverUrl,
                sourceUrl = url
            )
            MediaType.RADIO -> RadioItem(
                id = id,
                title = title,
                category = fields[TargetField.CATEGORY] ?: "",
                coverUrl = coverUrl,
                sourceUrl = url
            )
        }
        return item
    }
}