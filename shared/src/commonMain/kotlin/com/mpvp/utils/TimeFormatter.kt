package com.mpvp.utils

import kotlinx.datetime.Clock
import kotlinx.datetime.Instant
import kotlinx.datetime.TimeZone
import kotlinx.datetime.toLocalDateTime

/**
 * 时间格式化工具类
 *
 * 提供各种时间格式化方法，用于播放器时间显示、历史记录时间等
 */
object TimeFormatter {

    /** 秒的毫秒数 */
    private const val SECOND_MS = 1000L

    /** 分钟的毫秒数 */
    private const val MINUTE_MS = 60 * SECOND_MS

    /** 小时的毫秒数 */
    private const val HOUR_MS = 60 * MINUTE_MS

    /** 天的毫秒数 */
    private const val DAY_MS = 24 * HOUR_MS

    /**
     * 格式化时长（毫秒）
     *
     * @param durationMs 时长（毫秒）
     * @return 格式化后的字符串，如 "01:23" 或 "1:02:03"
     */
    fun formatDuration(durationMs: Long): String {
        if (durationMs <= 0) return "00:00"

        val totalSeconds = durationMs / SECOND_MS
        val hours = totalSeconds / 3600
        val minutes = (totalSeconds % 3600) / 60
        val seconds = totalSeconds % 60

        return if (hours > 0) {
            "${hours}:${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        } else {
            "${minutes.toString().padStart(2, '0')}:${seconds.toString().padStart(2, '0')}"
        }
    }

    /**
     * 格式化时长（秒）
     *
     * @param durationSeconds 时长（秒）
     * @return 格式化后的字符串
     */
    fun formatDurationSeconds(durationSeconds: Long): String {
        return formatDuration(durationSeconds * SECOND_MS)
    }

    /**
     * 格式化时间戳为相对时间
     *
     * @param timestamp 时间戳（毫秒）
     * @return 相对时间字符串，如 "刚刚"、"5分钟前"、"2小时前"、"昨天"、"3天前"
     */
    fun formatTimeAgo(timestamp: Long): String {
        val now = Clock.System.now().toEpochMilliseconds()
        val diff = now - timestamp

        return when {
            diff < MINUTE_MS -> "刚刚"
            diff < HOUR_MS -> "${diff / MINUTE_MS}分钟前"
            diff < DAY_MS -> "${diff / HOUR_MS}小时前"
            diff < 2 * DAY_MS -> "昨天"
            diff < 7 * DAY_MS -> "${diff / DAY_MS}天前"
            diff < 30 * DAY_MS -> "${diff / (7 * DAY_MS)}周前"
            diff < 365 * DAY_MS -> "${diff / (30 * DAY_MS)}个月前"
            else -> "${diff / (365 * DAY_MS)}年前"
        }
    }

    /**
     * 格式化时间戳为日期字符串
     *
     * @param timestamp 时间戳（毫秒）
     * @param pattern 日期格式（默认 "yyyy-MM-dd HH:mm"）
     * @return 格式化后的日期字符串
     */
    fun formatDate(timestamp: Long, pattern: String = "yyyy-MM-dd HH:mm"): String {
        val date = Instant.fromEpochMilliseconds(timestamp)
        val localDateTime = date.toLocalDateTime(TimeZone.currentSystemDefault())

        return buildString {
            if (pattern.contains("yyyy")) append(localDateTime.year)
            if (pattern.contains("-")) append("-")
            if (pattern.contains("MM")) append(localDateTime.monthNumber.toString().padStart(2, '0'))
            if (pattern.contains("-")) append("-")
            if (pattern.contains("dd")) append(localDateTime.dayOfMonth.toString().padStart(2, '0'))
            if (pattern.contains(" ")) append(" ")
            if (pattern.contains("HH")) append(localDateTime.hour.toString().padStart(2, '0'))
            if (pattern.contains(":")) append(":")
            if (pattern.contains("mm")) append(localDateTime.minute.toString().padStart(2, '0'))
        }
    }

    /**
     * 格式化文件大小
     *
     * @param bytes 文件大小（字节）
     * @return 格式化后的字符串，如 "1.5 MB"、"800 KB"
     */
    fun formatFileSize(bytes: Long): String {
        if (bytes <= 0) return "0 B"

        val kb = 1024.0
        val mb = kb * 1024
        val gb = mb * 1024
        val tb = gb * 1024

        return when {
            bytes < kb -> "${bytes} B"
            bytes < mb -> formatDouble(bytes / kb, 1) + " KB"
            bytes < gb -> formatDouble(bytes / mb, 1) + " MB"
            bytes < tb -> formatDouble(bytes / gb, 2) + " GB"
            else -> formatDouble(bytes / tb, 2) + " TB"
        }
    }

    private fun formatDouble(value: Double, decimals: Int): String {
        var factor = 1.0
        repeat(decimals) { factor *= 10.0 }
        val scaled = (value * factor).toLong().toDouble() / factor
        return if (scaled == scaled.toLong().toDouble()) {
            scaled.toLong().toString()
        } else {
            scaled.toString()
        }
    }

    /**
     * 将时间字符串转换为毫秒
     *
     * @param timeStr 时间字符串，如 "01:23" 或 "1:02:03"
     * @return 毫秒数
     */
    fun parseTimeToMillis(timeStr: String): Long {
        val parts = timeStr.split(":")
        return when (parts.size) {
            2 -> {
                val minutes = parts[0].toLongOrNull() ?: 0L
                val seconds = parts[1].toLongOrNull() ?: 0L
                (minutes * 60 + seconds) * SECOND_MS
            }
            3 -> {
                val hours = parts[0].toLongOrNull() ?: 0L
                val minutes = parts[1].toLongOrNull() ?: 0L
                val seconds = parts[2].toLongOrNull() ?: 0L
                (hours * 3600 + minutes * 60 + seconds) * SECOND_MS
            }
            else -> 0L
        }
    }

    /**
     * 格式化播放速度
     *
     * @param speed 播放速度
     * @return 格式化后的字符串，如 "1.0x"、"1.5x"
     */
    fun formatSpeed(speed: Float): String {
        return if (speed == speed.toInt().toFloat()) {
            "${speed.toInt()}.0x"
        } else {
            "${speed}x"
        }
    }
}
