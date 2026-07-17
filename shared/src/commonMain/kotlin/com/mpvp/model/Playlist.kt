package com.mpvp.model

import kotlinx.serialization.Serializable

/**
 * 播放列表数据类
 *
 * 管理一组视频的播放顺序和状态
 *
 * @property id 播放列表唯一标识
 * @property name 播放列表名称
 * @property items 视频列表
 * @property currentIndex 当前播放索引
 * @property playMode 播放模式
 * @property createTime 创建时间
 */
@Serializable
data class Playlist(
    val id: String,
    val name: String,
    val items: List<VideoItem> = emptyList(),
    val currentIndex: Int = 0,
    val playMode: PlayMode = PlayMode.LIST_ORDER,
    val createTime: Long = kotlinx.datetime.Clock.System.now().toEpochMilliseconds()
) {
    /**
     * 获取当前播放的视频
     */
    fun getCurrentVideo(): VideoItem? = items.getOrNull(currentIndex)

    /**
     * 视频数量
     */
    fun size(): Int = items.size

    /**
     * 是否为空
     */
    fun isEmpty(): Boolean = items.isEmpty()

    /**
     * 是否有下一个视频
     */
    fun hasNext(): Boolean = items.isNotEmpty() && when (playMode) {
        PlayMode.LIST_ORDER -> currentIndex < items.size - 1
        PlayMode.SINGLE_REPEAT -> true
        PlayMode.LIST_REPEAT -> true
        PlayMode.RANDOM -> items.size > 1
    }

    /**
     * 是否有上一个视频
     */
    fun hasPrevious(): Boolean = items.isNotEmpty() && when (playMode) {
        PlayMode.LIST_ORDER -> currentIndex > 0
        PlayMode.SINGLE_REPEAT -> true
        PlayMode.LIST_REPEAT -> true
        PlayMode.RANDOM -> items.size > 1
    }
}

/**
 * 播放模式枚举
 */
@Serializable
enum class PlayMode(val displayName: String) {
    /** 顺序播放 */
    LIST_ORDER("顺序播放"),

    /** 单曲循环 */
    SINGLE_REPEAT("单曲循环"),

    /** 列表循环 */
    LIST_REPEAT("列表循环"),

    /** 随机播放 */
    RANDOM("随机播放")
}
