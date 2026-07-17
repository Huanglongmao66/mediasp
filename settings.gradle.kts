// 项目设置文件 - 配置所有包含的模块
rootProject.name = "MultiPlatformVideoPlayer"

// 启用Gradle版本目录功能预览（如需要）
enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

// 包含共享模块（核心业务逻辑）
include(":shared")

// 包含Android应用模块
include(":androidApp")

// 包含Desktop桌面应用模块
include(":desktopApp")

// 包含Web应用模块
include(":webApp")

// 声明仓库来源
pluginManagement {
    repositories {
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        kotlin("multiplatform").version("1.9.22")
        kotlin("android").version("1.9.22")
        kotlin("plugin.serialization").version("1.9.22")
        id("org.jetbrains.compose").version("1.6.0")
        id("com.android.application").version("8.2.0")
        id("com.android.library").version("8.2.0")
    }
}

// 声明依赖仓库来源
dependencyResolutionManagement {
    repositories {
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
}
