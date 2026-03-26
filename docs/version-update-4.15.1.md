# 版本更新记录 - SDK 4.15.1

## 更新概览

根据环信官方集成文档检查并更新依赖配置：
- Android SDK: `4.11.0` → `4.15.1`
- iOS SDK: `4.11.0` → `4.15.1`

## 官方文档参考

- [Android 集成文档](https://doc.easemob.com/document/android/integration.html)
- [iOS 集成文档](https://doc.easemob.com/document/ios/integration.html)

---

## Android 配置变更

### 1. Maven 坐标变更 ⚠️

**旧配置:**
```json
"dependencies": [
  "com.hyphenate:hyphenate-chat:4.11.0"
]
```

**新配置:**
```json
"dependencies": [
  "io.hyphenate:hyphenate-chat:4.15.1"
]
```

**说明:** 官方已将 Maven 仓库从 `com.hyphenate` 迁移到 `io.hyphenate`

### 2. Target SDK 版本

新增 `targetSdkVersion: 33`（官方要求 targetVersion 33 及以上）

### 3. 最低 SDK 版本

保持 `minSdkVersion: 21`（符合官方要求 API 21+）

### 4. 权限配置更新

根据官方 4.15.1 文档更新 AndroidManifest.xml:

**必需权限:**
- `INTERNET` - 网络访问
- `ACCESS_NETWORK_STATE` - 网络状态
- `WAKE_LOCK` - 后台运行
- `VIBRATE` - 振动通知

**可选权限:**
- `RECORD_AUDIO` - 语音录制
- `CAMERA` - 拍照
- `READ/WRITE_EXTERNAL_STORAGE` - 文件存储
- `ACCESS_FINE/COARSE_LOCATION` - 定位
- `SCHEDULE_EXACT_ALARM` - 定时任务（3.9.8+ 可选）

### 5. CPU 架构

保持支持 `armeabi-v7a` 和 `arm64-v8a`

> 提示：如对 APK 大小敏感，可仅保留 `armeabi-v7a`，但建议保留双架构以兼容更多设备

---

## iOS 配置变更

### 1. SDK 版本更新

**旧配置:**
```json
"dependencies-pods": {
  "HyphenateChat": "4.11.0"
}
```

**新配置:**
```json
"dependencies-pods": {
  "HyphenateChat": "4.15.1"
}
```

### 2. 依赖框架

从 4.11.0 开始，SDK 依赖 `aosl.xcframework`，但 CocoaPods 集成会自动处理，无需手动添加。

### 3. 最低系统版本

保持 `deploymentTarget: "12.0"`（官方要求 iOS 10.0+，建议 12.0+）

### 4. 架构支持

保持 `arm64`（官方推荐）

---

## 开发环境要求

### Android

| 要求 | 版本 | 状态 |
|-----|------|------|
| Android Studio | Meerkat 2024.3.1+ | 推荐 |
| Gradle | 8.0+ | 推荐 |
| targetSdkVersion | 33+ | ✅ 已配置 |
| minSdkVersion | 21+ | ✅ 已配置 |
| JDK | 17+ | 推荐 |

### iOS

| 要求 | 版本 | 状态 |
|-----|------|------|
| Xcode | 最新版 | 推荐 |
| iOS | 12.0+ | ✅ 已配置 |
| CocoaPods | 1.10.1+ | 必需 |
| 架构 | arm64 | ✅ 已配置 |

---

## ProGuard 混淆规则

如需代码混淆，请在项目中添加：

```proguard
-keep class com.hyphenate.** {*;}
-dontwarn com.hyphenate.**
```

---

## 验证步骤

### Android 验证

1. 清理并重新构建自定义基座
2. 检查 Gradle 依赖是否正确下载：`io.hyphenate:hyphenate-chat:4.15.1`
3. 检查 APK 中是否包含以下 so 文件：
   - `libhyphenate.so`
   - `libcipherdb.so`
   - `libaosl.so` (4.11.0+)

### iOS 验证

1. 运行 `pod update` 更新依赖
2. 检查 Pods 中 HyphenateChat 版本是否为 4.15.1
3. 确认包含 `aosl.xcframework`（CocoaPods 自动处理）

---

## 更新时间

2026-03-26

## 相关文件

- `uni_modules/easemob-im/utssdk/app-android/config.json`
- `uni_modules/easemob-im/utssdk/app-android/AndroidManifest.xml`
- `uni_modules/easemob-im/utssdk/app-ios/config.json`
