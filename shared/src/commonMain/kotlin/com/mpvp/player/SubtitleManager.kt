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
     * 从VTT格式文本加载字幕
     */
    fun loadFromVtt(vttContent: String, language: String = "zh", languageName: String = "中文字幕") {
        val subtitles = VttParser.parse(vttContent)
        val track = SubtitleTrack(language, languageName, subtitles)

        val tracks = _subtitleTracks.value.toMutableList()
        tracks.add(track)
        _subtitleTracks.value = tracks

        if (_currentTrackIndex.value == -1) {
            selectTrack(tracks.size - 1)
        }
    }

    /**
     * 从ASS/SSA格式文本加载字幕
     */
    fun loadFromAss(assContent: String, language: String = "zh", languageName: String = "中文字幕") {
        val subtitles = AssParser.parse(assContent)
        val track = SubtitleTrack(language, languageName, subtitles)

        val tracks = _subtitleTracks.value.toMutableList()
        tracks.add(track)
        _subtitleTracks.value = tracks

        if (_currentTrackIndex.value == -1) {
            selectTrack(tracks.size - 1)
        }
    }

    /**
     * 自动检测格式并加载字幕
     */
    fun loadAuto(content: String, language: String = "zh", languageName: String = "中文字幕") {
        val trimmed = content.trim()
        when {
            trimmed.startsWith("WEBVTT") -> loadFromVtt(content, language, languageName)
            trimmed.startsWith("[Script Info]") || trimmed.contains("[Events]") -> loadFromAss(content, language, languageName)
            else -> loadFromSrt(content, language, languageName)
        }
    }

    /**
     * 获取当前字幕轨道
     */
    fun getCurrentTrack(): SubtitleTrack? = currentTrack

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

/**
 * VTT字幕解析器
 */
object VttParser {

    /**
     * 解析VTT格式字幕
     */
    fun parse(content: String): List<SubtitleItem> {
        val subtitles = mutableListOf<SubtitleItem>()
        val lines = content.lines()
        var i = 0

        // 跳过 WEBVTT 头部
        while (i < lines.size && lines[i].trim().isEmpty()) i++
        if (i < lines.size && lines[i].trim().startsWith("WEBVTT")) i++

        // 跳过 NOTE 和 STYLE 块
        while (i < lines.size) {
            val line = lines[i].trim()
            if (line.startsWith("NOTE") || line.startsWith("STYLE") || line.startsWith("REGION")) {
                while (i < lines.size && lines[i].trim().isNotEmpty()) i++
                while (i < lines.size && lines[i].trim().isEmpty()) i++
            } else {
                break
            }
        }

        var index = 0
        while (i < lines.size) {
            val line = lines[i].trim()

            // 跳过空行
            if (line.isEmpty()) {
                i++
                continue
            }

            // 跳过 cue 标识符（如 "1" 或 "cue-1"）
            var timeLine = line
            if (!line.contains("-->")) {
                i++
                if (i >= lines.size) break
                timeLine = lines[i].trim()
            }

            // 解析时间行
            val timeMatch = Regex(
                "(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})\\s*-->\\s*(\\d{2}):(\\d{2}):(\\d{2})\\.(\\d{3})"
            ).find(timeLine)

            if (timeMatch == null) {
                // 尝试 MM:SS.mmm 格式
                val shortTimeMatch = Regex(
                    "(\\d{2}):(\\d{2})\\.(\\d{3})\\s*-->\\s*(\\d{2}):(\\d{2})\\.(\\d{3})"
                ).find(timeLine)

                if (shortTimeMatch == null) {
                    i++
                    continue
                }

                val (m1, s1, ms1, m2, s2, ms2) = shortTimeMatch.destructured
                val startTime = m1.toLong() * 60000 + s1.toLong() * 1000 + ms1.toLong()
                val endTime = m2.toLong() * 60000 + s2.toLong() * 1000 + ms2.toLong()

                i++
                val textLines = mutableListOf<String>()
                while (i < lines.size && lines[i].trim().isNotEmpty()) {
                    textLines.add(lines[i].trim())
                    i++
                }

                if (textLines.isNotEmpty()) {
                    val text = cleanVttText(textLines.joinToString("\n"))
                    subtitles.add(SubtitleItem(
                        index = index++,
                        startTime = startTime,
                        endTime = endTime,
                        text = text
                    ))
                }
                continue
            }

            val (h1, m1, s1, ms1, h2, m2, s2, ms2) = timeMatch.destructured
            val startTime = h1.toLong() * 3600000 + m1.toLong() * 60000 + s1.toLong() * 1000 + ms1.toLong()
            val endTime = h2.toLong() * 3600000 + m2.toLong() * 60000 + s2.toLong() * 1000 + ms2.toLong()

            i++
            val textLines = mutableListOf<String>()
            while (i < lines.size && lines[i].trim().isNotEmpty()) {
                textLines.add(lines[i].trim())
                i++
            }

            if (textLines.isNotEmpty()) {
                val text = cleanVttText(textLines.joinToString("\n"))
                subtitles.add(SubtitleItem(
                    index = index++,
                    startTime = startTime,
                    endTime = endTime,
                    text = text
                ))
            }
        }

        return subtitles
    }

