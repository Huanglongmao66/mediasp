package com.mpvp.viewmodel

import com.mpvp.model.MediaItem
import com.mpvp.model.UiState
import com.mpvp.repository.MediaRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 媒体列表页面状态
 *
 * @param T 媒体项类型
 * @property items 媒体列表
 * @property favorites 收藏列表
 * @property uiState UI状态
 * @property searchQuery 搜索关键词
 */
data class MediaListState<T : MediaItem>(
    val items: List<T> = emptyList(),
    val favorites: List<T> = emptyList(),
    val uiState: UiState<List<T>> = UiState.Loading,
    val searchQuery: String = ""
)

/**
 * 通用媒体列表ViewModel基类
 *
 * 为各媒体类型提供统一的列表加载、搜索、收藏管理能力。
 * 子类只需指定仓库类型即可获得完整功能。
 *
 * @param T 媒体项类型
 * @property repository 媒体仓库
 */
abstract class MediaListViewModel<T : MediaItem>(
    private val repository: MediaRepository<T>
) : BaseViewModel() {

    private val _state = MutableStateFlow(MediaListState<T>())
    val state: StateFlow<MediaListState<T>> = _state.asStateFlow()

    init {
        loadItems()
        loadFavorites()
    }

    /** 加载全部媒体项 */
    private fun loadItems() {
        launch {
            repository.getAll().collectLatest { items ->
                _state.value = _state.value.copy(
                    items = items,
                    uiState = if (items.isEmpty()) UiState.Empty else UiState.Success(items)
                )
            }
        }
    }

    /** 加载收藏列表 */
    private fun loadFavorites() {
        launch {
            repository.getFavorites().collectLatest { favorites ->
                _state.value = _state.value.copy(favorites = favorites)
            }
        }
    }

    /**
     * 更新搜索关键词并执行搜索
     *
     * @param query 搜索关键词
     */
    fun updateSearchQuery(query: String) {
        _state.value = _state.value.copy(searchQuery = query)
        launch {
            val result = repository.search(query)
            _state.value = _state.value.copy(
                uiState = if (result.isEmpty()) UiState.Empty else UiState.Success(result)
            )
        }
    }

    /**
     * 切换收藏状态
     *
     * @param id 媒体项ID
     */
    fun toggleFavorite(id: String) {
        launch {
            try {
                repository.toggleFavorite(id)
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    uiState = UiState.Error("收藏操作失败: ${e.message}")
                )
            }
        }
    }

    /**
     * 根据ID获取单个媒体项
     *
     * @param id 媒体项ID
     * @return 媒体项，不存在返回null
     */
    suspend fun getById(id: String): T? = repository.getById(id)

    /** 刷新数据 */
    fun refresh() {
        loadItems()
        loadFavorites()
    }
}
