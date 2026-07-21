package com.mpvp.viewmodel

import com.mpvp.model.MediaPlaylist
import com.mpvp.model.MediaPlaylistItem
import com.mpvp.model.PlayMode
import com.mpvp.model.SubscriptionSource
import com.mpvp.repository.AppDataStore
import com.mpvp.utils.PlaylistFormatConverter
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.datetime.Clock
import kotlinx.coroutines.launch

/**
 * 订阅源状态
 */
data class SubscriptionState(
    val sources: List<SubscriptionSource> = emptyList(),
    val isLoading: Boolean = false
)

/**
 * 订阅源ViewModel
 *
 * 管理订阅源的增删改查，支持按媒体类型添加自定义订阅源。
 */
class SubscriptionViewModel(
    private val dataStore: AppDataStore
) : BaseViewModel() {

    private val _state = MutableStateFlow(SubscriptionState(isLoading = true))
    val state: StateFlow<SubscriptionState> = _state.asStateFlow()

    init {
        loadSources()
    }

    private fun loadSources() {
        launch {
            dataStore.getSubscriptionSources().collectLatest { sources ->
                _state.value = _state.value.copy(
                    sources = sources,
                    isLoading = false
                )
            }
        }
    }

    /**
     * 添加订阅源
     */
    fun addSource(name: String, url: String, type: com.mpvp.model.MediaType, description: String, apiKey: String? = null) {
        launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val source = SubscriptionSource(
                id = "sub_${now}",
                name = name,
                url = url,
                type = type,
                description = description,
                apiKey = apiKey?.takeIf { it.isNotBlank() },
                createdAt = now,
                updatedAt = now
            )
            dataStore.addSubscriptionSource(source)
        }
    }

    /**
     * 删除订阅源
     */
    fun deleteSource(sourceId: String) {
        launch {
            dataStore.deleteSubscriptionSource(sourceId)
        }
    }

    /**
     * 切换订阅源启用状态
     */
    fun toggleSource(source: SubscriptionSource) {
        launch {
            dataStore.updateSubscriptionSource(source.copy(enabled = !source.enabled))
        }
    }

    /**
     * 更新订阅源
     */
    fun updateSource(source: SubscriptionSource) {
        launch {
            dataStore.updateSubscriptionSource(source.copy(updatedAt = Clock.System.now().toEpochMilliseconds()))
        }
    }
}

/**
 * 播放列表状态
 */
data class PlaylistState(
    val playlists: List<MediaPlaylist> = emptyList(),
    val currentPlaylist: MediaPlaylist? = null,
    val isLoading: Boolean = false
)

/**
 * 播放列表ViewModel
 *
 * 管理自定义播放列表的创建、编辑、删除，支持跨媒体类型混合列表。
 */
