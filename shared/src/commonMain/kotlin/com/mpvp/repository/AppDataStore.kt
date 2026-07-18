package com.mpvp.repository

import com.mpvp.model.DownloadTask
import com.mpvp.model.MediaPlaylist
import com.mpvp.model.PlayHistory
import com.mpvp.model.PlayerConfig
import com.mpvp.model.SubscriptionSource
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

        /** 订阅源列表键 */
        private const val KEY_SUBSCRIPTIONS = "subscription_sources"

        /** 播放列表键 */
        private const val KEY_PLAYLISTS = "media_playlists"

        /** 下载任务键 */
        private const val KEY_DOWNLOADS = "download_tasks"
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

    // ==================== 订阅源管理 ====================

    /**
     * 获取所有订阅源（Flow）
     */
    fun getSubscriptionSources(): Flow<List<SubscriptionSource>> {
        return flowSettings.getStringFlow(KEY_SUBSCRIPTIONS, "")
            .map { jsonStr ->
                if (jsonStr.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        json.decodeFromString<List<SubscriptionSource>>(jsonStr)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
    }

    /**
     * 获取所有订阅源（同步）
     */
    private fun getSubscriptionSourcesList(): List<SubscriptionSource> {
        val jsonStr = settings.getString(KEY_SUBSCRIPTIONS, "")
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
     * 保存订阅源列表
     */
    private fun saveSubscriptionSourcesList(sources: List<SubscriptionSource>) {
        settings.putString(KEY_SUBSCRIPTIONS, json.encodeToString(sources))
    }

    /**
     * 添加订阅源
     */
    suspend fun addSubscriptionSource(source: SubscriptionSource) {
        val list = getSubscriptionSourcesList().toMutableList()
        val existingIndex = list.indexOfFirst { it.id == source.id }
        if (existingIndex >= 0) {
            list[existingIndex] = source
        } else {
            list.add(source)
        }
        saveSubscriptionSourcesList(list)
    }

    /**
     * 删除订阅源
     */
    suspend fun deleteSubscriptionSource(sourceId: String) {
        val list = getSubscriptionSourcesList().filter { it.id != sourceId }
        saveSubscriptionSourcesList(list)
    }

    /**
     * 更新订阅源
     */
    suspend fun updateSubscriptionSource(source: SubscriptionSource) {
        val list = getSubscriptionSourcesList().toMutableList()
        val index = list.indexOfFirst { it.id == source.id }
        if (index >= 0) {
            list[index] = source
            saveSubscriptionSourcesList(list)
        }
    }

    // ==================== 播放列表管理 ====================

    /**
     * 获取所有播放列表（Flow）
     */
    fun getMediaPlaylists(): Flow<List<MediaPlaylist>> {
        return flowSettings.getStringFlow(KEY_PLAYLISTS, "")
            .map { jsonStr ->
                if (jsonStr.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        json.decodeFromString<List<MediaPlaylist>>(jsonStr)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
    }

    /**
     * 获取所有播放列表（同步）
     */
    private fun getMediaPlaylistsList(): List<MediaPlaylist> {
        val jsonStr = settings.getString(KEY_PLAYLISTS, "")
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
     * 保存播放列表列表
     */
    private fun saveMediaPlaylistsList(playlists: List<MediaPlaylist>) {
        settings.putString(KEY_PLAYLISTS, json.encodeToString(playlists))
    }

    /**
     * 添加播放列表
     */
    suspend fun addMediaPlaylist(playlist: MediaPlaylist) {
        val list = getMediaPlaylistsList().toMutableList()
        val existingIndex = list.indexOfFirst { it.id == playlist.id }
        if (existingIndex >= 0) {
            list[existingIndex] = playlist
        } else {
            list.add(playlist)
        }
        saveMediaPlaylistsList(list)
    }

    /**
     * 删除播放列表
     */
    suspend fun deleteMediaPlaylist(playlistId: String) {
        val list = getMediaPlaylistsList().filter { it.id != playlistId }
        saveMediaPlaylistsList(list)
    }

    /**
     * 更新播放列表
     */
    suspend fun updateMediaPlaylist(playlist: MediaPlaylist) {
        val list = getMediaPlaylistsList().toMutableList()
        val index = list.indexOfFirst { it.id == playlist.id }
        if (index >= 0) {
            list[index] = playlist
            saveMediaPlaylistsList(list)
        }
    }

    /**
     * 根据ID获取播放列表
     */
    suspend fun getMediaPlaylistById(playlistId: String): MediaPlaylist? {
        return getMediaPlaylistsList().find { it.id == playlistId }
    }

    // ==================== 下载任务管理 ====================

    /**
     * 获取所有下载任务（Flow）
     */
    fun getDownloadTasks(): Flow<List<DownloadTask>> {
        return flowSettings.getStringFlow(KEY_DOWNLOADS, "")
            .map { jsonStr ->
                if (jsonStr.isEmpty()) {
                    emptyList()
                } else {
                    try {
                        json.decodeFromString<List<DownloadTask>>(jsonStr)
                    } catch (e: Exception) {
                        emptyList()
                    }
                }
            }
    }

    /**
     * 获取所有下载任务（同步）
     */
    fun getDownloadTasksList(): List<DownloadTask> {
        val jsonStr = settings.getString(KEY_DOWNLOADS, "")
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
     * 保存下载任务列表
     */
    fun saveDownloadTasksList(tasks: List<DownloadTask>) {
        settings.putString(KEY_DOWNLOADS, json.encodeToString(tasks))
    }

    /**
     * 添加下载任务
     */
    suspend fun addDownloadTask(task: DownloadTask) {
        val list = getDownloadTasksList().toMutableList()
        val existingIndex = list.indexOfFirst { it.id == task.id }
        if (existingIndex >= 0) {
            list[existingIndex] = task
        } else {
            list.add(task)
        }
        saveDownloadTasksList(list)
    }

    /**
     * 删除下载任务
     */
    suspend fun deleteDownloadTask(taskId: String) {
        val list = getDownloadTasksList().filter { it.id != taskId }
        saveDownloadTasksList(list)
    }

    /**
     * 更新下载任务
     */
    suspend fun updateDownloadTask(task: DownloadTask) {
        val list = getDownloadTasksList().toMutableList()
        val index = list.indexOfFirst { it.id == task.id }
        if (index >= 0) {
            list[index] = task
            saveDownloadTasksList(list)
        }
    }
}
