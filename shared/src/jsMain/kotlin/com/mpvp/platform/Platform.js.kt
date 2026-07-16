package com.mpvp.platform

/**
 * Web平台实现
 */
class WebPlatform : Platform {
    override val name: String = "Web"
    override val isAndroid: Boolean = false
    override val isDesktop: Boolean = false
    override val isIOS: Boolean = false
    override val isWeb: Boolean = true
}

/**
 * 获取平台实例 - Web实现
 */
actual fun getPlatform(): Platform = WebPlatform()
