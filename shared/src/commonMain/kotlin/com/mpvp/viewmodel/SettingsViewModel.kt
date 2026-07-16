package com.mpvp.viewmodel

import com.mpvp.model.PlayerConfig
import com.mpvp.repository.AppDataStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow

/**
 * 设置ViewModel
 *
 * 管理播放器配置的加载和持久化
 *
 * @property dataStore 数据存储
 */
class SettingsViewModel(
    private val dataStore: AppDataStore
) : BaseViewModel() {

    /** 配置状态 */
    private val _config = MutableStateFlow(PlayerConfig())
    val config: StateFlow<PlayerConfig> = _config.asStateFlow()

    init {
        loadConfig()
    }

    /**
     * 从存储加载配置
     */
    private fun loadConfig() {
        launch {
            _config.value = dataStore.getPlayerConfig()
        }
    }

    /**
     * 更新配置并持久化
     *
     * @param config 新配置
     */
    fun updateConfig(config: PlayerConfig) {
        _config.value = config
        launch {
            dataStore.savePlayerConfig(config)
        }
    }
}
