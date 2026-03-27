# 更新日志

## v1.0.0 (2026-03-27)

### 🎉 初始版本发布

#### 功能特性

- 基于环信 SDK 4.20.0 版本
- 支持 Android 和 iOS 双平台
- 实现基础功能：
  - SDK 初始化
  - 用户注册（开发测试用）
  - 密码登录 / Token 登录
  - 发送文本消息
  - 用户登出
  - 连接状态监听
  - 消息接收监听

#### 平台支持

- ✅ Android (minSdkVersion: 21)
- ✅ iOS (deploymentTarget: 12)
- ⏳ HarmonyOS (待实现)

#### 依赖版本

- Android: `io.hyphenate:hyphenate-chat:4.20.0`
- iOS: `HyphenateChat 4.20.0`

#### 注意事项

- 本版本为初始版本，主要用于验证 Android 端 UTS 插件的调用
- 用户注册功能仅用于开发测试，生产环境请在服务端完成
- 消息监听回调目前仅打印日志，后续版本将支持通过 uni 事件通知前端
