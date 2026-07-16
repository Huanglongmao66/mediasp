package com.mpvp.player

import com.mpvp.model.DanmakuDisplayState
import com.mpvp.model.DanmakuItem
import com.mpvp.model.DanmakuType
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

class DanmakuManager {

    private val danmakuList = mutableListOf<DanmakuItem>()

    private val _displayDanmakus = MutableStateFlow<List<DanmakuDisplayState>>(emptyList())
    val displayDanmakus: StateFlow<List<DanmakuDisplayState>> = _displayDanmakus.asStateFlow()

    private val _enabled = MutableStateFlow(true)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _opacity = MutableStateFlow(0.8f)
    val opacity: StateFlow<Float> = _opacity.asStateFlow()

    private val _speed = MutableStateFlow(1.0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    private val _displayAreaRatio = MutableStateFlow(0.4f)
    val displayAreaRatio: StateFlow<Float> = _displayAreaRatio.asStateFlow()

    private var displayWidth: Float = 0f
    private var displayHeight: Float = 0f

    private val trackHeight = 36f
    private val scrollTracks = mutableListOf<TrackState>()
    private val topTracks = mutableListOf<TrackState>()
    private val bottomTracks = mutableListOf<TrackState>()

    private val scope = CoroutineScope(Dispatchers.Main)
    private var animationJob: Job? = null
    private var currentTime: Float = 0f

    private val fixedDanmakuDuration = 3000L
    private val scrollDanmakuDuration = 8000L

    data class TrackState(
        var lastUsedTime: Long = 0L,
        var occupyingDanmakuId: String? = null
    )

    fun setDisplaySize(width: Float, height: Float) {
        displayWidth = width
        displayHeight = height
        initTracks()
    }

    private fun initTracks() {
        val maxTracks = ((displayHeight * _displayAreaRatio.value) / trackHeight).toInt().coerceAtLeast(3)
        scrollTracks.clear()
        topTracks.clear()
        bottomTracks.clear()
        repeat(maxTracks) {
            scrollTracks.add(TrackState())
            topTracks.add(TrackState())
            bottomTracks.add(TrackState())
        }
    }

    fun loadDanmakus(items: List<DanmakuItem>) {
        danmakuList.clear()
        danmakuList.addAll(items.sortedBy { it.time })
    }

    fun addDanmaku(item: DanmakuItem) {
        danmakuList.add(item)
        danmakuList.sortBy { it.time }
    }

    fun sendDanmaku(content: String, color: Long = 0xFFFFFF, type: DanmakuType = DanmakuType.SCROLL) {
        val item = DanmakuItem(
            id = "local_${System.currentTimeMillis()}_${Math.random().hashCode()}",
            content = content,
            time = currentTime,
            type = type,
            color = color
        )
        addDanmaku(item)
    }

    fun updateTime(time: Float) {
        currentTime = time
        if (_enabled.value) {
            addNewDanmakus(time)
        }
    }

    private fun addNewDanmakus(time: Float) {
        val newItems = danmakuList.filter {
            it.time <= time && it.time > time - 0.3f
        }.filter { item ->
            !_displayDanmakus.value.any { it.id == item.id }
        }

        newItems.forEach { item ->
            val displayState = createDisplayState(item)
            if (displayState != null) {
                val currentList = _displayDanmakus.value.toMutableList()
                currentList.add(displayState)
                _displayDanmakus.value = currentList
            }
        }
    }

    private fun createDisplayState(item: DanmakuItem): DanmakuDisplayState? {
        val fontSize = (item.fontSize * _fontScale.value).toInt().coerceIn(12, 48)

        return when (item.type) {
            DanmakuType.SCROLL -> {
                val trackIndex = findAvailableScrollTrack()
                if (trackIndex < 0) return null
                val y = trackIndex * trackHeight + 10f
                scrollTracks[trackIndex].lastUsedTime = System.currentTimeMillis()
                scrollTracks[trackIndex].occupyingDanmakuId = item.id
                DanmakuDisplayState(
                    id = item.id,
                    content = item.content,
                    x = displayWidth,
                    y = y,
                    color = item.color,
                    fontSize = fontSize,
                    type = item.type
                )
            }
            DanmakuType.TOP -> {
                val trackIndex = findAvailableTopTrack()
                if (trackIndex < 0) return null
                val y = trackIndex * trackHeight + 10f
                topTracks[trackIndex].lastUsedTime = System.currentTimeMillis()
                topTracks[trackIndex].occupyingDanmakuId = item.id
                DanmakuDisplayState(
                    id = item.id,
                    content = item.content,
                    x = displayWidth / 2f,
                    y = y,
                    color = item.color,
                    fontSize = fontSize,
                    type = item.type
                )
            }
            DanmakuType.BOTTOM -> {
                val trackIndex = findAvailableBottomTrack()
                if (trackIndex < 0) return null
                val y = displayHeight - (trackIndex + 1) * trackHeight - 10f
                bottomTracks[trackIndex].lastUsedTime = System.currentTimeMillis()
                bottomTracks[trackIndex].occupyingDanmakuId = item.id
                DanmakuDisplayState(
                    id = item.id,
                    content = item.content,
                    x = displayWidth / 2f,
                    y = y,
                    color = item.color,
                    fontSize = fontSize,
                    type = item.type
                )
            }
        }
    }

    private fun findAvailableScrollTrack(): Int {
        val now = System.currentTimeMillis()
        val minInterval = (scrollDanmakuDuration / _speed.value).toLong()

        for (i in scrollTracks.indices) {
            val track = scrollTracks[i]
            if (now - track.lastUsedTime > minInterval) {
                return i
            }
        }

        val currentList = _displayDanmakus.value
        for (i in scrollTracks.indices) {
            val occupyingId = scrollTracks[i].occupyingDanmakuId
            if (occupyingId != null) {
                val occupyingDanmaku = currentList.find { it.id == occupyingId }
                if (occupyingDanmaku == null || occupyingDanmaku.x < displayWidth * 0.3f) {
                    return i
                }
            }
        }

        return scrollTracks.indices.randomOrNull() ?: -1
    }

    private fun findAvailableTopTrack(): Int {
        val now = System.currentTimeMillis()
        for (i in topTracks.indices) {
            if (now - topTracks[i].lastUsedTime > fixedDanmakuDuration) {
                return i
            }
        }
        return -1
    }

    private fun findAvailableBottomTrack(): Int {
        val now = System.currentTimeMillis()
        for (i in bottomTracks.indices) {
            if (now - bottomTracks[i].lastUsedTime > fixedDanmakuDuration) {
                return i
            }
        }
        return -1
    }

    fun startAnimation() {
        if (animationJob != null) return
        animationJob = scope.launch {
            while (true) {
                delay(16)
                updateDanmakuPositions()
            }
        }
    }

    private fun updateDanmakuPositions() {
        if (!_enabled.value) return

        val now = System.currentTimeMillis()
        val currentList = _displayDanmakus.value

        val updatedList = currentList.mapNotNull { danmaku ->
            when (danmaku.type) {
                DanmakuType.SCROLL -> {
                    val scrollSpeed = (displayWidth / scrollDanmakuDuration) * 1000f * _speed.value
                    val newX = danmaku.x - scrollSpeed * 0.016f
                    if (newX < -200f) {
                        scrollTracks.forEach {
                            if (it.occupyingDanmakuId == danmaku.id) {
                                it.occupyingDanmakuId = null
                            }
                        }
                        null
                    } else {
                        danmaku.copy(x = newX)
                    }
                }
                DanmakuType.TOP, DanmakuType.BOTTOM -> {
                    if (now - danmaku.appearTime > fixedDanmakuDuration) {
                        if (danmaku.type == DanmakuType.TOP) {
                            topTracks.forEach {
                                if (it.occupyingDanmakuId == danmaku.id) {
                                    it.occupyingDanmakuId = null
                                }
                            }
                        } else {
                            bottomTracks.forEach {
                                if (it.occupyingDanmakuId == danmaku.id) {
                                    it.occupyingDanmakuId = null
                                }
                            }
                        }
                        null
                    } else {
                        danmaku
                    }
                }
            }
        }

        if (updatedList.size != currentList.size) {
            _displayDanmakus.value = updatedList
        } else {
            val positionsChanged = updatedList.zip(currentList).any { (new, old) ->
                new.x != old.x || new.y != old.y
            }
            if (positionsChanged) {
                _displayDanmakus.value = updatedList
            }
        }
    }

    fun stopAnimation() {
        animationJob?.cancel()
        animationJob = null
    }

    fun toggleEnabled() {
        _enabled.value = !_enabled.value
        if (!_enabled.value) {
            _displayDanmakus.value = emptyList()
        }
    }

    fun setEnabled(enabled: Boolean) {
        _enabled.value = enabled
        if (!enabled) {
            _displayDanmakus.value = emptyList()
        }
    }

    fun setOpacity(opacity: Float) {
        _opacity.value = opacity.coerceIn(0.1f, 1f)
    }

    fun setSpeed(speed: Float) {
        _speed.value = speed.coerceIn(0.5f, 3f)
    }

    fun setFontScale(scale: Float) {
        _fontScale.value = scale.coerceIn(0.5f, 2f)
    }

    fun setDisplayAreaRatio(ratio: Float) {
        _displayAreaRatio.value = ratio.coerceIn(0.2f, 1f)
        initTracks()
    }

    fun clearDisplay() {
        _displayDanmakus.value = emptyList()
        scrollTracks.forEach { it.occupyingDanmakuId = null }
        topTracks.forEach { it.occupyingDanmakuId = null }
        bottomTracks.forEach { it.occupyingDanmakuId = null }
    }

    fun reset() {
        clearDisplay()
        currentTime = 0f
        initTracks()
    }

    fun release() {
        stopAnimation()
        danmakuList.clear()
        clearDisplay()
    }
}