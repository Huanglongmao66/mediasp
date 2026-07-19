package com.mpvp.utils

import com.mpvp.model.MediaPlaylist
import com.mpvp.model.MediaPlaylistItem
import com.mpvp.model.MediaType
import com.mpvp.model.Playlist
import com.mpvp.model.VideoItem
import com.mpvp.model.VideoSourceType
import kotlinx.datetime.Clock

/**
 * 播放列表格式转换器
 *
 * 支持 M3U / M3U8 / XSPF 格式的导入与导出
 */
object PlaylistFormatConverter {

    // ======================== M3U / M3U8 ========================

    /**
     * 将视频列表导出为 M3U 格式
     */
    fun exportToM3u(videos: List<VideoItem>, playlistName: String = "Playlist"): String {
        val sb = StringBuilder()
        sb.append("#EXTM3U\n")
        sb.append("#PLAYLIST:$playlistName\n")
        videos.forEach { video ->
            val duration = if (video.duration > 0) video.duration / 1000 else -1
            sb.append("#EXTINF:$duration,${video.title}\n")
            sb.append("${video.videoUrl}\n")
        }
        return sb.toString()
    }

    /**
     * 将通用播放列表导出为 M3U 格式
     */
    fun exportToM3u(playlist: MediaPlaylist): String {
        val sb = StringBuilder()
        sb.append("#EXTM3U\n")
        sb.append("#PLAYLIST:${playlist.name}\n")
        playlist.items.forEach { item ->
            val duration = -1
            sb.append("#EXTINF:$duration,${item.title}\n")
            sb.append("${item.sourceUrl}\n")
        }
        return sb.toString()
    }

    /**
     * 从 M3U / M3U8 内容解析为视频项列表
     */
    fun importFromM3u(content: String): List<VideoItem> {
        val videos = mutableListOf<VideoItem>()
        val lines = content.lines()
        var currentTitle = ""
        var currentDuration = 0L

        for (line in lines) {
            val trimmed = line.trim()
            if (trimmed.isEmpty()) continue

            if (trimmed.startsWith("#EXTINF:")) {
                // 解析 #EXTINF:duration,title
                val commaIndex = trimmed.indexOf(",")
                if (commaIndex > 0) {
                    val durationStr = trimmed.substring(8, commaIndex).trim()
                    currentDuration = try {
                        durationStr.toLong() * 1000
                    } catch (e: NumberFormatException) {
                        0L
                    }
                    currentTitle = trimmed.substring(commaIndex + 1).trim()
                }
            } else if (trimmed.startsWith("#")) {
                // 跳过其他指令行
                continue
            } else {
                // URL 行
                val url = trimmed
                if (url.isNotEmpty()) {
                    val video = VideoItem(
                        id = "m3u_${videos.size}_${Clock.System.now().toEpochMilliseconds()}",
                        title = currentTitle.ifEmpty { extractFileName(url) },
                        videoUrl = url,
                        duration = currentDuration,
                        sourceType = if (url.startsWith("http")) VideoSourceType.NETWORK else VideoSourceType.LOCAL
                    )
                    videos.add(video)
                }
                currentTitle = ""
                currentDuration = 0L
            }
        }
        return videos
    }

    /**
     * 从 M3U 内容创建通用播放列表
     */
    fun importToMediaPlaylist(content: String, name: String = "导入的播放列表"): MediaPlaylist {
        val now = Clock.System.now().toEpochMilliseconds()
        val items = importFromM3u(content).mapIndexed { index, video ->
            MediaPlaylistItem(
                id = "item_${now}_$index",
                mediaId = video.id,
                mediaType = MediaType.VIDEO,
                title = video.title,
                coverUrl = video.coverUrl,
                sourceUrl = video.videoUrl,
                addedAt = now,
                sortOrder = index
            )
        }
        return MediaPlaylist(
            id = "playlist_imported_${now}",
            name = name,
            description = "从 M3U 文件导入",
            items = items,
            createdAt = now,
            updatedAt = now
        )
    }

