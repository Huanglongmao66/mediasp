package com.mpvp.model

import kotlinx.serialization.Serializable

/**
 * 电台项数据模型
 *
 * @property id 唯一标识
 * @property title 电台名称
 * @property frequency 频率描述（如 "FM 98.5"）
 * @property bitrate 码率（kbps）
 * @property isLive 是否为直播流
 * @property category 分类（如 新闻/音乐/交通）
 * @property coverUrl 封面URL
 * @property sourceUrl 直播流地址
 * @property isFavorite 是否收藏
 */
@Serializable
data class RadioItem(
    override val id: String,
    override val title: String,
    val frequency: String = "",
    val bitrate: Int = 128,
    val isLive: Boolean = true,
    val category: String = "",
    override val coverUrl: String? = null,
    override val sourceUrl: String,
    override val isFavorite: Boolean = false
) : MediaItem {
    override val type: MediaType = MediaType.RADIO
}
