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

/**
 * 弹幕管理器
 *
 * 管理弹幕的加载、发送、显示逻辑
 */
class DanmakuManager {

    /** 弹幕列表 */
    private val danmakuList = mutableListOf<DanmakuItem>()

    /** 当前显示的弹幕 */
    private val _displayDanmakus = MutableStateFlow<List<DanmakuDisplayState>>(emptyList())
    val displayDanmakus: StateFlow<List<DanmakuDisplayState>> = _displayDanmakus.asStateFlow()

    /** 弹幕是否开启 */
    private val _enabled = MutableStateFlow(true)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    /** 弹幕透明度 */
    private val _opacity = MutableStateFlow(0.8f)
    val opacity: StateFlow<Float> = _opacity.asStateFlow()

    /** 弹幕速度 */
    private val _speed = MutableStateFlow(1.0f)
    val speed: StateFlow<Float> = _speed.asStateFlow()

    /** 弹幕字号比例 */
    private val _fontScale = MutableStateFlow(1.0f)
    val fontScale: StateFlow<Float> = _fontScale.asStateFlow()

    /** 显示区域高度占比 */
    private val _displayAreaRatio = MutableStateFlow(0.4f)
    val displayAreaRatio: StateFlow<Float> = _displayAreaRatio.asStateFlow()

    /** 显示区域宽度 */
    private var displayWidth: Float = 0f

    /** 显示区域高度 */
    private var displayHeight: Float = 0f

    /** 滚动弹幕轨道 */
    private val scrollTracks = mutableListOf<Long>()

    /** 顶部弹幕轨道 */
    private val topTracks = mutableListOf<Long>()

    /** 底部弹幕轨道 */
    private val bottomTracks = mutableListOf<Long>()

    /** 协程作用域 */
    private val scope = CoroutineScope(Dispatchers.Main)

    /** 动画更新作业 */
    private var animationJob: Job? = null

    /** 当前播放时间 */
    private var currentTime: Float = 0f

    /**
     * 设置显示区域大小
     */
    fun setDisplaySize(width: Float, height: Float) {
        displayWidth = width
        displayHeight = height
        initTracks()
    }

    /**
     * 初始化弹幕轨道
     */
    private fun initTracks() {
        val trackHeight = 36f
        val maxTracks = ((displayHeight * _displayAreaRatio.value) / trackHeight).toInt().coerceAtLeast(1)
        scrollTracks.clear()
        topTracks.clear()
        bottomTracks.clear()
        repeat(maxTracks) {
            scrollTracks.add(0L)
            topTracks.add(0L)
            bottomTracks.add(0L)
        }
    }

    /**
     * 加载弹幕列表
     */
    fun loadDanmakus(items: List<DanmakuItem>) {
        danmakuList.clear()
        danmakuList.addAll(items.sortedBy { it.time })
    }

    /**
     * 添加弹幕
     */
    fun addDanmaku(item: DanmakuItem) {
        danmakuList.add(item)
        danmakuList.sortBy { it.time }
    }

    /**
     * 发送弹幕（立即显示）
     */
    fun sendDanmaku(content: String, color: Long = 0xFFFFFF, type: DanmakuType = DanmakuType.SCROLL) {
        val item = DanmakuItem(
            id = "local_${System.currentTimeMillis()}",
            content = content,
            time = currentTime,
            type = type,
            color = color
        )
        addDanmaku(item)
    }

    /**
     * 更新当前播放时间
     */
    fun updateTime(time: Float) {
        currentTime = time
        if (_enabled.value) {
            addNewDanmakus(time)
        }
    }

