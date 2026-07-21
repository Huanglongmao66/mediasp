package com.mpvp.viewmodel

import com.mpvp.model.UiState
import com.mpvp.model.VideoItem
import com.mpvp.platform.FileInfo
import com.mpvp.platform.PlatformFilePicker
import com.mpvp.platform.PlatformFileScanner
import com.mpvp.repository.AppDataStore
import com.mpvp.repository.VideoRepository
import com.mpvp.utils.FileUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

/**
 * 本地文件管理页面状态
 *
 * @property fileList 当前目录的文件列表
 * @property currentPath 当前路径
 * @property navigationStack 路径导航栈
 * @property uiState UI状态
 * @property isLoading 是否正在加载
 * @property selectedFiles 已选择的文件
 * @property importProgress 导入进度
 */
data class LocalFileState(
    val fileList: List<FileInfo> = emptyList(),
    val currentPath: String? = null,
    val navigationStack: List<String> = listOf(""),
    val uiState: UiState<List<FileInfo>> = UiState.Loading,
    val isLoading: Boolean = false,
    val selectedFiles: Set<String> = emptySet(),
    val importProgress: ImportProgress = ImportProgress()
)

/**
 * 导入进度
 *
 * @property total 总数
 * @property current 当前进度
 * @property failedCount 失败数量
 * @property isImporting 是否正在导入
 */
data class ImportProgress(
    val total: Int = 0,
    val current: Int = 0,
    val failedCount: Int = 0,
    val isImporting: Boolean = false,
    val errorMessage: String? = null
)

/**
 * 本地文件管理ViewModel
 *
 * 提供本地文件浏览、选择、导入到视频库等功能
 *
 * @property fileScanner 平台文件扫描器
 * @property filePicker 平台文件选择器
 * @property videoRepository 视频仓库
 * @property dataStore 数据存储
 */
