// 共享模块构建配置 - 包含跨平台核心业务逻辑
import org.jetbrains.compose.compose

plugins {
    kotlin("multiplatform")
    id("org.jetbrains.compose")
    id("com.android.library")
    kotlin("plugin.serialization")
}

// Kotlin 多平台配置
kotlin {
    // Android 目标配置
    androidTarget {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    // JVM 目标配置（Desktop桌面应用）
    jvm("desktop") {
        compilations.all {
            kotlinOptions {
                jvmTarget = "17"
            }
        }
    }

    // iOS 目标配置 - 支持x64、arm64和模拟器arm64
    val iosX64 = iosX64()
    val iosArm64 = iosArm64()
    val iosSimulatorArm64 = iosSimulatorArm64()

    // iOS Framework 配置
    listOf(iosX64, iosArm64, iosSimulatorArm64).forEach {
        it.binaries.framework {
            baseName = "shared"
            isStatic = true
        }
    }

    // Web (JS) 目标配置
    js(IR) {
        browser()
        binaries.executable()
    }

    // 源集配置
    sourceSets {
        // 通用主代码集 - 跨平台共享代码
        val commonMain by getting {
            dependencies {
                // Compose Multiplatform 核心依赖
                implementation(compose.runtime)
                implementation(compose.foundation)
                implementation(compose.material3)
                implementation(compose.material)
                implementation(compose.ui)
                implementation(compose.preview)
                implementation(compose.components.resources)

                // Kotlin 协程 - 异步编程支持
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.8.0")

                // Kotlin 序列化 - JSON数据处理
                implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")

                // Kotlin 日期时间 - 时间处理
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.5.0")

                // 跨平台设置存储 - DataStore替代方案
                implementation("com.russhwolf:multiplatform-settings:1.1.0")
                implementation("com.russhwolf:multiplatform-settings-coroutines:1.1.0")

                // Ktor 网络请求框架
                implementation("io.ktor:ktor-client-core:3.0.0")
                implementation("io.ktor:ktor-client-content-negotiation:3.0.0")
                implementation("io.ktor:ktor-serialization-kotlinx-json:3.0.0")
                implementation("io.ktor:ktor-client-logging:3.0.0")

                // Koin 依赖注入框架
                implementation("io.insert-koin:koin-core:3.5.0")
                implementation("io.insert-koin:koin-compose:1.1.0")

                // Kermit 跨平台日志库
                implementation("co.touchlab:kermit:2.0.0")

                // AndroidX Media3 媒体播放器
                implementation("androidx.media3:media3-common:1.2.1")
                implementation("androidx.media3:media3-exoplayer:1.2.1")
            }
        }

        // 通用测试代码集
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
            }
        }

        // Android 特定代码集
        val androidMain by getting {
            dependencies {
                // Android Lifecycle ViewModel
                implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.7.0")
                implementation("androidx.lifecycle:lifecycle-runtime-compose:2.7.0")
                implementation("androidx.activity:activity-compose:1.8.2")

                // Android Media3 ExoPlayer
                implementation("androidx.media3:media3-exoplayer:1.2.1")
                implementation("androidx.media3:media3-ui:1.2.1")
                implementation("androidx.media3:media3-common:1.2.1")

                // Android DataStore
                implementation("androidx.datastore:datastore-preferences:1.1.0")

                // Ktor Android 引擎
                implementation("io.ktor:ktor-client-android:3.0.0")

                // Android Koin
                implementation("io.insert-koin:koin-android:3.5.0")
            }
        }

        // Android 测试代码集
        val androidUnitTest by getting

        // Desktop 特定代码集
        val desktopMain by getting {
            dependencies {
                implementation(compose.desktop.currentOs)

                // Ktor Java 引擎
                implementation("io.ktor:ktor-client-java:3.0.0")

                // JavaFX 媒体支持（可选，用于桌面端视频播放）
                implementation("org.openjfx:javafx-media:21:win")
                implementation("org.openjfx:javafx-graphics:21:win")
            }
        }

        // iOS 中间代码集 - 共享iOS特定逻辑
        val iosMain by creating {
            dependsOn(commonMain)
            dependencies {
                implementation("io.ktor:ktor-client-darwin:3.0.0")
            }
        }

        val iosX64Main by getting { dependsOn(iosMain) }
        val iosArm64Main by getting { dependsOn(iosMain) }
        val iosSimulatorArm64Main by getting { dependsOn(iosMain) }

        // Web (JS) 特定代码集
        val jsMain by getting {
            dependencies {
                implementation(compose.web)
                implementation("io.ktor:ktor-client-js:3.0.0")
            }
        }
    }
}

// Android 库配置
android {
    namespace = "com.mpvp.shared"
    compileSdk = 34

    // 默认配置
    defaultConfig {
        minSdk = 24
    }

    // 编译选项
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
