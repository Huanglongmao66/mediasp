package com.mpvp.viewmodel

import com.mpvp.model.RadioItem
import com.mpvp.repository.RadioRepository

/**
 * 电台模块ViewModel
 *
 * 管理电台列表、搜索、收藏状态。
 *
 * @property repository 电台仓库
 */
class RadioViewModel(
    repository: RadioRepository
) : MediaListViewModel<RadioItem>(repository)
