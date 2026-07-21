package com.mpvp.viewmodel

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.cancel
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

/**
 * 基础ViewModel类
 *
 * 提供ViewModel的基础功能，包括协程作用域管理、状态管理
 * 跨平台兼容，不依赖Android特定的ViewModel类
 */
abstract class BaseViewModel {

    /** ViewModel协程作用域 */
    protected val viewModelScope = CoroutineScope(SupervisorJob() + Dispatchers.Main)

    /**
     * 启动协程
     *
     * @param block 协程代码块
     */
    protected fun launch(block: suspend CoroutineScope.() -> Unit) {
        viewModelScope.launch { block() }
    }

    /**
     * 清理资源
     *
     * 在ViewModel不再使用时调用，取消所有协程
     */
    open fun onCleared() {
        viewModelScope.cancel()
    }
}

/**
 * 带状态的ViewModel基类
 *
 * @param initialState 初始状态
 */
abstract class StateViewModel<T>(initialState: T) : BaseViewModel() {

    /** 可变状态流 */
    protected val _state = MutableStateFlow(initialState)

    /** 状态流（只读） */
    val state: StateFlow<T> = _state.asStateFlow()

    /**
     * 更新状态
     *
     * @param update 更新函数
     */
    protected fun updateState(update: T.() -> T) {
        _state.value = _state.value.update()
    }
}
