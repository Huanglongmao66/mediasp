package com.mpvp.viewmodel

import com.mpvp.model.NovelItem
import com.mpvp.repository.NovelRepository

/**
 * 小说模块ViewModel
 *
 * 管理小说列表、搜索、收藏状态。
 *
 * @property repository 小说仓库
 */
class NovelViewModel(
    repository: NovelRepository
) : MediaListViewModel<NovelItem>(repository)
