package com.mpvp.platform

/**
 * iOS平台实现
 */
class IOSPlatform : Platform {
    override val name: String = "iOS"
    override val isAndroid: Boolean = false
    override val isDesktop: Boolean = false
    override val isIOS: Boolean = true
    override val isWeb: Boolean = false
}

/**
 * 获取平台实例 - iOS实现
 */
actual fun getPlatform(): Platform = IOSPlatform()
