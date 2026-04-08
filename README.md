# Easemob Uni-App X SDK

环信即时通讯 SDK 的 Uni-App X 版本，支持 Android 和 iOS 双平台。

## 文档索引

本项目文档位于 `docs/` 目录，按以下主题组织：

### 快速入门
| 文档 | 说明 |
|------|------|
| [quick-reference.md](docs/quick-reference.md) | 快速参考手册，常用 API 速查 |

### 开发指南
| 文档 | 说明 |
|------|------|
| [development-guide.md](docs/development-guide.md) | 完整开发指南，包含项目结构、API 使用、最佳实践 |
| [uts-module-export-guide.md](docs/uts-module-export-guide.md) | UTS 模块导出规范与注意事项 |

### 问题排查
| 文档 | 说明 |
|------|------|
| [uni-app-x-common-issues.md](docs/uni-app-x-common-issues.md) | Uni-App X 常见问题与解决方案，包含：iOS 轮询架构设计、附件上传进度回调、类型兼容性等 |

### 技术调研
| 文档 | 说明 |
|------|------|
| [pre-research-report.md](docs/pre-research-report.md) | 前期技术调研报告，架构选型分析 |

### 版本更新
| 文档 | 说明 |
|------|------|
| [version-update-4.15.1.md](docs/version-update-4.15.1.md) | v4.15.1 版本更新说明 |

## 项目结构

```
.
├── docs/                       # 文档目录
├── uts-sdk-demo/              # SDK 演示项目
│   ├── pages/                 # 页面代码
│   ├── uni_modules/           # UTS 模块
│   │   └── easemob-uts-sdk/   # 环信 SDK 模块
│   │       └── utssdk/
│   │           ├── app-android/   # Android 平台实现
│   │           └── app-ios/       # iOS 平台实现
│   ├── android/               # Android 原生工程
│   └── ios/                   # iOS 原生工程
├── iOS_IM_SDK_V4.16.2/        # iOS 环信 SDK
└── uts-em-chat-main/          # UTS 模块发布包
```

## 快速开始

1. 阅读 [quick-reference.md](docs/quick-reference.md) 了解基本用法
2. 参考 [uts-sdk-demo](uts-sdk-demo/) 演示项目
3. 遇到问题查看 [uni-app-x-common-issues.md](docs/uni-app-x-common-issues.md)

## 平台支持

- **Android**: 完整支持，使用 Kotlin 混编
- **iOS**: 完整支持，使用 Swift 桥接 + 轮询架构

## 核心功能

- 用户登录/登出
- 文本消息发送/接收
- 图片/视频消息（含上传进度回调）
- 透传消息（CMD）
- 自定义消息
- 连接状态监听

