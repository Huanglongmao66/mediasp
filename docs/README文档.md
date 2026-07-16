# MultiPlatform Video Player (MPVP)

<div align="center">

**跨平台视频播放器**

基于 Kotlin + Compose Multiplatform 开发的现代化跨平台视频播放器

支持 Windows / Android / iOS / macOS / Web 五大平台

[![Kotlin](https://img.shields.io/badge/Kotlin-1.9.22-purple?logo=kotlin)](https://kotlinlang.org)
[![Compose](https://img.shields.io/badge/Compose%20Multiplatform-1.6.0-blue?logo=jetpackcompose)](https://www.jetbrains.com/lp/compose-multiplatform/)
[![Platform](https://img.shields.io/badge/Platform-Android%20%7C%20iOS%20%7C%20Desktop%20%7C%20Web-green)](#)
[![License](https://img.shields.io/badge/License-Apache%202.0-orange)](LICENSE)

[功能特性](#-功能特性) • [快速开始](#-快速开始) • [技术架构](#-技术架构) • [贡献指南](#-贡献指南)

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

## ✨ 功能特性

### 核心功能

| 功能 | 描述 | 状态 |
|------|------|------|
| 🎬 视频播放 | 本地视频 + 网络视频流播放 | ✅ |
| 📡 流媒体支持 | m3u8 / mp4 / 直播源 | ✅ |
| ⏯️ 播放控制 | 播放/暂停/进度/倍速/音量 | ✅ |
| 💾 播放记录 | 自动保存播放进度 | ✅ |
| ❤️ 收藏管理 | 收藏喜欢的视频 | ✅ |
| 📂 本地扫描 | 自动扫描设备视频 | ✅ |
| 🌙 暗黑模式 | 亮色/暗色主题切换 | ✅ |

### 扩展功能（规划中）

| 功能 | 描述 | 状态 |
|------|------|------|
| 💬 弹幕功能 | 实时弹幕显示 | 🔜 |
| 📺 投屏功能 | 投屏到电视等设备 | 🔜 |
| 🎵 音乐模块 | 音乐播放功能 | 🔜 |
| 🖼️ 图片模块 | 图片浏览功能 | 🔜 |
| 📖 小说模块 | 小说阅读功能 | 🔜 |

---

## 🖼️ 界面预览

### Android平台

```
首页                      播放页面                  本地视频
┌─────────────┐         ┌─────────────┐         ┌─────────────┐
│ 🎬 视频播放器 │         │ ← 视频标题   │         │ 📂 本地视频  │
├─────────────┤         ├─────────────┤         ├─────────────┤
│[本地][收藏] │         │             │         │ ┌─────┐     │
│[历史][在线] │         │   🎬 视频   │         │ │封面1│ ... │
├─────────────┤         │             │         │ │时长 │     │
│ ┌───┐ ┌───┐ │         │    ⏯️      │         │ └─────┘     │
│ │封面│ │封面│ │         │             │         │             │
│ │标题│ │标题│ │         ├─────────────┤         │ ┌─────┐     │
│ └───┘ └───┘ │         │ ▶ ━━●━━━ ⏸ │         │ │封面2│ ... │
│ ┌───┐ ┌───┐ │         │ 🔊 ━━━━━━   │         │ │时长 │     │
│ │封面│ │封面│ │         └─────────────┘         │ └─────┘     │
│ └───┘ └───┘ │                                 └─────────────┘
└─────────────┘
```

### Desktop平台

```
桌面主窗口
┌──────────────────────────────────────────────┐
│ 🎬 MultiPlatform Video Player    🔍  ⚙️  ✕  │
├──────────────────────────────────────────────┤
│  [本地]  [收藏]  [历史]  [在线]  [设置]     │
├──────────────────────────────────────────────┤
│  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐
│  │  封面  │  │  封面  │  │  封面  │  │  封面  │
│  │  标题  │  │  标题  │  │  标题  │  │  标题  │
│  │ 10:30  │  │ 45:12  │  │ 1:20:45│  │ 25:33  │
│  └────────┘  └────────┘  └────────┘  └────────┘
│  ┌────────┐  ┌────────┐  ┌────────┐  ┌────────┐
│  │  封面  │  │  封面  │  │  封面  │  │  封面  │
│  └────────┘  └────────┘  └────────┘  └────────┘
└──────────────────────────────────────────────┘
```

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
| Kotlinx Serialization | 1.6.0 | 序列化 |

### 架构模式

```
┌─────────────────────────────────────────┐
│           Presentation Layer            │
│   Compose UI + ViewModel + StateFlow    │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│             Domain Layer                │
│     Use Cases + Repository Interface    │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│              Data Layer                 │
│  DataStore + Network + FileSystem       │
└─────────────────────────────────────────┘
                    │
┌─────────────────────────────────────────┐
│            Platform Layer               │
│   Android / Desktop / iOS / Web         │
└─────────────────────────────────────────┘
```

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
| Node.js | 18+ (Web开发) |
| Gradle | 8.2+ |

### 克隆项目

```bash
git clone https://github.com/yourusername/MultiPlatformVideoPlayer.git
cd MultiPlatformVideoPlayer
```

### 运行项目

#### Android

```bash
# 方式一：使用Android Studio
# 1. 打开项目
# 2. 同步Gradle
# 3. 点击运行按钮

# 方式二：命令行
./gradlew androidApp:installDebug
```

#### Desktop

```bash
# 运行桌面应用
./gradlew desktopApp:run

# 打包发布版本
./gradlew desktopApp:packageDistributionForCurrentOS
```

#### iOS

```bash
# 1. 安装CocoaPods依赖
cd iosApp
pod install

# 2. 使用Xcode打开项目
open iosApp.xcworkspace

# 3. 在Xcode中运行
```

#### Web

```bash
# 运行Web版本
./gradlew webApp:jsBrowserDevelopmentRun

# 构建生产版本
./gradlew webApp:jsBrowserProductionWebpack
```

---

## 📁 详细文档

完整的项目文档位于 [docs](docs/) 目录：

| 文档 | 说明 |
|------|------|
| [开发文档](docs/开发文档.md) | 技术架构、技术选型、开发规范 |
| [开发任务规划文档](docs/开发任务规划文档.md) | 任务分解、时间规划 |
| [开发过程文档](docs/开发过程文档.md) | 开发进度、问题记录 |
| [软件使用说明文档](docs/软件使用说明文档.md) | 用户操作指南 |
| [布局文档](docs/布局文档.md) | 界面设计、布局规范 |

---

## 🔧 开发指南

### 代码规范

项目遵循 Kotlin 官方编码规范：

- 类名：大驼峰命名（`VideoPlayerViewModel`）
- 函数名：小驼峰命名（`loadVideoList()`）
- 变量名：小驼峰命名（`videoItems`）
- 常量：全大写+下划线（`MAX_BUFFER_SIZE`）

### 提交规范

使用 Conventional Commits 规范：

```
feat: 添加视频播放器组件
fix: 修复播放进度同步问题
docs: 更新API文档
style: 代码格式调整
refactor: 重构播放器架构
test: 添加单元测试
chore: 更新构建脚本
```

### 分支管理

```
main          # 主分支，稳定版本
develop       # 开发分支
feature/*     # 功能分支
bugfix/*      # 修复分支
release/*     # 发布分支
```

---

## 🤝 贡献指南

欢迎所有形式的贡献！

### 贡献流程

1. Fork 本仓库
2. 创建功能分支（`git checkout -b feature/AmazingFeature`）
3. 提交更改（`git commit -m 'feat: Add some AmazingFeature'`）
4. 推送到分支（`git push origin feature/AmazingFeature`）
5. 提交 Pull Request

### 贡献类型

- 🐛 修复Bug
- ✨ 添加新功能
- 📝 完善文档
- 🎨 改进UI设计
- ⚡ 性能优化
- 🌐 多语言翻译

---

## 📈 路线图

### v1.0.0 (当前版本)

- [x] 跨平台基础架构
- [x] 视频播放核心功能
- [x] 播放控制（播放/暂停/进度/倍速）
- [x] 本地视频扫描
- [x] 播放历史记录
- [x] 收藏功能

### v1.1.0 (计划中)

- [ ] 弹幕功能
- [ ] 播放列表管理
- [ ] 视频下载功能
- [ ] 字幕支持

### v1.2.0 (计划中)

- [ ] 投屏功能
- [ ] 后台播放优化
- [ ] 通知栏控制
- [ ] 小窗播放

### v2.0.0 (未来规划)

- [ ] 音乐模块
- [ ] 图片模块
- [ ] 小说模块
- [ ] 漫画模块

---

## 📜 开源协议

本项目基于 Apache License 2.0 协议开源。

详见 [LICENSE](LICENSE) 文件。

---

## 👥 开发团队

### 核心团队

- **项目负责人**: 架构设计、技术决策
- **Android开发**: Android平台开发
- **Desktop开发**: Desktop平台开发
- **iOS开发**: iOS平台开发
- **Web开发**: Web平台开发
- **UI设计**: 界面设计、交互设计
- **测试**: 功能测试、性能测试

### 贡献者

感谢所有贡献者的付出！

---

## 💬 社区与支持

### 联系方式

- 📧 邮箱: support@mpvp.example.com
- 💬 Discord: [加入社区](https://discord.gg/mpvp)
- 🐛 问题反馈: [GitHub Issues](https://github.com/yourusername/MultiPlatformVideoPlayer/issues)

### 关注我们

- 🌐 官网: https://mpvp.example.com
- 📖 文档: https://docs.mpvp.example.com
- 🐙 GitHub: https://github.com/yourusername/MultiPlatformVideoPlayer

---

## 🙏 致谢

感谢以下开源项目的支持：

- [Kotlin](https://kotlinlang.org/) - 编程语言
- [Compose Multiplatform](https://www.jetbrains.com/lp/compose-multiplatform/) - UI框架
- [androidx.media3](https://developer.android.com/media) - 媒体播放器
- [Koin](https://insert-koin.io/) - 依赖注入
- [Ktor](https://ktor.io/) - 网络框架
- [Coil](https://coil-kt.github.io/coil/) - 图片加载

---

<div align="center">

**⭐ 如果这个项目对你有帮助，请给一个 Star ⭐**

Made with ❤️ by MPVP Team

</div>