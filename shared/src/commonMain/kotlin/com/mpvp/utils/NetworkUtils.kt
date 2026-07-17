package com.mpvp.utils

import io.ktor.client.HttpClient
import io.ktor.client.call.body
import io.ktor.client.plugins.HttpTimeout
import io.ktor.client.plugins.contentnegotiation.ContentNegotiation
import io.ktor.client.plugins.logging.LogLevel
import io.ktor.client.plugins.logging.Logging
import io.ktor.client.request.get
import io.ktor.client.request.head
import io.ktor.client.request.parameter
import io.ktor.client.statement.HttpResponse
import io.ktor.client.statement.bodyAsText
import io.ktor.http.HttpStatusCode
import io.ktor.http.isSuccess
import io.ktor.serialization.kotlinx.json.json
import kotlinx.serialization.json.Json

/**
 * 网络请求工具类
 *
 * 提供统一的网络请求封装，包括HTTP客户端配置、
 * 视频URL验证、m3u8解析等功能
 */
object NetworkUtils {

    /** 默认连接超时时间（毫秒） */
    private const val DEFAULT_CONNECT_TIMEOUT = 15_000L

    /** 默认读取超时时间（毫秒） */
    private const val DEFAULT_SOCKET_TIMEOUT = 30_000L

    /**
     * 创建HTTP客户端
     *
     * @param enableLogging 是否启用日志
     * @return 配置好的HttpClient实例
     */
    fun createHttpClient(enableLogging: Boolean = false): HttpClient {
        return HttpClient {
            // 内容协商配置 - JSON解析
            install(ContentNegotiation) {
                json(Json {
                    prettyPrint = true
                    isLenient = true
                    ignoreUnknownKeys = true
                    encodeDefaults = true
                })
            }

            // 超时配置
            install(HttpTimeout) {
                connectTimeoutMillis = DEFAULT_CONNECT_TIMEOUT
                socketTimeoutMillis = DEFAULT_SOCKET_TIMEOUT
                requestTimeoutMillis = 60_000
            }

            // 日志配置
            if (enableLogging) {
                install(Logging) {
                    level = LogLevel.HEADERS
                }
            }
        }
    }

    /**
     * 检查URL是否可访问
     *
     * @param client HTTP客户端
     * @param url 要检查的URL
     * @return 是否可访问
     */
    suspend fun isUrlAccessible(client: HttpClient, url: String): Boolean {
        return try {
            val response = client.head(url)
            response.status.isSuccess()
        } catch (e: Exception) {
            false
        }
    }

    /**
     * 获取URL响应内容类型
     *
     * @param client HTTP客户端
     * @param url URL
     * @return 内容类型字符串
     */
    suspend fun getContentType(client: HttpClient, url: String): String? {
        return try {
            val response = client.head(url)
            response.headers["Content-Type"]
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取URL内容长度
     *
     * @param client HTTP客户端
     * @param url URL
     * @return 内容长度（字节），失败返回-1
     */
    suspend fun getContentLength(client: HttpClient, url: String): Long {
        return try {
            val response = client.head(url)
            response.headers["Content-Length"]?.toLongOrNull() ?: -1L
        } catch (e: Exception) {
            -1L
        }
    }

    /**
     * 下载文本内容
     *
     * @param client HTTP客户端
     * @param url URL
     * @return 文本内容
     */
    suspend fun downloadText(client: HttpClient, url: String): String? {
        return try {
            val response = client.get(url)
            if (response.status.isSuccess()) {
                response.bodyAsText()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 获取JSON数据并反序列化
     *
     * @param client HTTP客户端
     * @param url URL
     * @return 反序列化后的对象
     */
    suspend inline fun <reified T> getJson(
        client: HttpClient,
        url: String,
        params: Map<String, String> = emptyMap()
    ): T? {
        return try {
            val response = client.get(url) {
                params.forEach { (key, value) ->
                    parameter(key, value)
                }
            }
            if (response.status.isSuccess()) {
                response.body<T>()
            } else {
                null
            }
        } catch (e: Exception) {
            null
        }
    }

    /**
     * 解析m3u8播放列表
     *
     * 从m3u8内容中提取所有TS分片URL
     *
     * @param m3u8Content m3u8文件内容
     * @param baseUrl 基础URL（用于拼接相对路径）
     * @return TS分片URL列表
     */
    fun parseM3u8Playlist(m3u8Content: String, baseUrl: String): List<String> {
        val segments = mutableListOf<String>()
        val lines = m3u8Content.lines()

        for (line in lines) {
            val trimmedLine = line.trim()
            // 跳过注释行和空行
            if (trimmedLine.isEmpty() || trimmedLine.startsWith("#")) {
                continue
            }
            // 这是一个TS分片URL
            val segmentUrl = if (trimmedLine.startsWith("http://") || trimmedLine.startsWith("https://")) {
                trimmedLine
            } else {
                // 拼接相对路径
                val baseDir = baseUrl.substringBeforeLast("/", "")
                if (trimmedLine.startsWith("/")) {
                    // 绝对路径
                    val protocol = baseUrl.substringBefore("://")
                    val host = baseUrl.substringAfter("://").substringBefore("/")
                    "$protocol://$host$trimmedLine"
                } else {
                    // 相对路径
                    "$baseDir/$trimmedLine"
                }
            }
            segments.add(segmentUrl)
        }

        return segments
    }

    /**
     * 检查是否为有效的m3u8内容
     *
     * @param content 内容字符串
     * @return 是否为m3u8
     */
    fun isM3u8Content(content: String): Boolean {
        return content.trim().startsWith("#EXTM3U")
    }

    /**
     * 检查m3u8是否为直播流
     *
     * @param content m3u8内容
     * @return 是否为直播流
     */
    fun isM3u8LiveStream(content: String): Boolean {
        return content.contains("#EXT-X-STREAM-INF") ||
               !content.contains("#EXT-X-ENDLIST")
    }

    /**
     * 从m3u8中获取主播放列表URL
     *
     * @param content m3u8内容
     * @param baseUrl 基础URL
     * @return 主播放列表URL列表
     */
    fun getM3u8MasterPlaylists(content: String, baseUrl: String): List<String> {
        val playlists = mutableListOf<String>()
        val lines = content.lines()

        for (i in lines.indices) {
            val line = lines[i].trim()
            if (line.startsWith("#EXT-X-STREAM-INF") && i + 1 < lines.size) {
                val playlistUrl = lines[i + 1].trim()
                val fullUrl = if (playlistUrl.startsWith("http")) {
                    playlistUrl
                } else {
                    val baseDir = baseUrl.substringBeforeLast("/", "")
                    "$baseDir/$playlistUrl"
                }
                playlists.add(fullUrl)
            }
        }

        return playlists
    }
}