    /**
     * 添加当前时间应该出现的弹幕
     */
    private fun addNewDanmakus(time: Float) {
        val newItems = danmakuList.filter {
            it.time <= time && it.time > time - 0.5f
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

    /**
     * 创建弹幕显示状态
     */
    private fun createDisplayState(item: DanmakuItem): DanmakuDisplayState? {
        val fontSize = (item.fontSize * _fontScale.value).toInt()

        return when (item.type) {
            DanmakuType.SCROLL -> {
                val trackIndex = findAvailableScrollTrack()
                if (trackIndex < 0) return null
                val y = trackIndex * 36f + 10f
                scrollTracks[trackIndex] = System.currentTimeMillis()
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
                val y = trackIndex * 36f + 10f
                topTracks[trackIndex] = System.currentTimeMillis()
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
                val y = displayHeight - (trackIndex + 1) * 36f - 10f
                bottomTracks[trackIndex] = System.currentTimeMillis()
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

    /**
     * 查找可用的滚动弹幕轨道
     */
    private fun findAvailableScrollTrack(): Int {
        for (i in scrollTracks.indices) {
            if (System.currentTimeMillis() - scrollTracks[i] > 2000) {
                return i
            }
        }
        return scrollTracks.indices.randomOrNull() ?: -1
    }

    /**
     * 查找可用的顶部弹幕轨道
     */
    private fun findAvailableTopTrack(): Int {
        for (i in topTracks.indices) {
            if (System.currentTimeMillis() - topTracks[i] > 3000) {
                return i
            }
        }
        return -1
    }

    /**
     * 查找可用的底部弹幕轨道
     */
    private fun findAvailableBottomTrack(): Int {
        for (i in bottomTracks.indices) {
            if (System.currentTimeMillis() - bottomTracks[i] > 3000) {
                return i
            }
        }
        return -1
    }

    /**
     * 开始弹幕动画
     */
    fun startAnimation() {
        if (animationJob != null) return
        animationJob = scope.launch {
            while (true) {
                delay(16) // 约60fps
                updateDanmakuPositions()
            }
        }
    }

    /**
     * 更新弹幕位置
     */
    private fun updateDanmakuPositions() {
        if (!_enabled.value) return

        val currentList = _displayDanmakus.value
        val updatedList = currentList.mapNotNull { danmaku ->
            when (danmaku.type) {
                DanmakuType.SCROLL -> {
                    val newX = danmaku.x - 2f * _speed.value
                    if (newX < -500f) null else danmaku.copy(x = newX)
                }
                DanmakuType.TOP, DanmakuType.BOTTOM -> {
                    // 固定位置弹幕持续3秒后消失
                    // 这里简化处理，实际应该记录出现时间
                    danmaku
                }
            }
        }
        _displayDanmakus.value = updatedList
    }

    /**
     * 停止弹幕动画
     */
    fun stopAnimation() {
        animationJob?.cancel()
        animationJob = null
    }

    /**
     * 切换弹幕开关
     */
    fun toggleEnabled() {
        _enabled.value = !_enabled.value
        if (!_enabled.value) {
            _displayDanmakus.value = emptyList()
        }
    }

    /**
     * 设置弹幕开关
     */
    fun setEnabled(enabled: Boolean) {
        _enabled.value = enabled
        if (!enabled) {
            _displayDanmakus.value = emptyList()
        }
    }

    /**
     * 设置弹幕透明度
     */
    fun setOpacity(opacity: Float) {
        _opacity.value = opacity.coerceIn(0.1f, 1f)
    }

    /**
     * 设置弹幕速度
     */
    fun setSpeed(speed: Float) {
        _speed.value = speed.coerceIn(0.5f, 3f)
    }

    /**
     * 设置字体大小比例
     */
    fun setFontScale(scale: Float) {
        _fontScale.value = scale.coerceIn(0.5f, 2f)
    }

    /**
     * 设置显示区域比例
     */
    fun setDisplayAreaRatio(ratio: Float) {
        _displayAreaRatio.value = ratio.coerceIn(0.2f, 1f)
        initTracks()
    }

    /**
     * 清空显示的弹幕
     */
    fun clearDisplay() {
        _displayDanmakus.value = emptyList()
    }

    /**
     * 重置弹幕
     */
    fun reset() {
        clearDisplay()
        currentTime = 0f
        initTracks()
    }

    /**
     * 释放资源
     */
    fun release() {
        stopAnimation()
        danmakuList.clear()
        clearDisplay()
    }
}
