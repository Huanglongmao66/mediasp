package com.mpvp.repository

import com.mpvp.model.MediaItem
import kotlinx.coroutines.flow.Flow

/**
 * 通用媒体仓库接口
 *
 * 为各媒体类型（音乐/图片/小说/电台）提供统一的数据访问抽象。
 * 新增媒体类型时，实现此接口并填充对应数据源即可。
 *
 * @param T 媒体项类型
 */
interface MediaRepository<T : MediaItem> {

    /**
     * 获取全部媒体项
     *
     * @return 媒体项列表流
     */
    fun getAll(): Flow<List<T>>

    /**
     * 根据ID获取单个媒体项
     *
     * @param id 媒体项ID
     * @return 媒体项，不存在返回null
     */
    suspend fun getById(id: String): T?

    /**
     * 搜索媒体项
     *
     * @param query 搜索关键词
     * @return 匹配的媒体项列表
     */
    suspend fun search(query: String): List<T>

    /**
     * 获取收藏列表
     *
     * @return 已收藏的媒体项列表流
     */
    fun getFavorites(): Flow<List<T>>

    /**
     * 切换收藏状态
     *
     * @param id 媒体项ID
     */
    suspend fun toggleFavorite(id: String)
}
