package com.mpvp.utils

import kotlinx.coroutines.CoroutineExceptionHandler
import kotlinx.coroutines.SupervisorJob

/**
 * 全局异常处理工具类
 *
 * 提供统一的异常处理机制，包括：
 * - 网络异常处理
 * - 文件解析异常处理
 * - 播放器异常处理
 * - 协程异常处理
 */
object ExceptionHandler {

    /**
     * 错误类型枚举
     */
    enum class ErrorType(val message: String) {
        NETWORK_ERROR("网络连接失败，请检查网络设置"),
        NETWORK_TIMEOUT("网络请求超时，请稍后重试"),
        FILE_NOT_FOUND("文件不存在或已被删除"),
        FILE_PARSE_ERROR("文件解析失败，格式可能不支持"),
        VIDEO_FORMAT_ERROR("不支持的视频格式"),
        PLAYBACK_ERROR("视频播放失败"),
        BUFFER_ERROR("视频缓冲失败"),
        PERMISSION_DENIED("权限不足，请授予相关权限"),
        STORAGE_ERROR("存储空间不足"),
        UNKNOWN_ERROR("发生未知错误")
    }

    /**
     * 创建协程异常处理器
     *
     * @param onError 错误回调
     * @return CoroutineExceptionHandler
     */
    fun createCoroutineExceptionHandler(
        onError: (String) -> Unit
    ): CoroutineExceptionHandler {
        return CoroutineExceptionHandler { _, throwable ->
            val errorMessage = getErrorMessage(throwable)
            onError(errorMessage)
        }
    }

    /**
     * 根据异常获取用户友好的错误信息
     *
     * @param throwable 异常对象
     * @return 错误信息
     */
    fun getErrorMessage(throwable: Throwable): String {
        val throwableName = throwable::class.simpleName ?: ""
        val message = throwable.message
        return when {
            // 网络相关异常
            throwableName.contains("UnknownHostException", ignoreCase = true) -> ErrorType.NETWORK_ERROR.message
            throwableName.contains("ConnectException", ignoreCase = true) -> ErrorType.NETWORK_ERROR.message
            throwableName.contains("SocketTimeoutException", ignoreCase = true) -> ErrorType.NETWORK_TIMEOUT.message
            throwableName.contains("IOException", ignoreCase = true) -> ErrorType.NETWORK_ERROR.message
            // 文件相关异常
            throwableName.contains("FileNotFoundException", ignoreCase = true) -> ErrorType.FILE_NOT_FOUND.message
            // 权限异常
            throwable is SecurityException -> ErrorType.PERMISSION_DENIED.message
            // 参数异常
            throwable is IllegalArgumentException -> ErrorType.FILE_PARSE_ERROR.message
            // 其他异常
            message.isNullOrBlank() -> ErrorType.UNKNOWN_ERROR.message
            else -> "${ErrorType.UNKNOWN_ERROR.message}: $message"
        }
    }

    /**
     * 根据错误类型获取错误信息
     *
     * @param errorType 错误类型
     * @return 错误信息
     */
    fun getErrorMessage(errorType: ErrorType): String {
        return errorType.message
    }

    /**
     * 安全执行操作
     *
     * 捕获异常并返回结果或错误信息
     *
     * @param block 要执行的代码块
     * @param onError 错误回调
     * @return 操作结果，失败返回null
     */
    fun <T> safeRun(
        block: () -> T,
        onError: ((String) -> Unit)? = null
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            val errorMessage = getErrorMessage(e)
            onError?.invoke(errorMessage)
            null
        }
    }

    /**
     * 安全执行异步操作
     *
     * @param block 要执行的挂起代码块
     * @param onError 错误回调
     * @return 操作结果，失败返回null
     */
    suspend fun <T> safeRunSuspend(
        block: suspend () -> T,
        onError: ((String) -> Unit)? = null
    ): T? {
        return try {
            block()
        } catch (e: Exception) {
            val errorMessage = getErrorMessage(e)
            onError?.invoke(errorMessage)
            null
        }
    }
}

/**
 * 播放器错误处理工具
 */
object PlayerErrorHandler {

    /**
     * 播放器错误类型
     */
    enum class PlayerError(val code: Int, val message: String) {
        SOURCE_ERROR(1001, "视频源错误"),
        RENDER_ERROR(1002, "视频渲染错误"),
        DECODE_ERROR(1003, "视频解码错误"),
        AUDIO_ERROR(1004, "音频处理错误"),
        NETWORK_ERROR(1005, "网络加载错误"),
        DRM_ERROR(1006, "版权保护错误"),
        UNKNOWN(9999, "未知播放错误")
    }

    /**
     * 根据错误码获取错误信息
     *
     * @param errorCode 错误码
     * @return 错误信息
     */
    fun getErrorMessage(errorCode: Int): String {
        return PlayerError.values().find { it.code == errorCode }?.message
            ?: PlayerError.UNKNOWN.message
    }

    /**
     * 判断错误是否可重试
     *
     * @param errorCode 错误码
     * @return 是否可重试
     */
    fun isRetryable(errorCode: Int): Boolean {
        return when (errorCode) {
            PlayerError.SOURCE_ERROR.code,
            PlayerError.NETWORK_ERROR.code -> true
            PlayerError.DRM_ERROR.code,
            PlayerError.DECODE_ERROR.code -> false
            else -> true
        }
    }

    /**
     * 获取重试建议
     *
     * @param errorCode 错误码
     * @return 重试建议
     */
    fun getRetrySuggestion(errorCode: Int): String {
        return when {
            isRetryable(errorCode) -> "请检查网络连接后点击重试"
            errorCode == PlayerError.DECODE_ERROR.code -> "请尝试切换解码方式"
            errorCode == PlayerError.DRM_ERROR.code -> "此视频受版权保护，无法播放"
            else -> "请尝试重新加载视频"
        }
    }
}
