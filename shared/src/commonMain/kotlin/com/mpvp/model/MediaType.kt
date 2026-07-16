package com.mpvp.model

/**
 * 媒体类型枚举
 *
 * 定义应用支持的所有媒体类型，便于扩展新模块。
 * 新增媒体类型时只需在此添加枚举值并实现对应的模型/仓库/页面。
 */
enum class MediaType(val displayName: String) {
    VIDEO("视频"),
    MUSIC("音乐"),
    IMAGE("图片"),
    NOVEL("小说"),
    RADIO("电台")
}
