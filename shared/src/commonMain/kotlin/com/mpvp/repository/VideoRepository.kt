package com.mpvp.repository

import com.mpvp.model.PlayHistory
import com.mpvp.model.VideoItem
import com.mpvp.model.VideoSourceType
import com.mpvp.platform.PlatformFileScanner
import com.mpvp.utils.FileUtils
import com.mpvp.utils.NetworkUtils
import io.ktor.client.HttpClient
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.datetime.Clock

/**
 * 视频仓库
 *
 * 负责视频数据的获取、管理，包括：
 * - 本地视频扫描
 * - 网络视频加载
 * - 播放历史管理
 * - 收藏管理
 *
 * @property fileScanner 平台文件扫描器
 * @property httpClient HTTP客户端
 * @property dataStore 数据存储
 */
class VideoRepository(
    private val fileScanner: PlatformFileScanner,
    private val httpClient: HttpClient,
    private val dataStore: AppDataStore
) {

    /**
     * 扫描本地视频文件
     *
     * @param directory 指定目录，为空则扫描默认目录
     * @return 视频列表的Flow
     */
    fun scanLocalVideos(directory: String? = null): Flow<List<VideoItem>> = flow {
        try {
            val videoPaths = fileScanner.scanVideoFiles(directory)
            val videoItems = videoPaths.mapNotNull { path ->
                try {
                    val fileInfo = fileScanner.getFileInfo(path)
                    val duration = fileScanner.getVideoDuration(path)
                    VideoItem(
                        id = FileUtils.generateVideoId(path),
                        title = FileUtils.getFileName(path),
                        videoUrl = path,
                        localPath = path,
                        sourceType = VideoSourceType.LOCAL,
                        videoFormat = com.mpvp.model.VideoFormat.fromExtension(FileUtils.getFileExtension(path)),
                        fileSize = fileInfo?.size ?: 0L,
                        duration = duration
                    )
                } catch (e: Exception) {
                    null
                }
            }
            emit(videoItems)
        } catch (e: Exception) {
            emit(emptyList())
        }
    }

    /**
     * 添加网络视频
     *
     * @param url 视频URL
     * @param title 视频标题
     * @param coverUrl 封面URL
     * @return 创建的VideoItem
     */
    suspend fun addNetworkVideo(
        url: String,
        title: String,
        coverUrl: String? = null
    ): VideoItem {
        val videoItem = FileUtils.createNetworkVideoItem(url, title, coverUrl)
        dataStore.saveVideo(videoItem)
        return videoItem
    }

    /**
     * 获取所有保存的视频
     *
     * @return 视频列表的Flow
     */
    fun getAllVideos(): Flow<List<VideoItem>> {
        return dataStore.getAllVideos()
    }

    /**
     * 根据类型获取视频
     *
     * @param sourceType 视频来源类型
     * @return 视频列表的Flow
     */
    fun getVideosByType(sourceType: VideoSourceType): Flow<List<VideoItem>> = flow {
        dataStore.getAllVideos().collect { videos ->
            emit(videos.filter { it.sourceType == sourceType })
        }
    }

    /**
     * 删除视频
     *
     * @param videoId 视频ID
     */
    suspend fun deleteVideo(videoId: String) {
        dataStore.deleteVideo(videoId)
    }

    /**
     * 更新视频信息
     *
     * @param video 更新后的视频对象
     */
    suspend fun updateVideo(video: VideoItem) {
        dataStore.saveVideo(video)
    }

    /**
     * 保存播放进度
     *
     * @param videoId 视频ID
     * @param position 播放位置（毫秒）
     * @param duration 视频时长（毫秒）
     */
    suspend fun savePlayProgress(
        videoId: String,
        position: Long,
        duration: Long
    ) {
        val progress = if (duration > 0) position.toFloat() / duration.toFloat() else 0f
        dataStore.savePlayProgress(videoId, position, progress)
    }

    /**
     * 获取播放进度
     *
     * @param videoId 视频ID
     * @return 播放位置（毫秒）
     */
    suspend fun getPlayProgress(videoId: String): Long {
        return dataStore.getPlayProgress(videoId)
    }

    /**
     * 添加到收藏
     *
     * @param video 视频对象
     */
    suspend fun addToFavorite(video: VideoItem) {
        dataStore.addToFavorite(video)
    }

    /**
     * 取消收藏
     *
     * @param videoId 视频ID
     */
    suspend fun removeFromFavorite(videoId: String) {
        dataStore.removeFromFavorite(videoId)
    }

    /**
     * 获取收藏列表
     *
     * @return 收藏视频列表的Flow
     */
    fun getFavorites(): Flow<List<VideoItem>> {
        return dataStore.getFavorites()
    }

    /**
     * 检查是否已收藏
     *
     * @param videoId 视频ID
     * @return 是否已收藏
     */
    suspend fun isFavorite(videoId: String): Boolean {
        return dataStore.isFavorite(videoId)
    }

    /**
     * 记录播放历史
     *
     * @param video 视频对象
     * @param position 播放位置
     */
    suspend fun recordPlayHistory(video: VideoItem, position: Long) {
        val history = PlayHistory(
            id = "history_${video.id}_${Clock.System.now().toEpochMilliseconds()}",
            videoId = video.id,
            videoTitle = video.title,
            videoUrl = video.videoUrl,
            coverUrl = video.coverUrl,
            duration = video.duration,
            playPosition = position,
            playProgress = if (video.duration > 0) position.toFloat() / video.duration.toFloat() else 0f,
            playTime = Clock.System.now().toEpochMilliseconds(),
            sourceType = video.sourceType
        )
        dataStore.savePlayHistory(history)
    }

    /**
     * 获取播放历史
     *
     * @return 播放历史列表的Flow
     */
    fun getPlayHistory(): Flow<List<PlayHistory>> {
        return dataStore.getPlayHistory()
    }

    /**
     * 清空播放历史
     */
    suspend fun clearPlayHistory() {
        dataStore.clearPlayHistory()
    }

    /**
     * 删除单条播放历史
     *
     * @param historyId 历史记录ID
     */
    suspend fun deletePlayHistory(historyId: String) {
        dataStore.deletePlayHistory(historyId)
    }

    /**
     * 验证网络视频URL
     *
     * @param url 视频URL
     * @return 是否有效
     */
    suspend fun validateVideoUrl(url: String): Boolean {
        return try {
            NetworkUtils.isUrlAccessible(httpClient, url)
        } catch (e: Exception) {
            false
        }
    }
}
