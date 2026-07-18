// Desktop 桌面应用模块构建配置
import org.jetbrains.compose.desktop.application.dsl.TargetFormat

plugins {
    kotlin("jvm")
    id("org.jetbrains.compose")
}

group = "com.mpvp"
version = "1.0.0"

repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}

// 依赖配置
dependencies {
    // 依赖共享模块
    implementation(project(":shared"))

    // Compose Desktop 当前操作系统支持
    implementation(compose.desktop.currentOs)

    // Compose Material3
    implementation(compose.material3)

    // Kotlin 协程 - 提供 Desktop Main dispatcher（必需，否则应用启动崩溃）
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-swing:1.8.0")

    // 跨平台设置存储（Main.kt 直接使用 Settings() 构造 JVM Preferences 后端）
    implementation("com.russhwolf:multiplatform-settings:1.1.0")

    // Ktor 客户端核心（NetworkUtils.createHttpClient 返回 HttpClient）
    implementation("io.ktor:ktor-client-core:3.0.0")
    implementation("io.ktor:ktor-client-java:3.0.0")

    // 日志支持
    implementation("org.slf4j:slf4j-simple:2.0.9")
}

// Compose Desktop 应用配置
compose.desktop {
    application {
        // 主类配置
        mainClass = "com.mpvp.desktop.MainKt"

        // 原生分发配置
        nativeDistributions {
            // 目标打包格式
            targetFormats(
                TargetFormat.Dmg,
                TargetFormat.Msi,
                TargetFormat.Deb,
                TargetFormat.Exe,
                TargetFormat.AppImage
            )

            // 包信息
            packageName = "MultiPlatformVideoPlayer"
            packageVersion = "1.0.0"
            description = "跨平台视频播放器"
            vendor = "MPVP Team"
            copyright = "© 2026 MPVP Team. All rights reserved."

            // Windows 平台配置
            windows {
                iconFile.set(project.file("icons/icon.ico"))
                menuGroup = "MPVP"
                // 请求管理员权限（不需要）
                perUserInstall = true
            }

            // macOS 平台配置
            macOS {
                iconFile.set(project.file("icons/icon.icns"))
                bundleID = "com.mpvp.desktop"
                appCategory = "public.app-category.video"
            }

            // Linux 平台配置
            linux {
                iconFile.set(project.file("icons/icon.png"))
                menuGroup = "Video"
                // 维护Debian包依赖
                debMaintainer = "support@mpvp.example.com"
            }

            // JVM 内存配置
            jvmArgs += listOf(
                "-Xmx2048m",
                "-Dfile.encoding=UTF-8"
            )

            // 包含必要的 Java 模块（解决运行时类找不到问题）
            modules("java.base", "java.desktop", "java.net.http", "java.sql", "java.naming")
        }
    }
}

// Java 编译配置
tasks.withType<JavaCompile> {
    sourceCompatibility = "17"
    targetCompatibility = "17"
}

// Kotlin 编译配置
tasks.withType<org.jetbrains.kotlin.gradle.tasks.KotlinCompile> {
    kotlinOptions {
        jvmTarget = "17"
    }
}

// 源集配置 - 兼容 src/jvmMain 布局
sourceSets {
    named("main") {
        kotlin.srcDir("src/jvmMain/kotlin")
        resources.srcDir("src/jvmMain/resources")
    }
}