    // ======================== XSPF ========================

    /**
     * 将视频列表导出为 XSPF 格式
     */
    fun exportToXspf(videos: List<VideoItem>, playlistName: String = "Playlist"): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">\n")
        sb.append("  <title>${escapeXml(playlistName)}</title>\n")
        sb.append("  <trackList>\n")
        videos.forEach { video ->
            sb.append("    <track>\n")
            sb.append("      <title>${escapeXml(video.title)}</title>\n")
            video.coverUrl?.let {
                sb.append("      <image>${escapeXml(it)}</image>\n")
            }
            sb.append("      <location>${escapeXml(video.videoUrl)}</location>\n")
            if (video.duration > 0) {
                sb.append("      <duration>${video.duration}</duration>\n")
            }
            sb.append("    </track>\n")
        }
        sb.append("  </trackList>\n")
        sb.append("</playlist>\n")
        return sb.toString()
    }

    /**
     * 将通用播放列表导出为 XSPF 格式
     */
    fun exportToXspf(playlist: MediaPlaylist): String {
        val sb = StringBuilder()
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n")
        sb.append("<playlist version=\"1\" xmlns=\"http://xspf.org/ns/0/\">\n")
        sb.append("  <title>${escapeXml(playlist.name)}</title>\n")
        if (playlist.description.isNotEmpty()) {
            sb.append("  <annotation>${escapeXml(playlist.description)}</annotation>\n")
        }
        sb.append("  <trackList>\n")
        playlist.items.forEach { item ->
            sb.append("    <track>\n")
            sb.append("      <title>${escapeXml(item.title)}</title>\n")
            item.coverUrl?.let {
                sb.append("      <image>${escapeXml(it)}</image>\n")
            }
            sb.append("      <location>${escapeXml(item.sourceUrl)}</location>\n")
            sb.append("    </track>\n")
        }
        sb.append("  </trackList>\n")
        sb.append("</playlist>\n")
        return sb.toString()
    }

    /**
     * 从 XSPF 内容解析为视频项列表
     */
    fun importFromXspf(content: String): List<VideoItem> {
        val videos = mutableListOf<VideoItem>()
        val trackRegex = Regex("<track>(.*?)</track>", RegexOption.DOT_MATCHES_ALL)
        val titleRegex = Regex("<title>(.*?)</title>", RegexOption.DOT_MATCHES_ALL)
        val locationRegex = Regex("<location>(.*?)</location>", RegexOption.DOT_MATCHES_ALL)
        val imageRegex = Regex("<image>(.*?)</image>", RegexOption.DOT_MATCHES_ALL)
        val durationRegex = Regex("<duration>(.*?)</duration>")

        trackRegex.findAll(content).forEachIndexed { index, match ->
            val trackContent = match.groupValues[1]
            val title = titleRegex.find(trackContent)?.groupValues?.get(1)?.trim()?.let { unescapeXml(it) } ?: ""
            val location = locationRegex.find(trackContent)?.groupValues?.get(1)?.trim()?.let { unescapeXml(it) } ?: ""
            val image = imageRegex.find(trackContent)?.groupValues?.get(1)?.trim()?.let { unescapeXml(it) }
            val duration = durationRegex.find(trackContent)?.groupValues?.get(1)?.trim()?.toLongOrNull() ?: 0L

            if (location.isNotEmpty()) {
                videos.add(VideoItem(
                    id = "xspf_${index}_${Clock.System.now().toEpochMilliseconds()}",
                    title = title.ifEmpty { extractFileName(location) },
                    videoUrl = location,
                    coverUrl = image,
                    duration = duration,
                    sourceType = if (location.startsWith("http")) VideoSourceType.NETWORK else VideoSourceType.LOCAL
                ))
            }
        }
        return videos
    }

    /**
     * 从 XSPF 内容创建通用播放列表
     */
    fun importToMediaPlaylistFromXspf(content: String, name: String = "导入的播放列表"): MediaPlaylist {
        val now = Clock.System.now().toEpochMilliseconds()
        val items = importFromXspf(content).mapIndexed { index, video ->
            MediaPlaylistItem(
                id = "item_${now}_$index",
                mediaId = video.id,
                mediaType = MediaType.VIDEO,
                title = video.title,
                coverUrl = video.coverUrl,
                sourceUrl = video.videoUrl,
                addedAt = now,
                sortOrder = index
            )
        }
        return MediaPlaylist(
            id = "playlist_imported_xspf_${now}",
            name = name,
            description = "从 XSPF 文件导入",
            items = items,
            createdAt = now,
            updatedAt = now
        )
    }

    // ======================== 自动检测格式 ========================

    /**
     * 自动检测格式并导入
     */
    fun importAuto(content: String, name: String = "导入的播放列表"): MediaPlaylist {
        val trimmed = content.trim()
        return when {
            trimmed.startsWith("#EXTM3U") -> importToMediaPlaylist(content, name)
            trimmed.startsWith("<?xml") || trimmed.startsWith("<playlist") -> importToMediaPlaylistFromXspf(content, name)
            else -> importToMediaPlaylist(content, name) // 默认尝试 M3U
        }
    }

    /**
     * 导出为 JSON 格式（应用内部格式）
     */
    fun exportToJson(playlist: MediaPlaylist): String {
        val sb = StringBuilder()
        sb.append("{\n")
        sb.append("  \"name\": \"${escapeJson(playlist.name)}\",\n")
        sb.append("  \"description\": \"${escapeJson(playlist.description)}\",\n")
        sb.append("  \"items\": [\n")
        playlist.items.forEachIndexed { index, item ->
            sb.append("    {\n")
            sb.append("      \"title\": \"${escapeJson(item.title)}\",\n")
            sb.append("      \"sourceUrl\": \"${escapeJson(item.sourceUrl)}\",\n")
            item.coverUrl?.let { sb.append("      \"coverUrl\": \"${escapeJson(it)}\",\n") }
            sb.append("      \"mediaType\": \"${item.mediaType.name}\"\n")
            sb.append("    }")
            if (index < playlist.items.size - 1) sb.append(",")
            sb.append("\n")
        }
        sb.append("  ]\n")
        sb.append("}\n")
        return sb.toString()
    }

    // ======================== 辅助方法 ========================

    /** 导出格式枚举 */
    enum class ExportFormat(val displayName: String, val fileExtension: String) {
        M3U("M3U 播放列表", ".m3u"),
        XSPF("XSPF 播放列表", ".xspf"),
        JSON("JSON 格式", ".json")
    }

    /** 导入格式枚举 */
    enum class ImportFormat(val displayName: String, val fileExtension: String) {
        AUTO("自动检测", ""),
        M3U("M3U/M3U8", ".m3u"),
        XSPF("XSPF", ".xspf"),
        JSON("JSON", ".json")
    }

    private fun extractFileName(url: String): String {
        val withoutQuery = url.substringBefore("?")
        val fileName = withoutQuery.substringAfterLast("/")
        return fileName.ifEmpty { "未知媒体" }
    }

    private fun escapeXml(text: String): String {
        return text
            .replace("&", "&amp;")
            .replace("<", "&lt;")
            .replace(">", "&gt;")
            .replace("\"", "&quot;")
            .replace("'", "&apos;")
    }

    private fun unescapeXml(text: String): String {
        return text
            .replace("&amp;", "&")
            .replace("&lt;", "<")
            .replace("&gt;", ">")
            .replace("&quot;", "\"")
            .replace("&apos;", "'")
    }

    private fun escapeJson(text: String): String {
        return text
            .replace("\\", "\\\\")
            .replace("\"", "\\\"")
            .replace("\n", "\\n")
            .replace("\r", "\\r")
            .replace("\t", "\\t")
    }
}
