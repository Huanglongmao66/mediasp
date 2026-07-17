package com.mpvp.model

import kotlinx.serialization.Serializable

/**
 * 小说项数据模型
 *
 * @property id 唯一标识
 * @property title 书名
 * @property author 作者
 * @property description 简介
 * @property chapterCount 章节数
 * @property coverUrl 封面URL
 * @property sourceUrl 内容地址
 * @property content 正文内容（框架阶段可为空）
 * @property isFavorite 是否收藏
 */
@Serializable
data class NovelItem(
    override val id: String,
    override val title: String,
    val author: String = "未知作者",
    val description: String = "",
    val chapterCount: Int = 0,
    override val coverUrl: String? = null,
    override val sourceUrl: String,
    val content: String = "",
    override val isFavorite: Boolean = false
) : MediaItem {
    override val type: MediaType = MediaType.NOVEL
}
