package com.mpvp.repository

import com.mpvp.model.MediaItem
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.map

/**
 * 内存媒体仓库基类
 *
 * 提供基于内存的通用实现，子类只需提供初始数据列表即可。
 * 适用于框架阶段的演示与本地数据，后续可替换为网络/数据库实现。
 *
 * @param T 媒体项类型
 * @property initialItems 初始数据
 */
abstract class InMemoryMediaRepository<T : MediaItem>(
    initialItems: List<T>
) : MediaRepository<T> {

    /** 可变数据源 */
    private val _items = MutableStateFlow(initialItems)

    override fun getAll(): Flow<List<T>> = _items.asStateFlow()

    override suspend fun getById(id: String): T? = _items.value.find { it.id == id }

    override suspend fun search(query: String): List<T> {
        val q = query.trim()
        return if (q.isEmpty()) {
            _items.value
        } else {
            _items.value.filter { it.title.contains(q, ignoreCase = true) }
        }
    }

    override fun getFavorites(): Flow<List<T>> = _items.map { list -> list.filter { it.isFavorite } }

    @Suppress("UNCHECKED_CAST")
    override suspend fun toggleFavorite(id: String) {
        _items.value = _items.value.map { item ->
            if (item.id == id) updateFavorite(item, !item.isFavorite) else item
        }
    }

    /**
     * 子类实现：返回更新收藏状态后的新实例
     */
    protected abstract fun updateFavorite(item: T, isFavorite: Boolean): T
}
