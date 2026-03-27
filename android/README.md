# Android 本地离线打包指南

## 前置条件

1. **Android Studio** 2023.3+ (Ladybug)
2. **JDK** 17+
3. **UniApp 离线 SDK** (已下载)

## 快速开始

### 1. 配置离线 SDK

如果你已经下载了 `Android-SDK@5.05.82597_20260319.zip`，直接运行：

```bash
cd android
./setup-offline-sdk.sh
```

或者指定 zip 路径：
```bash
./setup-offline-sdk.sh /path/to/Android-SDK@5.05.82597_20260319.zip
```

### 2. 申请 DCloud AppKey

访问 https://nativesupport.dcloud.net.cn/AppDocs/usesdk/appkey 申请 AppKey

修改 `app/src/main/AndroidManifest.xml`：
```xml
<meta-data
    android:name="dcloud_appkey"
    android:value="你的AppKey" />
```

### 3. 生成本地打包资源

在 HBuilderX 中：
1. 打开 `demo` 项目
2. 点击 **发行** → **原生 App-本地打包** → **生成本地打包 App 资源**
3. 等待生成完成

### 4. 复制前端资源

```bash
./copy-resources.sh
```

### 5. 构建 APK

```bash
# 调试版本
./build.sh debug

# 发布版本  
./build.sh release
```

APK 输出路径：`app/build/outputs/apk/debug/app-debug.apk`

## 手动配置（备选）

如果自动配置失败，可以手动操作：

### 解压 SDK

```bash
# 创建 SDK 目录
mkdir -p SDK

# 解压
unzip ~/Downloads/5.05/Android-SDK@5.05.82597_20260319.zip -d SDK/

# 如果多套了一层目录，调整结构
mv SDK/Android-SDK@*/* SDK/
```

### 复制 data 资源

```bash
cp -r SDK/assets/data app/src/main/assets/
```

### 检查 SDK 版本要求

| 组件 | 版本要求 | 说明 |
|-----|---------|-----|
| compileSdk | 36 | HBuilderX 4.81+ 适配 16KB |
| buildTools | 36.0.0 | - |
| Gradle | 8.14.3 | - |
| Android Gradle Plugin | 8.12.0 | - |

## 工程结构

```
android/
├── SDK/                          # UniApp 离线 SDK（自动解压）
│   ├── libs/                     # 基础库 AAR
│   │   ├── lib.5plus.base-release.aar
│   │   ├── uniapp-v8-release.aar
│   │   ├── utsplugin-release.aar
│   │   └── ...
│   └── assets/data/              # 预设资源
├── app/
│   ├── src/main/
│   │   ├── assets/
│   │   │   ├── data/             # SDK 数据资源
│   │   │   └── apps/             # 前端资源（HBuilderX 生成）
│   │   ├── AndroidManifest.xml   # 应用配置
│   │   └── res/                  # 应用资源
│   └── build.gradle              # App 构建配置
├── uni_modules/easemob-im/       # 环信 UTS 插件
│   └── src/main/kotlin/.../EasemobIMPlugin.kt
├── setup-offline-sdk.sh          # SDK 自动配置脚本 ⭐
├── copy-resources.sh             # 复制前端资源脚本 ⭐
├── build.sh                      # 构建脚本 ⭐
└── check-sdk.sh                  # SDK 检查脚本
```

## 开发调试

### 查看日志

```bash
# 环信 SDK 日志
adb logcat -s EMChat:D

# 插件日志
adb logcat -s EasemobIM:D
```

### 热更新

- **前端代码修改**: 在 HBuilderX 重新生成本地资源 → 运行 `./copy-resources.sh` → 点击 Run
- **UTS 插件修改**: 直接修改 Kotlin 代码 → 点击 Run

## 常见问题

### Q: 找不到 SDK zip 文件？
A: 确保已下载离线 SDK，或手动指定路径：
```bash
./setup-offline-sdk.sh /your/path/Android-SDK@xxx.zip
```

### Q: 编译报错 "Could not find com.android.tools.build:gradle:8.12.0"？
A: 检查网络连接，或修改 `build.gradle` 使用离线模式。

### Q: 运行时提示 "AppKey 错误"？
A: 需要先申请 DCloud AppKey 并配置到 AndroidManifest.xml。

## 参考文档

- [UniApp Android 原生工程配置](https://nativesupport.dcloud.net.cn/AppDocs/usesdk/android.html)
- [UniApp 离线打包 SDK 下载](https://nativesupport.dcloud.net.cn/AppDocs/download/android.html)
- [DCloud AppKey 申请](https://nativesupport.dcloud.net.cn/AppDocs/usesdk/appkey)
