package com.mpvp.model

import kotlinx.serialization.Serializable

/**
 * 弹幕数据类
 *
 * @property id 弹幕唯一标识
 * @property content 弹幕内容
 * @property time 出现时间（秒）
 * @property type 弹幕类型
 * @property color 弹幕颜色
 * @property fontSize 字体大小
 * @property userId 发送用户ID
 * @property userName 发送用户名
 */
@Serializable
data class DanmakuItem(
    val id: String,
    val content: String,
    val time: Float,
    val type: DanmakuType = DanmakuType.SCROLL,
    val color: Long = 0xFFFFFF,
    val fontSize: Int = 24,
    val userId: String? = null,
    val userName: String? = null
)

/**
 * 弹幕类型枚举
 */
@Serializable
enum class DanmakuType {
    /** 滚动弹幕 */
    SCROLL,

    /** 顶部弹幕 */
    TOP,

    /** 底部弹幕 */
    BOTTOM
}

/**
 * 弹幕显示状态
 */
data class DanmakuDisplayState(
    val id: String,
    val content: String,
    val x: Float,
    val y: Float,
    val color: Long,
    val fontSize: Int,
    val type: DanmakuType,
    val appearTime: Long = System.currentTimeMillis()
)
