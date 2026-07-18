// 根项目构建配置文件
// 插件版本在 settings.gradle.kts 的 pluginManagement 中统一管理

// 在根项目中声明 Compose 插件（apply false），确保所有子项目使用同一个 classloader，
// 避免多 classloader 加载导致的 BuildService 参数类型不匹配问题
// 参考: https://github.com/JetBrains/compose-multiplatform/issues/3459
plugins {
    kotlin("multiplatform") apply false
    kotlin("jvm") apply false
    kotlin("android") apply false
    kotlin("plugin.serialization") apply false
    id("org.jetbrains.compose") apply false
    id("com.android.application") apply false
    id("com.android.library") apply false
}

// 项目组与版本配置
group = "com.mpvp"
version = "1.0.0"

// 所有子项目共享的仓库配置
allprojects {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
    }
}
