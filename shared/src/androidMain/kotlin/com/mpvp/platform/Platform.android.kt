package com.mpvp.platform

import android.content.Context
import android.os.Build
import android.provider.MediaStore

/**
 * Android平台实现
 */
class AndroidPlatform : Platform {
    override val name: String = "Android ${Build.VERSION.RELEASE}"
    override val isAndroid: Boolean = true
    override val isDesktop: Boolean = false
    override val isIOS: Boolean = false
    override val isWeb: Boolean = false
}

/**
 * 获取平台实例 - Android实现
 */
actual fun getPlatform(): Platform = AndroidPlatform()

/**
 * Android文件扫描器
 *
 * 使用MediaStore扫描设备中的视频文件
 */
class AndroidFileScanner(private val context: Context) : PlatformFileScanner {

    override suspend fun scanVideoFiles(directory: String?): List<String> {
        val videoPaths = mutableListOf<String>()
        val projection = arrayOf(
            MediaStore.Video.Media.DATA,
            MediaStore.Video.Media.DISPLAY_NAME,
            MediaStore.Video.Media.SIZE,
            MediaStore.Video.Media.DURATION,
            MediaStore.Video.Media.DATE_MODIFIED
        )

        val cursor = context.contentResolver.query(
            MediaStore.Video.Media.EXTERNAL_CONTENT_URI,
            projection,
            null,
            null,
            "${MediaStore.Video.Media.DATE_MODIFIED} DESC"
        )

        cursor?.use {
            val pathColumn = it.getColumnIndexOrThrow(MediaStore.Video.Media.DATA)
            while (it.moveToNext()) {
                val path = it.getString(pathColumn)
                if (path != null) {
                    videoPaths.add(path)
                }
            }
        }

        return videoPaths
    }

    override suspend fun getFileInfo(filePath: String): FileInfo? {
        return try {
            val file = java.io.File(filePath)
            FileInfo(
                path = file.absolutePath,
                name = file.name,
                size = file.length(),
                lastModified = file.lastModified(),
                isDirectory = file.isDirectory
            )
        } catch (e: Exception) {
            null
        }
    }

    override suspend fun generateThumbnail(videoPath: String, timeMs: Long): String? {
        // 使用MediaMetadataRetriever生成缩略图
        // 实际实现需要更复杂的处理
        return null
    }

    override suspend fun getVideoDuration(videoPath: String): Long {
        return try {
            val retriever = android.media.MediaMetadataRetriever()
            retriever.setDataSource(videoPath)
            val duration = retriever.extractMetadata(
                android.media.MediaMetadataRetriever.METADATA_KEY_DURATION
            )?.toLongOrNull() ?: 0L
            retriever.release()
            duration
        } catch (e: Exception) {
            0L
        }
    }
}
