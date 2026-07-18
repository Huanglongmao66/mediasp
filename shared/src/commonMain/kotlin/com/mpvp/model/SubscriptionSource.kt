package com.mpvp.model

import kotlinx.datetime.Clock
import kotlinx.serialization.Serializable

/**
 * 订阅源数据模型
 *
 * 定义一个可扩展的内容订阅源，支持视频/音乐/图片/小说/电台等媒体类型。
 * 用户可在设置中添加自定义订阅源，应用从订阅源拉取内容列表。
 *
 * @property id 唯一标识
 * @property name 订阅源名称
 * @property url 订阅源地址（API或RSS地址）
 * @property type 订阅源支持的媒体类型
 * @property description 订阅源描述
 * @property enabled 是否启用
 * @property apiKey 订阅源API密钥（可选）
 * @property createdAt 创建时间
 * @property updatedAt 更新时间
 */
@Serializable
data class SubscriptionSource(
    val id: String,
    val name: String,
    val url: String,
    val type: MediaType,
    val description: String = "",
    val enabled: Boolean = true,
    val apiKey: String? = null,
    val createdAt: Long = Clock.System.now().toEpochMilliseconds(),
    val updatedAt: Long = Clock.System.now().toEpochMilliseconds()
)

/**
 * 订阅源状态枚举
 */
@Serializable
enum class SubscriptionStatus(val displayName: String) {
    /** 正常 */
    ACTIVE("正常"),

    /** 已禁用 */
    DISABLED("已禁用"),

    /** 同步中 */
    SYNCING("同步中"),

    /** 同步失败 */
    ERROR("同步失败")
}
