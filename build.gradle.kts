// 根项目构建配置文件 - 声明所有插件版本但不在此应用
plugins {
    // Kotlin Multiplatform 插件 - 支持跨平台编译
    kotlin("multiplatform") version "1.9.22" apply false

    // Kotlin Android 插件 - Android平台支持
    kotlin("android") version "1.9.22" apply false

    // Compose Multiplatform 插件 - 跨平台UI框架
    id("org.jetbrains.compose") version "1.6.0" apply false

    // Android Application 插件 - Android应用构建
    id("com.android.application") version "8.2.0" apply false

    // Android Library 插件 - Android库构建
    id("com.android.library") version "8.2.0" apply false

    // Kotlin Serialization 插件 - 序列化支持
    kotlin("plugin.serialization") version "1.9.22" apply false
}

// 项目组与版本配置
group = "com.mpvp"
version = "1.0.0"

// 所有子项目共享的仓库配置
repositories {
    google()
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    maven("https://jitpack.io")
}
