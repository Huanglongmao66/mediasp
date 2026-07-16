package com.mpvp.model

/**
 * 媒体内容基础接口
 *
 * 所有媒体类型（视频/音乐/图片/小说/电台）的统一抽象，
 * 提供通用字段，便于在通用列表、搜索、收藏等场景中复用。
 *
 * 新增媒体类型时，实现此接口并补充类型特有字段即可。
 */
interface MediaItem {
    /** 唯一标识 */
    val id: String

    /** 标题 */
    val title: String

    /** 封面图URL */
    val coverUrl: String?

    /** 来源地址（播放/阅读/查看地址） */
    val sourceUrl: String

    /** 媒体类型 */
    val type: MediaType

    /** 是否已收藏 */
    val isFavorite: Boolean
}
