// 项目设置文件 - 配置所有包含的模块
rootProject.name = "MultiPlatformVideoPlayer"

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
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/gradle-plugin")
        maven("https://maven.aliyun.com/repository/public")
        google()
        mavenCentral()
        gradlePluginPortal()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    }
    plugins {
        kotlin("multiplatform").version("1.9.24")
        kotlin("android").version("1.9.24")
        kotlin("jvm").version("1.9.24")
        kotlin("plugin.serialization").version("1.9.24")
        kotlin("plugin.compose").version("1.9.24")
        id("org.jetbrains.compose").version("1.6.11")
        id("com.android.application").version("8.5.2")
        id("com.android.library").version("8.5.2")
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
