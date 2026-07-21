package com.mpvp.player

import com.mpvp.model.PlayMode
import com.mpvp.model.Playlist
import com.mpvp.model.VideoItem
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlin.random.Random

/**
 * 播放列表管理器
 *
 * 管理播放列表的播放顺序、模式切换、上一首/下一首切换等
 */
class PlaylistManager {

    /** 当前播放列表 */
    private val _playlist = MutableStateFlow(Playlist(id = "default", name = "默认列表"))
    val playlist: StateFlow<Playlist> = _playlist.asStateFlow()

    /**
     * 设置播放列表
     *
     * @param items 视频列表
     * @param startIndex 起始播放索引
     */
    fun setPlaylist(items: List<VideoItem>, startIndex: Int = 0) {
        val validIndex = startIndex.coerceIn(0, (items.size - 1).coerceAtLeast(0))
        _playlist.value = _playlist.value.copy(
            items = items,
            currentIndex = validIndex
        )
    }

    /**
     * 添加视频到播放列表
     */
    fun addVideo(video: VideoItem) {
        val currentItems = _playlist.value.items.toMutableList()
        currentItems.add(video)
        _playlist.value = _playlist.value.copy(items = currentItems)
    }

    /**
     * 从播放列表移除视频
     */
    fun removeVideo(index: Int) {
        if (index < 0 || index >= _playlist.value.items.size) return
        val currentItems = _playlist.value.items.toMutableList()
        currentItems.removeAt(index)
        var newIndex = _playlist.value.currentIndex
        if (index < newIndex) {
            newIndex--
        } else if (index == newIndex && newIndex >= currentItems.size) {
            newIndex = (currentItems.size - 1).coerceAtLeast(0)
        }
        _playlist.value = _playlist.value.copy(
            items = currentItems,
            currentIndex = newIndex
        )
    }

    /**
     * 清空播放列表
     */
    fun clear() {
        _playlist.value = _playlist.value.copy(
            items = emptyList(),
            currentIndex = 0
        )
    }

    /**
     * 获取当前播放的视频
     */
    fun getCurrentVideo(): VideoItem? = _playlist.value.getCurrentVideo()

    /**
     * 跳转到指定索引
     */
    fun skipTo(index: Int): VideoItem? {
        val items = _playlist.value.items
        if (items.isEmpty()) return null
        val validIndex = index.coerceIn(0, items.size - 1)
        _playlist.value = _playlist.value.copy(currentIndex = validIndex)
        return items[validIndex]
    }

    /**
     * 播放下一个视频
     *
     * @return 下一个视频，如果没有则返回null
     */
    fun playNext(): VideoItem? {
        val items = _playlist.value.items
        if (items.isEmpty()) return null

        val nextIndex = when (_playlist.value.playMode) {
            PlayMode.LIST_ORDER -> {
                val next = _playlist.value.currentIndex + 1
                if (next >= items.size) return null
                next
            }
            PlayMode.SINGLE_REPEAT -> _playlist.value.currentIndex
            PlayMode.LIST_REPEAT -> {
                (_playlist.value.currentIndex + 1) % items.size
            }
            PlayMode.RANDOM -> {
                if (items.size <= 1) return null
                var randomIndex: Int
                do {
                    randomIndex = Random.nextInt(items.size)
                } while (randomIndex == _playlist.value.currentIndex)
                randomIndex
            }
        }

        _playlist.value = _playlist.value.copy(currentIndex = nextIndex)
        return items[nextIndex]
    }

    /**
     * 播放上一个视频
     *
     * @return 上一个视频，如果没有则返回null
     */
    fun playPrevious(): VideoItem? {
        val items = _playlist.value.items
        if (items.isEmpty()) return null

        val prevIndex = when (_playlist.value.playMode) {
            PlayMode.LIST_ORDER -> {
                val prev = _playlist.value.currentIndex - 1
                if (prev < 0) return null
                prev
            }
            PlayMode.SINGLE_REPEAT -> _playlist.value.currentIndex
            PlayMode.LIST_REPEAT -> {
                val prev = _playlist.value.currentIndex - 1
                if (prev < 0) items.size - 1 else prev
            }
            PlayMode.RANDOM -> {
                if (items.size <= 1) return null
                var randomIndex: Int
                do {
                    randomIndex = Random.nextInt(items.size)
                } while (randomIndex == _playlist.value.currentIndex)
                randomIndex
            }
        }

        _playlist.value = _playlist.value.copy(currentIndex = prevIndex)
        return items[prevIndex]
    }

    /**
     * 切换播放模式
     */
    fun togglePlayMode(): PlayMode {
        val modes = PlayMode.values()
        val currentOrdinal = _playlist.value.playMode.ordinal
        val nextOrdinal = (currentOrdinal + 1) % modes.size
        val nextMode = modes[nextOrdinal]
        _playlist.value = _playlist.value.copy(playMode = nextMode)
        return nextMode
    }

    /**
     * 设置播放模式
     */
    fun setPlayMode(mode: PlayMode) {
        _playlist.value = _playlist.value.copy(playMode = mode)
    }

    /**
     * 获取当前播放模式
     */
    fun getPlayMode(): PlayMode = _playlist.value.playMode
}
