package com.mpvp.viewmodel

import com.mpvp.model.PlayHistory
import com.mpvp.model.UiState
import com.mpvp.model.VideoItem
import com.mpvp.model.VideoSourceType
import com.mpvp.repository.VideoRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 视频列表页面状态
 *
 * @property videos 视频列表
 * @property favorites 收藏列表
 * @property history 播放历史
 * @property uiState UI状态
 * @property currentTab 当前选中的标签
 * @property isScanning 是否正在扫描
 */
data class VideoListState(
    val videos: List<VideoItem> = emptyList(),
    val favorites: List<VideoItem> = emptyList(),
    val history: List<PlayHistory> = emptyList(),
    val uiState: UiState<List<VideoItem>> = UiState.Loading,
    val currentTab: VideoTab = VideoTab.LOCAL,
    val isScanning: Boolean = false
)

/**
 * 视频标签枚举
 */
enum class VideoTab(val title: String) {
    LOCAL("本地"),
    FAVORITE("收藏"),
    HISTORY("历史"),
    ONLINE("在线")
}

/**
 * 视频列表ViewModel
 *
 * 管理视频列表页面的状态和数据，包括：
 * - 加载视频列表
 * - 切换标签页
 * - 扫描本地视频
 * - 收藏管理
 * - 历史记录管理
 *
 * @property repository 视频仓库
 */
class VideoListViewModel(
    private val repository: VideoRepository
) : BaseViewModel() {

    /** 页面状态 */
    private val _state = MutableStateFlow(VideoListState())
    val state: StateFlow<VideoListState> = _state.asStateFlow()

    init {
        loadVideos()
        loadFavorites()
        loadHistory()
    }

    /**
     * 加载视频列表
     */
    private fun loadVideos() {
        launch {
            repository.getAllVideos().collectLatest { videos ->
                _state.value = _state.value.copy(
                    videos = videos,
                    uiState = if (videos.isEmpty()) {
                        UiState.Empty
                    } else {
                        UiState.Success(videos)
                    }
                )
            }
        }
    }

    /**
     * 加载收藏列表
     */
    private fun loadFavorites() {
        launch {
            repository.getFavorites().collectLatest { favorites ->
                _state.value = _state.value.copy(favorites = favorites)
            }
        }
    }

    /**
     * 加载播放历史
     */
    private fun loadHistory() {
        launch {
            repository.getPlayHistory().collectLatest { history ->
                _state.value = _state.value.copy(history = history)
            }
        }
    }

    /**
     * 切换标签页
     *
     * @param tab 目标标签
     */
    fun switchTab(tab: VideoTab) {
        _state.value = _state.value.copy(currentTab = tab)
    }

    /**
     * 扫描本地视频
     *
     * @param directory 指定目录
     */
    fun scanLocalVideos(directory: String? = null) {
        launch {
            _state.value = _state.value.copy(isScanning = true)
            try {
                repository.scanLocalVideos(directory).collectLatest { videos ->
                    // 保存扫描到的视频
                    videos.forEach { video ->
                        repository.updateVideo(video)
                    }
                    _state.value = _state.value.copy(
                        isScanning = false,
                        uiState = UiState.Success(videos)
                    )
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isScanning = false,
                    uiState = UiState.Error("扫描失败: ${e.message}")
                )
            }
        }
    }

    /**
     * 添加网络视频
     *
     * 添加成功后从仓库重新获取视频列表，确保状态一致
     *
     * @param url 视频URL
     * @param title 视频标题
     * @param coverUrl 封面URL
     */
    fun addNetworkVideo(url: String, title: String, coverUrl: String? = null) {
        launch {
            try {
                _state.value = _state.value.copy(uiState = UiState.Loading)
                repository.addNetworkVideo(url, title, coverUrl)
                // 添加成功后重新加载列表，确保状态一致
                refresh()
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    uiState = UiState.Error("添加失败: ${e.message}")
                )
            }
        }
    }

    /**
     * 切换收藏状态
     *
     * @param video 视频对象
     */
    fun toggleFavorite(video: VideoItem) {
        launch {
            try {
                if (video.isFavorite) {
                    repository.removeFromFavorite(video.id)
                } else {
                    repository.addToFavorite(video)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    uiState = UiState.Error("收藏操作失败: ${e.message}")
                )
            }
        }
    }

    /**
     * 删除视频
     *
     * @param videoId 视频ID
     */
    fun deleteVideo(videoId: String) {
        launch {
            repository.deleteVideo(videoId)
        }
    }

    /**
     * 删除播放历史
     *
     * @param historyId 历史记录ID
     */
    fun deleteHistory(historyId: String) {
        launch {
            repository.deletePlayHistory(historyId)
        }
    }

    /**
     * 清空播放历史
     */
    fun clearHistory() {
        launch {
            repository.clearPlayHistory()
        }
    }

    /**
     * 根据类型获取视频
     *
     * @param sourceType 视频来源类型
     * @return 过滤后的视频列表
     */
    fun getVideosByType(sourceType: VideoSourceType): List<VideoItem> {
        return _state.value.videos.filter { it.sourceType == sourceType }
    }

    /**
     * 搜索视频
     *
     * @param query 搜索关键词
     * @return 搜索结果
     */
    fun searchVideos(query: String): List<VideoItem> {
        return if (query.isBlank()) {
            _state.value.videos
        } else {
            _state.value.videos.filter {
                it.title.contains(query, ignoreCase = true)
            }
        }
    }

    /**
     * 刷新数据
     */
    fun refresh() {
        loadVideos()
        loadFavorites()
        loadHistory()
    }
}