    /**
     * 清理VTT文本中的标签
     */
    private fun cleanVttText(text: String): String {
        return text
            .replace(Regex("<[^>]+>"), "") // 移除HTML标签
            .replace(Regex("\\{[^}]*\\}"), "") // 移除样式标签
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&nbsp;", " ")
            .trim()
    }
}

/**
 * ASS/SSA字幕解析器
 */
object AssParser {

    /**
     * 解析ASS/SSA格式字幕
     */
    fun parse(content: String): List<SubtitleItem> {
        val subtitles = mutableListOf<SubtitleItem>()
        val lines = content.lines()
        var inEvents = false
        var formatFields = listOf("Layer", "Start", "End", "Style", "Name", "MarginL", "MarginR", "MarginV", "Effect", "Text")
        var index = 0

        for (line in lines) {
            val trimmed = line.trim()

            if (trimmed.startsWith("[")) {
                inEvents = trimmed.equals("[Events]", ignoreCase = true)
                continue
            }

            if (!inEvents) continue

            if (trimmed.startsWith("Format:")) {
                formatFields = trimmed.substring(8).split(",").map { it.trim() }
                continue
            }

            if (trimmed.startsWith("Dialogue:")) {
                val data = trimmed.substring(9)
                val fields = parseAssFields(data, formatFields.size)

                val startIdx = formatFields.indexOf("Start")
                val endIdx = formatFields.indexOf("End")
                val textIdx = formatFields.indexOf("Text")

                if (startIdx >= 0 && endIdx >= 0 && textIdx >= 0 && fields.size > maxOf(startIdx, endIdx, textIdx)) {
                    val startTime = parseAssTime(fields[startIdx])
                    val endTime = parseAssTime(fields[endIdx])
                    val text = cleanAssText(fields[textIdx])

                    if (text.isNotEmpty()) {
                        subtitles.add(SubtitleItem(
                            index = index++,
                            startTime = startTime,
                            endTime = endTime,
                            text = text
                        ))
                    }
                }
            }
        }

        return subtitles
    }

    /**
     * 解析ASS字段（Text字段可能包含逗号）
     */
    private fun parseAssFields(data: String, expectedCount: Int): List<String> {
        val fields = data.split(",").toMutableList()
        // 如果字段数超过预期，合并多余的到最后一个字段（Text字段通常在最后）
        while (fields.size > expectedCount) {
            val last = fields.removeAt(fields.size - 1)
            fields[fields.size - 1] = fields[fields.size - 1] + "," + last
        }
        return fields.map { it.trim() }
    }

    /**
     * 解析ASS时间格式 H:MM:SS.CS
     */
    private fun parseAssTime(timeStr: String): Long {
        val match = Regex("(\\d+):(\\d{2}):(\\d{2})\\.(\\d{2})").find(timeStr.trim()) ?: return 0L
        val (h, m, s, cs) = match.destructured
        return h.toLong() * 3600000 + m.toLong() * 60000 + s.toLong() * 1000 + cs.toLong() * 10
    }

    /**
     * 清理ASS文本中的样式标签
     */
    private fun cleanAssText(text: String): String {
        return text
            .replace(Regex("\\{[^}]*\\}"), "") // 移除ASS样式标签 {...}
            .replace("\\N", "\n") // 换行符
            .replace("\\n", "\n")
            .replace("\\h", " ") // 硬空格
            .trim()
    }
}