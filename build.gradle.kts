// 根项目构建配置文件
// 插件版本在 settings.gradle.kts 的 pluginManagement 中统一管理

// 项目组与版本配置
group = "com.mpvp"
version = "1.0.0"

// 所有子项目共享的仓库配置
allprojects {
    repositories {
        maven("https://maven.aliyun.com/repository/google")
        maven("https://maven.aliyun.com/repository/public")
        google()
        mavenCentral()
        maven("https://maven.pkg.jetbrains.space/public/p/compose/dev")
        maven("https://jitpack.io")
    }
}
