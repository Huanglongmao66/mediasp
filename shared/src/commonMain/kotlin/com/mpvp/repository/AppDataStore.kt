package com.mpvp.repository

import com.mpvp.model.PlayHistory
import com.mpvp.model.PlayerConfig
import com.mpvp.model.VideoItem
import com.russhwolf.settings.ObservableSettings
import com.russhwolf.settings.coroutines.FlowSettings
import com.russhwolf.settings.coroutines.toFlowSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import kotlinx.serialization.encodeToString
import kotlinx.serialization.json.Json

/**
 * 应用数据存储类
 *
 * 使用Multiplatform Settings实现跨平台数据持久化
 * 存储视频列表、播放历史、收藏、配置等数据
 *
 * @property settings 平台设置存储
 */
class AppDataStore(private val settings: ObservableSettings) {

    /** 流式设置 */
    private val flowSettings: FlowSettings = settings.toFlowSettings()

    /** JSON序列化器 */
    private val json = Json {
        prettyPrint = true
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

    companion object {
        /** 视频列表键 */
        private const val KEY_VIDEOS = "videos"

        /** 收藏列表键 */
        private const val KEY_FAVORITES = "favorites"

        /** 播放历史键 */
        private const val KEY_HISTORY = "play_history"

        /** 播放进度前缀 */
        private const val KEY_PLAY_PROGRESS_PREFIX = "play_progress_"

        /** 播放配置键 */
        private const val KEY_PLAYER_CONFIG = "player_config"
    }

    // ==================== 视频列表管理 ====================

    /**
     * 保存视频到列表
     */
    suspend fun saveVideo(video: VideoItem) {
        val videos = getAllVideosList().toMutableList()
        val existingIndex = videos.indexOfFirst { it.id == video.id }
        if (existingIndex >= 0) {
            videos[existingIndex] = video
        } else {
            videos.add(video)
        }
        saveVideosList(videos)
    }

    /**
     * 获取所有视频列表（Flow）
     */
    fun getAllVideos(): Flow<List<VideoItem>> {
        return flowSettings.getStringFlow(KEY_VIDEOS, "")
            .map { jsonStr ->
                if (jsonStr.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        json.decodeFromString<List<VideoItem>>(jsonStr)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
    }

    /**
     * 获取所有视频列表（同步）
     */
    private fun getAllVideosList(): List<VideoItem> {
        val jsonStr = settings.getString(KEY_VIDEOS, "")
        return if (jsonStr.isEmpty()) {
            emptyList()
        } else {
            try {
                json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * 保存视频列表
     */
    private fun saveVideosList(videos: List<VideoItem>) {
        settings.putString(KEY_VIDEOS, json.encodeToString(videos))
    }

    /**
     * 删除视频
     */
    suspend fun deleteVideo(videoId: String) {
        val videos = getAllVideosList().filter { it.id != videoId }
        saveVideosList(videos)
    }

    // ==================== 播放进度管理 ====================

    /**
     * 保存播放进度
     */
    suspend fun savePlayProgress(videoId: String, position: Long, progress: Float) {
        settings.putLong("${KEY_PLAY_PROGRESS_PREFIX}${videoId}_position", position)
        settings.putFloat("${KEY_PLAY_PROGRESS_PREFIX}${videoId}_progress", progress)
    }

    /**
     * 获取播放进度
     */
    suspend fun getPlayProgress(videoId: String): Long {
        return settings.getLong("${KEY_PLAY_PROGRESS_PREFIX}${videoId}_position", 0L)
    }

    // ==================== 收藏管理 ====================

    /**
     * 添加到收藏
     */
    suspend fun addToFavorite(video: VideoItem) {
        val favorites = getFavoritesList().toMutableList()
        if (favorites.none { it.id == video.id }) {
            favorites.add(video.copy(isFavorite = true))
            saveFavoritesList(favorites)
        }
    }

    /**
     * 取消收藏
     */
    suspend fun removeFromFavorite(videoId: String) {
        val favorites = getFavoritesList().filter { it.id != videoId }
        saveFavoritesList(favorites)
    }

    /**
     * 获取收藏列表（Flow）
     */
    fun getFavorites(): Flow<List<VideoItem>> {
        return flowSettings.getStringFlow(KEY_FAVORITES, "")
            .map { jsonStr ->
                if (jsonStr.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        json.decodeFromString<List<VideoItem>>(jsonStr)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
    }

    /**
     * 获取收藏列表（同步）
     */
    private fun getFavoritesList(): List<VideoItem> {
        val jsonStr = settings.getString(KEY_FAVORITES, "")
        return if (jsonStr.isEmpty()) {
            emptyList()
        } else {
            try {
                json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * 保存收藏列表
     */
    private fun saveFavoritesList(favorites: List<VideoItem>) {
        settings.putString(KEY_FAVORITES, json.encodeToString(favorites))
    }

    /**
     * 检查是否已收藏
     */
    suspend fun isFavorite(videoId: String): Boolean {
        return getFavoritesList().any { it.id == videoId }
    }

    // ==================== 播放历史管理 ====================

    /**
     * 保存播放历史
     */
    suspend fun savePlayHistory(history: PlayHistory) {
        val historyList = getPlayHistoryList().toMutableList()
        // 移除同视频的旧记录
        historyList.removeAll { it.videoId == history.videoId }
        // 添加新记录到开头
        historyList.add(0, history)
        // 限制历史记录数量
        if (historyList.size > 100) {
            historyList.subList(100, historyList.size).clear()
        }
        saveHistoryList(historyList)
    }

    /**
     * 获取播放历史（Flow）
     */
    fun getPlayHistory(): Flow<List<PlayHistory>> {
        return flowSettings.getStringFlow(KEY_HISTORY, "")
            .map { jsonStr ->
                if (jsonStr.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        json.decodeFromString<List<PlayHistory>>(jsonStr)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
    }

    /**
     * 获取播放历史（同步）
     */
    private fun getPlayHistoryList(): List<PlayHistory> {
        val jsonStr = settings.getString(KEY_HISTORY, "")
        return if (jsonStr.isEmpty()) {
            emptyList()
        } else {
            try {
                json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                emptyList()
            }
        }
    }

    /**
     * 保存历史列表
     */
    private fun saveHistoryList(historyList: List<PlayHistory>) {
        settings.putString(KEY_HISTORY, json.encodeToString(historyList))
    }

    /**
     * 清空播放历史
     */
    suspend fun clearPlayHistory() {
        settings.remove(KEY_HISTORY)
    }

    /**
     * 删除单条播放历史
     */
    suspend fun deletePlayHistory(historyId: String) {
        val historyList = getPlayHistoryList().filter { it.id != historyId }
        saveHistoryList(historyList)
    }

    // ==================== 配置管理 ====================

    /**
     * 获取播放器配置
     */
    fun getPlayerConfig(): PlayerConfig {
        val jsonStr = settings.getString(KEY_PLAYER_CONFIG, "")
        return if (jsonStr.isEmpty()) {
            PlayerConfig()
        } else {
            try {
                json.decodeFromString(jsonStr)
            } catch (e: Exception) {
                PlayerConfig()
            }
        }
    }

    /**
     * 保存播放器配置
     */
    suspend fun savePlayerConfig(config: PlayerConfig) {
        settings.putString(KEY_PLAYER_CONFIG, json.encodeToString(config))
    }
}
