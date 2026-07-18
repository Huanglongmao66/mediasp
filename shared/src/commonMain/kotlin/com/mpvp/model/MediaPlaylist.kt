package com.mpvp.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * 通用播放列表项数据模型
 *
 * 支持在同一个播放列表中混合不同媒体类型（视频/音乐/图片/小说/电台）。
 * 通过 [mediaType] 区分类型，通过 [mediaId] 关联具体媒体项，
 * 同时缓存标题和封面用于快速展示。
 *
 * @property id 播放列表项唯一标识
 * @property mediaId 关联的媒体项ID
 * @property mediaType 媒体类型
 * @property title 标题（缓存）
 * @property coverUrl 封面URL（缓存）
 * @property sourceUrl 来源地址（缓存）
 * @property addedAt 添加到列表的时间
 * @property sortOrder 排序序号
 */
@Serializable
data class MediaPlaylistItem(
    val id: String,
    val mediaId: String,
    val mediaType: MediaType,
    val title: String,
    val coverUrl: String? = null,
    val sourceUrl: String = "",
    val addedAt: Long = Clock.System.now().toEpochMilliseconds(),
    val sortOrder: Int = 0
)

/**
 * 通用播放列表数据模型
 *
 * 支持跨媒体类型的混合播放列表，用户可创建自定义播放列表，
 * 将视频、音乐、电台等不同类型媒体加入同一列表中。
 *
 * @property id 播放列表唯一标识
 * @property name 播放列表名称
 * @property description 播放列表描述
 * @property items 播放列表项
 * @property coverUrl 播放列表封面（取首个项目封面或自定义）
 * @property playMode 播放模式
 * @property currentIndex 当前播放索引
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
@Serializable
data class MediaPlaylist(
    val id: String,
    val name: String,
    val description: String = "",
    val items: List<MediaPlaylistItem> = emptyList(),
    val coverUrl: String? = null,
    val playMode: PlayMode = PlayMode.LIST_ORDER,
    val currentIndex: Int = 0,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
) {
    /** 播放列表项数量 */
    fun size(): Int = items.size

    /** 是否为空 */
    fun isEmpty(): Boolean = items.isEmpty()

    /** 获取当前播放项 */
    fun getCurrentItem(): MediaPlaylistItem? = items.getOrNull(currentIndex)

    /** 是否有下一个 */
    fun hasNext(): Boolean = items.isNotEmpty() && when (playMode) {
        PlayMode.LIST_ORDER -> currentIndex < items.size - 1
        PlayMode.SINGLE_REPEAT -> true
        PlayMode.LIST_REPEAT -> true
        PlayMode.RANDOM -> items.size > 1
    }

    /** 是否有上一个 */
    fun hasPrevious(): Boolean = items.isNotEmpty() && when (playMode) {
        PlayMode.LIST_ORDER -> currentIndex > 0
        PlayMode.SINGLE_REPEAT -> true
        PlayMode.LIST_REPEAT -> true
        PlayMode.RANDOM -> items.size > 1
    }

    /** 获取列表封面（优先自定义封面，其次取首个项封面） */
    fun effectiveCoverUrl(): String? = coverUrl ?: items.firstOrNull()?.coverUrl
}
