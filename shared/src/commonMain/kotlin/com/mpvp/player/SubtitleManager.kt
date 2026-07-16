package com.mpvp.player

import com.mpvp.model.SubtitleItem
import com.mpvp.model.SubtitleStyle
import com.mpvp.model.SubtitleTrack
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 字幕管理器
 *
 * 管理字幕的加载、解析、显示
 */
class SubtitleManager {

    private val _currentSubtitle = MutableStateFlow<SubtitleItem?>(null)
    val currentSubtitle: StateFlow<SubtitleItem?> = _currentSubtitle.asStateFlow()

    private val _subtitleTracks = MutableStateFlow<List<SubtitleTrack>>(emptyList())
    val subtitleTracks: StateFlow<List<SubtitleTrack>> = _subtitleTracks.asStateFlow()

    private val _currentTrackIndex = MutableStateFlow(-1)
    val currentTrackIndex: StateFlow<Int> = _currentTrackIndex.asStateFlow()

    private val _enabled = MutableStateFlow(true)
    val enabled: StateFlow<Boolean> = _enabled.asStateFlow()

    private val _fontSize = MutableStateFlow(18)
    val fontSize: StateFlow<Int> = _fontSize.asStateFlow()

    private var currentTrack: SubtitleTrack? = null

    /**
     * 从SRT格式文本加载字幕
     */
    fun loadFromSrt(srtContent: String, language: String = "zh", languageName: String = "中文字幕") {
        val subtitles = SrtParser.parse(srtContent)
        val track = SubtitleTrack(language, languageName, subtitles)

        val tracks = _subtitleTracks.value.toMutableList()
        tracks.add(track)
        _subtitleTracks.value = tracks

        if (_currentTrackIndex.value == -1) {
            selectTrack(tracks.size - 1)
        }
    }

    /**
     * 选择字幕轨道
     */
    fun selectTrack(index: Int) {
        if (index < 0 || index >= _subtitleTracks.value.size) {
            _currentTrackIndex.value = -1
            currentTrack = null
            _currentSubtitle.value = null
            return
        }
        _currentTrackIndex.value = index
        currentTrack = _subtitleTracks.value[index]
    }

    /**
     * 更新当前播放时间
     */
    fun updateTime(positionMs: Long) {
        if (!_enabled.value || currentTrack == null) {
            _currentSubtitle.value = null
            return
        }

        val subtitle = currentTrack!!.subtitles.find {
            positionMs >= it.startTime && positionMs <= it.endTime
        }

        if (subtitle?.text != _currentSubtitle.value?.text) {
            _currentSubtitle.value = subtitle?.let {
                it.copy(style = it.style.copy(fontSize = _fontSize.value))
            }
        }
    }

    fun setEnabled(enabled: Boolean) {
        _enabled.value = enabled
        if (!enabled) {
            _currentSubtitle.value = null
        }
    }

    fun toggleEnabled() {
        setEnabled(!_enabled.value)
    }

    fun setFontSize(size: Int) {
        _fontSize.value = size.coerceIn(12, 36)
    }

    fun clearTracks() {
        _subtitleTracks.value = emptyList()
        _currentTrackIndex.value = -1
        currentTrack = null
        _currentSubtitle.value = null
    }
}

/**
 * SRT字幕解析器
 */
object SrtParser {

    /**
     * 解析SRT格式字幕
     */
    fun parse(content: String): List<SubtitleItem> {
        val subtitles = mutableListOf<SubtitleItem>()
        val blocks = content.trim().split(Regex("\\n\\s*\\n"))

        blocks.forEachIndexed { index, block ->
            val lines = block.trim().lines()
            if (lines.size < 3) return@forEachIndexed

            val timeLine = lines[1]
            val timeMatch = Regex("(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})\\s*-->\\s*(\\d{2}):(\\d{2}):(\\d{2}),(\\d{3})")
                .find(timeLine) ?: return@forEachIndexed

            val (h1, m1, s1, ms1, h2, m2, s2, ms2) = timeMatch.destructured
            val startTime = toMillis(h1, m1, s1, ms1)
            val endTime = toMillis(h2, m2, s2, ms2)

            val text = lines.drop(2).joinToString("\n")

            subtitles.add(SubtitleItem(
                index = index,
                startTime = startTime,
                endTime = endTime,
                text = text
            ))
        }

        return subtitles
    }

    private fun toMillis(h: String, m: String, s: String, ms: String): Long {
        return h.toLong() * 3600000 + m.toLong() * 60000 + s.toLong() * 1000 + ms.toLong()
    }
}