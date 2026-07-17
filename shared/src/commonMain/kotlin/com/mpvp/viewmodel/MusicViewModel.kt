package com.mpvp.viewmodel

import com.mpvp.model.MusicItem
import com.mpvp.repository.MusicRepository

/**
 * 音乐模块ViewModel
 *
 * 管理音乐列表、搜索、收藏状态。
 *
 * @property repository 音乐仓库
 */
class MusicViewModel(
    repository: MusicRepository
) : MediaListViewModel<MusicItem>(repository)
