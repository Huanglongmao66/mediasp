package com.mpvp.viewmodel

import com.mpvp.model.DownloadStatus
import com.mpvp.model.DownloadTask
import com.mpvp.model.MediaType
import com.mpvp.repository.AppDataStore
import com.mpvp.repository.DownloadManager
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 下载状态
 */
data class DownloadState(
    val tasks: List<DownloadTask> = emptyList(),
    val downloadingCount: Int = 0,
    val completedCount: Int = 0,
    val isLoading: Boolean = false
)

/**
 * 下载ViewModel
 *
 * 管理下载任务的创建、控制和状态展示。
 */
class DownloadViewModel(
    private val downloadManager: DownloadManager,
    private val dataStore: AppDataStore
) : BaseViewModel() {

    private val _state = MutableStateFlow(DownloadState(isLoading = true))
    val state: StateFlow<DownloadState> = _state.asStateFlow()

    init {
        loadTasks()
    }

    private fun loadTasks() {
        launch {
            // 从持久化存储加载任务
            val savedTasks = dataStore.getDownloadTasksList()
            downloadManager.loadTasks(savedTasks)

            // 监听任务变化
            downloadManager.tasks.collectLatest { tasks ->
                _state.value = DownloadState(
                    tasks = tasks,
                    downloadingCount = tasks.count { it.status == DownloadStatus.DOWNLOADING },
                    completedCount = tasks.count { it.status == DownloadStatus.COMPLETED },
                    isLoading = false
                )
                // 同步保存到持久化存储
                dataStore.saveDownloadTasksList(tasks)
            }
        }
    }

    /**
     * 创建下载任务
     */
    fun createTask(
        title: String,
        sourceUrl: String,
        mediaType: MediaType,
        mediaId: String,
        coverUrl: String? = null
    ) {
        launch {
            val task = downloadManager.createTask(title, sourceUrl, mediaType, mediaId, coverUrl)
            dataStore.addDownloadTask(task)
            // 自动开始下载
            downloadManager.startDownload(task.id)
        }
    }

    /**
     * 开始下载
     */
    fun startDownload(taskId: String) {
        downloadManager.startDownload(taskId)
    }

    /**
     * 暂停下载
     */
    fun pauseDownload(taskId: String) {
        downloadManager.pauseDownload(taskId)
        launch {
            downloadManager.getTaskById(taskId)?.let { task ->
                dataStore.updateDownloadTask(task)
            }
        }
    }

    /**
     * 继续下载
     */
    fun resumeDownload(taskId: String) {
        downloadManager.resumeDownload(taskId)
    }

    /**
     * 取消下载
     */
    fun cancelDownload(taskId: String) {
        downloadManager.cancelDownload(taskId)
        launch {
            dataStore.deleteDownloadTask(taskId)
        }
    }

    /**
     * 重试下载
     */
    fun retryDownload(taskId: String) {
        downloadManager.retryDownload(taskId)
    }

    /**
     * 删除已完成的任务
     */
    fun deleteCompletedTask(taskId: String) {
        downloadManager.removeTask(taskId)
        launch {
            dataStore.deleteDownloadTask(taskId)
        }
    }

    /**
     * 全部开始
     */
    fun startAll() {
        _state.value.tasks.filter { it.status == DownloadStatus.WAITING || it.status == DownloadStatus.PAUSED }
            .forEach { task ->
                downloadManager.startDownload(task.id)
            }
    }

    /**
     * 全部暂停
     */
    fun pauseAll() {
        _state.value.tasks.filter { it.status == DownloadStatus.DOWNLOADING }
            .forEach { task ->
                downloadManager.pauseDownload(task.id)
            }
    }

    /**
     * 清除所有已完成的任务
     */
    fun clearAllCompleted() {
        _state.value.tasks.filter { it.status == DownloadStatus.COMPLETED }
            .forEach { task ->
                downloadManager.removeTask(task.id)
                launch {
                    dataStore.deleteDownloadTask(task.id)
                }
            }
    }

    /**
     * 重试所有失败的任务
     */
    fun retryAllFailed() {
        _state.value.tasks.filter { it.status == DownloadStatus.FAILED }
            .forEach { task ->
                downloadManager.retryDownload(task.id)
            }
    }

    /**
     * 批量删除任务
     */
    fun deleteTasks(taskIds: Set<String>) {
        taskIds.forEach { taskId ->
            downloadManager.cancelDownload(taskId)
            launch {
                dataStore.deleteDownloadTask(taskId)
            }
        }
    }

    /**
     * 批量开始下载
     */
    fun startDownloads(taskIds: Set<String>) {
        taskIds.forEach { taskId ->
            val task = downloadManager.getTaskById(taskId)
            if (task?.status == DownloadStatus.WAITING || task?.status == DownloadStatus.PAUSED) {
                downloadManager.startDownload(taskId)
            }
        }
    }

    /**
     * 批量暂停下载
     */
    fun pauseDownloads(taskIds: Set<String>) {
        taskIds.forEach { taskId ->
            val task = downloadManager.getTaskById(taskId)
            if (task?.status == DownloadStatus.DOWNLOADING) {
                downloadManager.pauseDownload(taskId)
            }
        }
    }

    /**
     * 设置最大并发下载数
     */
    fun setMaxConcurrentDownloads(max: Int) {
        downloadManager.maxConcurrentDownloads = max.coerceIn(1, 5)
    }

    /**
     * 获取任务详情
     */
    fun getTaskById(taskId: String): DownloadTask? {
        return downloadManager.getTaskById(taskId)
    }

    /**
     * 获取指定媒体类型的下载任务
     */
    fun getTasksByMediaType(mediaType: MediaType): List<DownloadTask> {
        return _state.value.tasks.filter { it.mediaType == mediaType }
    }

    /**
     * 获取已完成任务数
     */
    fun getCompletedCount(): Int = _state.value.completedCount

    /**
     * 获取正在下载任务数
     */
    fun getDownloadingCount(): Int = _state.value.downloadingCount
}