package com.mpvp.repository

import com.mpvp.model.DownloadStatus
import com.mpvp.model.DownloadTask
import com.mpvp.model.MediaType
import kotlinx.coroutines.flow.StateFlow

/**
 * 下载管理器接口
 *
 * 定义跨平台下载管理的通用接口，各平台提供具体实现。
 */
interface DownloadManager {
    /** 下载任务列表 */
    val tasks: StateFlow<List<DownloadTask>>

    /** 最大并发下载数 */
    var maxConcurrentDownloads: Int

    /**
     * 创建下载任务
     */
    suspend fun createTask(
        title: String,
        sourceUrl: String,
        mediaType: MediaType,
        mediaId: String,
        coverUrl: String? = null
    ): DownloadTask

    /**
     * 开始下载任务
     */
    fun startDownload(taskId: String)

    /**
     * 暂停下载任务
     */
    fun pauseDownload(taskId: String)

    /**
     * 继续下载任务
     */
    fun resumeDownload(taskId: String)

    /**
     * 取消并删除下载任务
     */
    fun cancelDownload(taskId: String)

    /**
     * 重试下载任务
     */
    fun retryDownload(taskId: String)

    /**
     * 获取所有任务
     */
    fun getAllTasks(): List<DownloadTask>

    /**
     * 根据ID获取任务
     */
    fun getTaskById(id: String): DownloadTask?

    /**
     * 添加任务到列表
     */
    fun addTask(task: DownloadTask)

    /**
     * 从列表移除任务
     */
    fun removeTask(taskId: String)

    /**
     * 更新任务
     */
    fun updateTask(taskId: String, updater: (DownloadTask) -> DownloadTask)

    /**
     * 加载已保存的任务列表
     */
    fun loadTasks(tasks: List<DownloadTask>)
}

/**
 * 下载管理器简单实现（用于不支持后台下载的平台）
 *
 * 仅维护任务列表状态，不执行真实下载。
 */
class SimpleDownloadManager : DownloadManager {
    private val _tasks = kotlinx.coroutines.flow.MutableStateFlow<List<DownloadTask>>(emptyList())
    override val tasks: StateFlow<List<DownloadTask>> = _tasks

    override var maxConcurrentDownloads: Int = 3

    override suspend fun createTask(
        title: String,
        sourceUrl: String,
        mediaType: MediaType,
        mediaId: String,
        coverUrl: String?
    ): DownloadTask {
        val task = DownloadTask(
            id = java.util.UUID.randomUUID().toString(),
            title = title,
            sourceUrl = sourceUrl,
            localPath = "",
            mediaType = mediaType,
            mediaId = mediaId,
            coverUrl = coverUrl
        )
        addTask(task)
        return task
    }

    override fun startDownload(taskId: String) {
        updateTask(taskId) { t -> t.copy(status = DownloadStatus.DOWNLOADING) }
    }

    override fun pauseDownload(taskId: String) {
        updateTask(taskId) { t -> t.copy(status = DownloadStatus.PAUSED) }
    }

    override fun resumeDownload(taskId: String) {
        updateTask(taskId) { t -> t.copy(status = DownloadStatus.DOWNLOADING) }
    }

    override fun cancelDownload(taskId: String) {
        removeTask(taskId)
    }

    override fun retryDownload(taskId: String) {
        updateTask(taskId) { t -> t.copy(status = DownloadStatus.WAITING, progress = 0, downloadedSize = 0) }
    }

    override fun getAllTasks(): List<DownloadTask> = _tasks.value

    override fun getTaskById(id: String): DownloadTask? = _tasks.value.find { it.id == id }

    override fun addTask(task: DownloadTask) {
        _tasks.value = _tasks.value + task
    }

    override fun removeTask(taskId: String) {
        _tasks.value = _tasks.value.filter { it.id != taskId }
    }

    override fun updateTask(taskId: String, updater: (DownloadTask) -> DownloadTask) {
        _tasks.value = _tasks.value.map { if (it.id == taskId) updater(it) else it }
    }

    override fun loadTasks(tasks: List<DownloadTask>) {
        _tasks.value = tasks.map { task ->
            if (task.status == DownloadStatus.DOWNLOADING) {
                task.copy(status = DownloadStatus.PAUSED)
            } else {
                task
            }
        }
    }
}