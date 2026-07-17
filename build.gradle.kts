// 根项目构建配置文件 - 声明所有插件但不在此应用
plugins {
    kotlin("multiplatform") apply false
    kotlin("android") apply false
    id("org.jetbrains.compose") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
    kotlin("plugin.serialization") apply false
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