class PlaylistViewModel(
    private val dataStore: AppDataStore
) : BaseViewModel() {

    private val _state = MutableStateFlow(PlaylistState(isLoading = true))
    val state: StateFlow<PlaylistState> = _state.asStateFlow()

    init {
        loadPlaylists()
    }

    private fun loadPlaylists() {
        launch {
            dataStore.getMediaPlaylists().collectLatest { playlists ->
                _state.value = _state.value.copy(
                    playlists = playlists,
                    isLoading = false
                )
            }
        }
    }

    /**
     * 创建播放列表
     */
    fun createPlaylist(name: String, description: String = "") {
        launch {
            val now = Clock.System.now().toEpochMilliseconds()
            val playlist = MediaPlaylist(
                id = "playlist_${now}",
                name = name,
                description = description,
                createdAt = now,
                updatedAt = now
            )
            dataStore.addMediaPlaylist(playlist)
        }
    }

    /**
     * 删除播放列表
     */
    fun deletePlaylist(playlistId: String) {
        launch {
            dataStore.deleteMediaPlaylist(playlistId)
        }
    }

    /**
     * 重命名播放列表
     */
    fun renamePlaylist(playlistId: String, newName: String) {
        launch {
            val playlist = dataStore.getMediaPlaylistById(playlistId)
            playlist?.let {
                dataStore.updateMediaPlaylist(it.copy(name = newName, updatedAt = Clock.System.now().toEpochMilliseconds()))
            }
        }
    }

    /**
     * 向播放列表添加媒体项
     */
    fun addItemToPlaylist(playlistId: String, item: MediaPlaylistItem) {
        launch {
            val playlist = dataStore.getMediaPlaylistById(playlistId)
            playlist?.let {
                val newItems = it.items.toMutableList()
                // 避免重复添加
                if (newItems.none { i -> i.mediaId == item.mediaId && i.mediaType == item.mediaType }) {
                    newItems.add(item.copy(sortOrder = newItems.size))
                    dataStore.updateMediaPlaylist(it.copy(items = newItems, updatedAt = Clock.System.now().toEpochMilliseconds()))
                }
            }
        }
    }

    /**
     * 从播放列表移除媒体项
     */
    fun removeItemFromPlaylist(playlistId: String, itemId: String) {
        launch {
            val playlist = dataStore.getMediaPlaylistById(playlistId)
            playlist?.let {
                val newItems = it.items.filter { i -> i.id != itemId }
                dataStore.updateMediaPlaylist(it.copy(items = newItems, updatedAt = Clock.System.now().toEpochMilliseconds()))
            }
        }
    }

    /**
     * 切换播放模式
     */
    fun togglePlayMode(playlistId: String) {
        launch {
            val playlist = dataStore.getMediaPlaylistById(playlistId)
            playlist?.let {
                val modes = PlayMode.entries
                val nextIndex = (modes.indexOf(it.playMode) + 1) % modes.size
                dataStore.updateMediaPlaylist(it.copy(playMode = modes[nextIndex], updatedAt = Clock.System.now().toEpochMilliseconds()))
            }
        }
    }

    /**
     * 加载播放列表详情
     */
    fun loadPlaylist(playlistId: String) {
        launch {
            val playlist = dataStore.getMediaPlaylistById(playlistId)
            _state.value = _state.value.copy(currentPlaylist = playlist)
        }
    }

    /**
     * 更新播放列表封面
     */
    fun updatePlaylistCover(playlistId: String, coverUrl: String?) {
        launch {
            val playlist = dataStore.getMediaPlaylistById(playlistId)
            playlist?.let {
                dataStore.updateMediaPlaylist(it.copy(coverUrl = coverUrl, updatedAt = Clock.System.now().toEpochMilliseconds()))
            }
        }
    }

    // ======================== 导入导出功能 ========================

    /**
     * 导入播放列表（自动检测格式）
     *
     * @param content 文件内容
     * @param format 导入格式（AUTO/M3U/XSPF）
     * @param name 播放列表名称
     * @return 导入的播放列表ID，失败返回null
     */
    fun importPlaylist(content: String, format: PlaylistFormatConverter.ImportFormat = PlaylistFormatConverter.ImportFormat.AUTO, name: String = "导入的播放列表"): String? {
        val playlist = when (format) {
            PlaylistFormatConverter.ImportFormat.AUTO -> PlaylistFormatConverter.importAuto(content, name)
            PlaylistFormatConverter.ImportFormat.M3U -> PlaylistFormatConverter.importToMediaPlaylist(content, name)
            PlaylistFormatConverter.ImportFormat.XSPF -> PlaylistFormatConverter.importToMediaPlaylistFromXspf(content, name)
            PlaylistFormatConverter.ImportFormat.JSON -> PlaylistFormatConverter.importAuto(content, name)
        }

        if (playlist.items.isEmpty()) return null

        launch {
            dataStore.addMediaPlaylist(playlist)
        }
        return playlist.id
    }

    /**
     * 导出播放列表
     *
     * @param playlistId 播放列表ID
     * @param format 导出格式
     * @param callback 回调，返回导出的文件内容（失败返回null）
     */
    fun exportPlaylist(playlistId: String, format: PlaylistFormatConverter.ExportFormat, callback: (String?) -> Unit) {
        launch {
            val playlist = dataStore.getMediaPlaylistById(playlistId)
            if (playlist == null) {
                callback(null)
                return@launch
            }
            val result = when (format) {
                PlaylistFormatConverter.ExportFormat.M3U -> PlaylistFormatConverter.exportToM3u(playlist)
                PlaylistFormatConverter.ExportFormat.XSPF -> PlaylistFormatConverter.exportToXspf(playlist)
                PlaylistFormatConverter.ExportFormat.JSON -> PlaylistFormatConverter.exportToJson(playlist)
            }
            callback(result)
        }
    }

    /**
     * 导出所有播放列表为 JSON
     */
    fun exportAllPlaylists(): String {
        val playlists = state.value.playlists
        val sb = StringBuilder()
        sb.append("[\n")
        playlists.forEachIndexed { index, playlist ->
            sb.append(PlaylistFormatConverter.exportToJson(playlist))
            if (index < playlists.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("]")
        return sb.toString()
    }
}
