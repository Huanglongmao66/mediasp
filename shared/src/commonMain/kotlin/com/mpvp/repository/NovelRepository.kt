package com.mpvp.repository

import com.mpvp.model.NovelItem

/**
 * 小说仓库
 *
 * 框架阶段使用内置示例数据，后续可替换为网络书源/本地TXT实现。
 */
class NovelRepository : InMemoryMediaRepository<NovelItem>(
    initialItems = listOf(
        NovelItem(
            id = "novel_1",
            title = "示例小说一",
            author = "示例作者",
            description = "这是一本示例小说的简介内容，用于演示小说模块框架。",
            chapterCount = 120,
            sourceUrl = "novel://example/1",
            content = "第一章 示例内容\n\n这是一段示例正文，用于演示小说阅读器框架。\n\n第二章 示例内容\n\n小说正文在此展开……"
        ),
        NovelItem(
            id = "novel_2",
            title = "示例小说二",
            author = "另一位作者",
            description = "这是第二本示例小说的简介。",
            chapterCount = 86,
            sourceUrl = "novel://example/2",
            content = "第一章 示例内容\n\n第二本小说的正文在此展开……"
        ),
        NovelItem(
            id = "novel_3",
            title = "示例小说三",
            author = "示例作者",
            description = "这是第三本示例小说的简介。",
            chapterCount = 200,
            sourceUrl = "novel://example/3",
            content = "第一章 示例内容\n\n第三本小说的正文在此展开……"
        )
    )
) {
    override fun updateFavorite(item: NovelItem, isFavorite: Boolean): NovelItem =
        item.copy(isFavorite = isFavorite)
}
