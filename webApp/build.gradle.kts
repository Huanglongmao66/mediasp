// Web 应用模块构建配置
plugins {
    kotlin("js")
    id("org.jetbrains.compose")
}

group = "com.mpvp"
version = "1.0.0"

// Kotlin JS 配置
kotlin {
    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled.set(true)
                }
                // 开发服务器配置
                devServer = (devServer ?: org.jetbrains.kotlin.gradle.targets.js.webpack.WebpackDevServerConfig()).apply {
                    port = 8080
                    static = (static ?: mutableListOf()).apply {
                        add(project.projectDir.path + "/src/jsMain/resources")
                    }
                }
            }
            testTask {
                useKarma {
                    useChromeHeadless()
                }
            }
        }
        binaries.executable()
    }
}

// 依赖配置
dependencies {
    // 依赖共享模块
    implementation(project(":shared"))

    // Compose Web
    implementation(compose.web)

    // Compose 运行时
    implementation(compose.runtime)
    implementation(compose.foundation)
    implementation(compose.material3)
}

// 仓库配置
repositories {
    mavenCentral()
    maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
    google()
}
