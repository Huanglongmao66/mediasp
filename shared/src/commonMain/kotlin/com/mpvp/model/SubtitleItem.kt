package com.mpvp.model

import kotlinx.serialization.Serializable

/**
 * 字幕数据类
 *
 * @property index 字幕索引
 * @property startTime 开始时间（毫秒）
 * @property endTime 结束时间（毫秒）
 * @property text 字幕文本
 * @property style 字幕样式
 */
@Serializable
data class SubtitleItem(
    val index: Int,
    val startTime: Long,
    val endTime: Long,
    val text: String,
    val style: SubtitleStyle = SubtitleStyle()
)

/**
 * 字幕样式
 *
 * @property fontSize 字体大小
 * @property color 字体颜色
 * @property backgroundColor 背景颜色
 * @property isBold 是否加粗
 * @property isItalic 是否斜体
 */
@Serializable
data class SubtitleStyle(
    val fontSize: Int = 18,
    val color: Long = 0xFFFFFF,
    val backgroundColor: Long = 0x80000000,
    val isBold: Boolean = true,
    val isItalic: Boolean = false
)

/**
 * 字幕轨道
 *
 * @property language 语言标识
 * @property languageName 语言名称
 * @property subtitles 字幕列表
 */
data class SubtitleTrack(
    val language: String,
    val languageName: String,
    val subtitles: List<SubtitleItem>
)