package com.mpvp.viewmodel

import com.mpvp.model.ImageItem
import com.mpvp.repository.ImageRepository

/**
 * 图片模块ViewModel
 *
 * 管理图片列表、搜索、收藏状态。
 *
 * @property repository 图片仓库
 */
class ImageViewModel(
    repository: ImageRepository
) : MediaListViewModel<ImageItem>(repository)
