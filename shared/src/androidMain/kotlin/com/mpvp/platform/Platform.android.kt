package com.mpvp.platform

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.os.Build
import android.provider.MediaStore
import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

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

/**
 * Android文件选择器
 *
 * 使用系统文件选择器选择视频文件或目录
 */
class AndroidFilePicker(private val activity: ComponentActivity) : PlatformFilePicker {

    private var singleFileLauncher: ActivityResultLauncher<String>? = null
    private var multiFileLauncher: ActivityResultLauncher<String>? = null
    private var directoryLauncher: ActivityResultLauncher<Uri>? = null

    init {
        singleFileLauncher = activity.registerForActivityResult(
            ActivityResultContracts.GetContent()
        ) { uri: Uri? ->
            // 通过回调返回结果
        }
    }

    override suspend fun pickVideoFile(): String? {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activity.registerForActivityResult(
                ActivityResultContracts.GetContent()
            ) { uri: Uri? ->
                val path = uri?.let { getPathFromUri(it) }
                if (continuation.isActive) {
                    continuation.resume(path)
                }
            }
            launcher.launch("video/*")
        }
    }

    override suspend fun pickMultipleVideoFiles(): List<String> {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activity.registerForActivityResult(
                ActivityResultContracts.GetMultipleContents()
            ) { uris: List<Uri> ->
                val paths = uris.mapNotNull { getPathFromUri(it) }
                if (continuation.isActive) {
                    continuation.resume(paths)
                }
            }
            launcher.launch("video/*")
        }
    }

    override suspend fun pickDirectory(): String? {
        return suspendCancellableCoroutine { continuation ->
            val launcher = activity.registerForActivityResult(
                ActivityResultContracts.OpenDocumentTree()
            ) { uri: Uri? ->
                val path = uri?.let { getPathFromUri(it) }
                if (continuation.isActive) {
                    continuation.resume(path)
                }
            }
            launcher.launch(null)
        }
    }

    private fun getPathFromUri(uri: Uri): String? {
        return try {
            // 对于 content:// URI，优先尝试读取真实文件路径
            val cursor = activity.contentResolver.query(uri, null, null, null, null)
            cursor?.use {
                if (it.moveToFirst()) {
                    val columnIndex = it.getColumnIndex(android.provider.OpenableColumns.DISPLAY_NAME)
                    if (columnIndex >= 0) {
                        val name = it.getString(columnIndex)
                        // 复制文件到应用沙盒以获取稳定的本地路径
                        val cacheFile = java.io.File(activity.cacheDir, name)
                        activity.contentResolver.openInputStream(uri)?.use { input ->
                            java.io.FileOutputStream(cacheFile).use { output ->
                                input.copyTo(output)
                            }
                        }
                        return cacheFile.absolutePath
                    }
                }
            }
            uri.toString()
        } catch (e: Exception) {
            uri.toString()
        }
    }
}