class LocalFileViewModel(
    private val fileScanner: PlatformFileScanner,
    private val filePicker: PlatformFilePicker,
    private val videoRepository: VideoRepository,
    private val dataStore: AppDataStore
) : BaseViewModel() {

    private val _state = MutableStateFlow(LocalFileState())
    val state: StateFlow<LocalFileState> = _state.asStateFlow()

    init {
        loadDirectory(null)
        observeVideos()
    }

    /**
     * 加载指定目录的文件列表
     *
     * @param path 目录路径，为空则加载根目录
     */
    fun loadDirectory(path: String?) {
        launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                val files = if (path == null) {
                    getRootDirectories()
                } else {
                    getDirectoryContents(path)
                }
                _state.value = _state.value.copy(
                    fileList = files,
                    currentPath = path,
                    isLoading = false,
                    uiState = if (files.isEmpty()) UiState.Empty else UiState.Success(files)
                )
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    isLoading = false,
                    uiState = UiState.Error("加载失败: ${e.message}")
                )
            }
        }
    }

    /**
     * 进入子目录
     *
     * @param path 子目录路径
     */
    fun enterDirectory(path: String) {
        val newStack = _state.value.navigationStack.toMutableList()
        newStack.add(path)
        _state.value = _state.value.copy(navigationStack = newStack)
        loadDirectory(path)
    }

    /**
     * 返回上级目录
     */
    fun navigateUp() {
        val stack = _state.value.navigationStack
        if (stack.size > 1) {
            val newStack = stack.dropLast(1)
            _state.value = _state.value.copy(navigationStack = newStack)
            loadDirectory(newStack.last().takeIf { it.isNotEmpty() })
        }
    }

    /**
     * 跳转到指定层级
     *
     * @param index 层级索引
     */
    fun navigateToLevel(index: Int) {
        val stack = _state.value.navigationStack
        if (index in stack.indices) {
            val newStack = stack.subList(0, index + 1)
            _state.value = _state.value.copy(navigationStack = newStack)
            loadDirectory(newStack.last().takeIf { it.isNotEmpty() })
        }
    }

    /**
     * 切换文件选择状态
     *
     * @param filePath 文件路径
     */
    fun toggleFileSelection(filePath: String) {
        val newSelection = _state.value.selectedFiles.toMutableSet()
        if (newSelection.contains(filePath)) {
            newSelection.remove(filePath)
        } else {
            newSelection.add(filePath)
        }
        _state.value = _state.value.copy(selectedFiles = newSelection)
    }

    /**
     * 全选/取消全选
     */
    fun toggleSelectAll() {
        val videoFiles = _state.value.fileList.filter {
            !it.isDirectory && FileUtils.isVideoFile(it.name)
        }
        val allSelected = videoFiles.all { _state.value.selectedFiles.contains(it.path) }
        val newSelection = if (allSelected) {
            _state.value.selectedFiles - videoFiles.map { it.path }.toSet()
        } else {
            _state.value.selectedFiles + videoFiles.map { it.path }.toSet()
        }
        _state.value = _state.value.copy(selectedFiles = newSelection)
    }

    /**
     * 清空选择
     */
    fun clearSelection() {
        _state.value = _state.value.copy(selectedFiles = emptySet())
    }

    /**
     * 通过系统文件选择器选择单个视频文件
     */
    fun pickVideoFile() {
        launch {
            try {
                val path = filePicker.pickVideoFile()
                if (path != null) {
                    importVideoFile(path)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    importProgress = _state.value.importProgress.copy(
                        errorMessage = "选择文件失败: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * 通过系统文件选择器选择多个视频文件
     */
    fun pickMultipleVideoFiles() {
        launch {
            try {
                val paths = filePicker.pickMultipleVideoFiles()
                if (paths.isNotEmpty()) {
                    importVideoFiles(paths)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    importProgress = _state.value.importProgress.copy(
                        errorMessage = "选择文件失败: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * 通过系统文件选择器选择目录
     */
    fun pickDirectory() {
        launch {
            try {
                val path = filePicker.pickDirectory()
                if (path != null) {
                    enterDirectory(path)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(
                    importProgress = _state.value.importProgress.copy(
                        errorMessage = "选择目录失败: ${e.message}"
                    )
                )
            }
        }
    }

    /**
     * 导入已选择的文件
     */
    fun importSelectedFiles() {
        val files = _state.value.selectedFiles.toList()
        if (files.isNotEmpty()) {
            importVideoFiles(files)
        }
    }

    /**
     * 导入单个视频文件
     *
     * @param filePath 文件路径
     */
    fun importVideoFile(filePath: String) {
        importVideoFiles(listOf(filePath))
    }

    /**
     * 导入多个视频文件
     *
     * @param filePaths 文件路径列表
     */
    fun importVideoFiles(filePaths: List<String>) {
        if (filePaths.isEmpty()) return

        launch {
            val total = filePaths.size
            var current = 0
            var failedCount = 0

            _state.value = _state.value.copy(
                importProgress = ImportProgress(
                    total = total,
                    current = 0,
                    failedCount = 0,
                    isImporting = true
                )
            )

            filePaths.forEach { path ->
                try {
                    if (!FileUtils.isVideoFile(path)) {
                        failedCount++
                        return@forEach
                    }

                    val fileInfo = fileScanner.getFileInfo(path)
                    val duration = fileScanner.getVideoDuration(path)
                    val fileSize = fileInfo?.size ?: 0L

                    val videoItem = FileUtils.createVideoItemFromPath(
                        path = path,
                        fileSize = fileSize,
                        duration = duration
                    )
                    dataStore.saveVideo(videoItem)
                } catch (e: Exception) {
                    failedCount++
                } finally {
                    current++
                    _state.value = _state.value.copy(
                        importProgress = _state.value.importProgress.copy(
                            current = current,
                            failedCount = failedCount
                        )
                    )
                }
            }

            _state.value = _state.value.copy(
                importProgress = _state.value.importProgress.copy(
                    isImporting = false,
                    failedCount = failedCount
                ),
                selectedFiles = emptySet()
            )
        }
    }

    /**
     * 扫描本地所有视频
     */
    fun scanAllLocalVideos() {
        launch {
            _state.value = _state.value.copy(isLoading = true)
            try {
                videoRepository.scanLocalVideos().collectLatest { videos ->
                    videos.forEach { video ->
                        videoRepository.updateVideo(video)
                    }
                    _state.value = _state.value.copy(isLoading = false)
                }
            } catch (e: Exception) {
                _state.value = _state.value.copy(isLoading = false)
            }
        }
    }

    /**
     * 清除导入错误消息
     */
    fun clearImportError() {
        _state.value = _state.value.copy(
            importProgress = _state.value.importProgress.copy(errorMessage = null)
        )
    }

    /**
     * 获取根目录列表
     */
    private suspend fun getRootDirectories(): List<FileInfo> {
        return getPlatformRootDirectories()
    }

    /**
     * 获取目录内容
     *
     * @param path 目录路径
     */
    private suspend fun getDirectoryContents(path: String): List<FileInfo> {
        val result = mutableListOf<FileInfo>()
        try {
            val dir = java.io.File(path)
            if (dir.exists() && dir.isDirectory) {
                val files = dir.listFiles() ?: emptyArray()
                files.sortedWith(
                    compareBy({ !it.isDirectory }, { it.name.lowercase() })
                ).forEach { file ->
                    val fileInfo = FileInfo(
                        path = file.absolutePath,
                        name = file.name,
                        size = file.length(),
                        lastModified = file.lastModified(),
                        isDirectory = file.isDirectory
                    )
                    result.add(fileInfo)
                }
            }
        } catch (e: Exception) {
            // 读取失败
        }
        return result
    }

    /**
     * 获取平台特定的根目录
     * 各平台通过依赖注入或平台特性获取
     */
    private fun getPlatformRootDirectories(): List<FileInfo> {
        return getDesktopRootDirectories()
    }

    private fun getDesktopRootDirectories(): List<FileInfo> {
        val roots = mutableListOf<FileInfo>()
        val currentOS = System.getProperty("os.name").lowercase()
        when {
            currentOS.contains("win") -> {
                // Windows: 列出所有盘符
                for (letter in 'A'..'Z') {
                    val drive = java.io.File("$letter:\\")
                    if (drive.exists()) {
                        roots.add(
                            FileInfo(
                                path = drive.absolutePath,
                                name = "${letter}:\\",
                                size = 0L,
                                lastModified = 0L,
                                isDirectory = true
                            )
                        )
                    }
                }
            }
            currentOS.contains("mac") || currentOS.contains("nix") || currentOS.contains("nux") -> {
                // Unix/Linux/macOS: 列出 / 和用户主目录
                val rootFile = java.io.File("/")
                if (rootFile.exists()) {
                    roots.add(
                        FileInfo(
                            path = rootFile.absolutePath,
                            name = "根目录 (/)",
                            size = 0L,
                            lastModified = 0L,
                            isDirectory = true
                        )
                    )
                }
                val homeFile = java.io.File(System.getProperty("user.home"))
                if (homeFile.exists()) {
                    roots.add(
                        FileInfo(
                            path = homeFile.absolutePath,
                            name = "用户目录 (${homeFile.name})",
                            size = 0L,
                            lastModified = 0L,
                            isDirectory = true
                        )
                    )
                }
            }
        }
        return roots
    }

    /**
     * 观察视频列表变化
     */
    private fun observeVideos() {
        launch {
            dataStore.getAllVideos().collectLatest { _ ->
                // 视频列表变化时不需要特别处理，仅作为数据同步
            }
        }
    }
}