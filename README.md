# MultiPlatform Video Player (MPVP)

<div align="center">

**跨平台视频播放器**

基于 Kotlin + Compose Multiplatform 开发的现代化跨平台视频播放器

支持 Windows / Android / iOS / macOS / Web 五大平台

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose%20Multiplatform-1.6.0-blue?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-green)](#)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange)](LICENSE)

</div>

---

## 📖 项目简介

MultiPlatform Video Player (MPVP) 是一款基于 Kotlin Multiplatform 和 Compose Multiplatform 技术栈开发的跨平台视频播放器应用。采用现代化的 MVVM + Clean Architecture 架构，提供一致的跨平台用户体验。

### 🎯 设计目标

- **跨平台一致性**: 一套代码，多端运行，保持功能一致性
- **现代化架构**: 采用MVVM + Clean Architecture，易于维护和扩展
- **模块化设计**: 功能模块独立，便于扩展新功能（音乐、图片、小说等）
- **用户体验优先**: 流畅的播放体验，美观的界面设计

---

## ✨ 核心功能

- 🎬 **视频播放**: 支持本地视频和网络视频流播放
- 📡 **流媒体支持**: m3u8 / mp4 / 直播源
- ⏯️ **播放控制**: 播放/暂停/进度/倍速/音量
- 💾 **播放记录**: 自动保存播放进度，下次打开继续播放
- ❤️ **收藏管理**: 收藏喜欢的视频，快速访问
- 📂 **本地扫描**: 自动扫描设备中的视频文件
- 🌙 **暗黑模式**: 支持亮色/暗色主题切换
- 📱 **跨平台**: 一个应用，多端使用

---

## 📚 完整文档

完整的项目文档位于 [docs](docs/) 目录：

| 文档 | 说明 |
|------|------|
| [📄 文档导航](docs/README.md) | 文档中心入口 |
| [🔧 开发文档](docs/开发文档.md) | 技术架构、技术选型、开发规范 |
| [📋 开发任务规划文档](docs/开发任务规划文档.md) | 任务分解、时间规划 |
| [📝 开发过程文档](docs/开发过程文档.md) | 开发进度、问题记录 |
| [📖 软件使用说明文档](docs/软件使用说明文档.md) | 用户操作指南 |
| [📐 布局文档](docs/布局文档.md) | 界面设计、布局规范 |

---

## 🏗️ 技术架构

### 技术栈

| 技术 | 版本 | 用途 |
|------|------|------|
| Kotlin | 1.9.22 | 开发语言 |
| Compose Multiplatform | 1.6.0 | UI框架 |
| androidx.media3 | 1.2.1 | 媒体播放器 |
| DataStore | 1.1.0 | 数据存储 |
| Ktor Client | 3.0.0 | 网络请求 |
| Koin | 3.5.0 | 依赖注入 |

### 项目结构

```
MultiPlatformVideoPlayer/
├── shared/                   # 共享模块（核心代码）
│   └── src/
│       ├── commonMain/       # 通用代码
│       ├── androidMain/      # Android特定
│       ├── desktopMain/      # Desktop特定
│       ├── iosMain/          # iOS特定
│       └── webMain/          # Web特定
├── androidApp/               # Android应用
├── desktopApp/               # Desktop应用
├── iosApp/                   # iOS应用
├── webApp/                   # Web应用
└── docs/                     # 项目文档
```

---

## 🚀 快速开始

### 环境要求

| 工具 | 版本要求 |
|------|----------|
| JDK | 17+ |
| Android Studio | 2023.1.1+ |
| Xcode | 15.0+ (iOS开发) |
| Gradle | 8.2+ |

### 运行项目

#### Android
```bash
./gradlew androidApp:installDebug
```

#### Desktop
```bash
./gradlew desktopApp:run
```

#### iOS
```bash
cd iosApp
pod install
open iosApp.xcworkspace
```

#### Web
```bash
./gradlew webApp:jsBrowserDevelopmentRun
```

---

## 🔧 开发状态

### 当前阶段：第一阶段 - 项目初始化与基础架构

**进度**:
- ✅ 文档体系建立完成
- ⏳ 项目基础架构待开发

详细进度请查看 [开发过程文档](docs/开发过程文档.md)

---

## 📈 开发路线图

### v1.0.0 (当前版本)
- [x] 完成文档体系建立
- [ ] 跨平台基础架构
- [ ] 视频播放核心功能
- [ ] 播放控制
- [ ] 本地视频扫描
- [ ] 播放历史记录
- [ ] 收藏功能

### 扩展功能规划
- [ ] 弹幕功能
- [ ] 投屏功能
- [ ] 音乐模块
- [ ] 图片模块
- [ ] 小说模块

---

## 🤝 贡献指南

欢迎所有形式的贡献！请查看 [贡献指南](docs/README文档.md#-贡献指南) 了解详情。

### 贡献流程

1. Fork 本仓库
2. 创建功能分支（`git checkout -b feature/AmazingFeature`）
3. 提交更改（`git commit -m 'feat: Add some AmazingFeature'`）
4. 推送到分支（`git push origin feature/AmazingFeature`）
5. 提交 Pull Request

---

## 📜 开源协议

本项目基于 Apache License 2.0 协议开源。详见 [LICENSE](LICENSE) 文件。

---

## 💬 联系方式

- 📧 邮箱: support@mpvp.example.com
- 📖 文档: [docs/](docs/)
- 🐛 问题反馈: [GitHub Issues](https://github.com/yourusername/MultiPlatformVideoPlayer/issues)

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个 Star ⭐**

Made with ❤️ by MPVP Team

</div>