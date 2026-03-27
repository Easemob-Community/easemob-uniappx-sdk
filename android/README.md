# Android 本地离线打包指南

## 前置条件

1. **Android Studio** 2023.3+ (Ladybug)
2. **JDK** 17+
3. **UniApp 离线 SDK** (从 DCloud 官网下载)

## 下载所需文件

### 1. UniApp Android 离线 SDK
访问 https://nativesupport.dcloud.net.cn/AppDocs/download/android.html 下载最新版

### 2. UTS 编译插件（关键）
从下载的离线 SDK 中找到以下文件，复制到 `plugins/` 目录：
- `uts-kotlin-compiler-plugin-0.0.1.jar`
- `uts-kotlin-gradle-plugin-0.0.1.jar`
- `uts-kotlin-common-0.0.1.jar` (如果有)

### 3. UniApp 基础库 AAR
从离线 SDK 的 `SDK/libs/` 目录复制所有 AAR 文件到 `app/libs/`：
- `uniappx-release.aar` (或类似名称)
- `lib.5plus.base-release.aar`
- 其他依赖 AAR

## 工程结构

```
android/
├── app/                          # 主应用模块
│   ├── src/main/
│   │   ├── assets/apps/          # 前端资源目录
│   │   ├── AndroidManifest.xml   # 应用配置
│   │   └── res/                  # 资源文件
│   ├── libs/                     # AAR 依赖库
│   └── build.gradle              # App 构建配置
├── uni_modules/easemob-im/       # 环信 UTS 插件模块
│   ├── src/main/
│   │   ├── java/                 # UTS 编译后的 Kotlin 代码
│   │   └── AndroidManifest.xml   # 插件配置
│   └── build.gradle              # 插件构建配置
├── plugins/                      # UTS 编译插件
│   ├── uts-kotlin-compiler-plugin-0.0.1.jar
│   └── uts-kotlin-gradle-plugin-0.0.1.jar
├── build.gradle                  # 项目构建配置
├── settings.gradle               # 项目设置
└── gradle.properties             # Gradle 配置
```

## 打包步骤

### 第一步：在 HBuilderX 生成本地打包资源

1. 打开 `demo` 项目
2. 点击 **发行** → **原生 App-本地打包** → **生成本地打包 App 资源**
3. 等待生成完成，控制台会输出资源路径，如：
   ```
   /demo/unpackage/resources/app-android
   ```

### 第二步：复制前端资源

将生成的资源复制到 Android 工程：

```bash
# 复制 apps 目录
cp -r demo/unpackage/resources/app-android/apps/* android/app/src/main/assets/apps/

# 复制 uni_modules 中的 UTS 插件源码（用于编译）
cp -r demo/uni_modules/easemob-im android/uni_modules/
```

### 第三步：同步 Gradle

在 Android Studio 中：
1. File → Sync Project with Gradle Files
2. 等待同步完成

### 第四步：编译 UTS 插件（关键）

#### 方案 A：使用 UTS 编译插件（推荐，如果有插件文件）
- 确保 `plugins/` 目录有 UTS 编译插件 JAR 文件
- 直接 Build → Make Project

#### 方案 B：手动编写 Kotlin 混编代码（备选）
如果缺少 UTS 编译插件，可以直接在 `uni_modules/easemob-im/src/main/kotlin/` 目录下编写 Kotlin 代码实现相同功能。

### 第五步：构建 APK

```bash
# 调试版本
./gradlew :app:assembleDebug

# 发布版本
./gradlew :app:assembleRelease
```

APK 输出路径：
- Debug: `app/build/outputs/apk/debug/app-debug.apk`
- Release: `app/build/outputs/apk/release/app-release.apk`

## 调试技巧

### 1. 查看环信 SDK 日志
在 Android Studio 的 Logcat 中过滤：
```
tag:EMChat
```

### 2. UTS 代码断点调试
- 在 `uni_modules/easemob-im/src/main/kotlin/` 的对应 Kotlin 文件中设置断点
- 点击 Debug 'app'

### 3. 热更新
- 前端代码修改：重新执行"生成本地打包资源"，替换 assets 后重新运行
- UTS 插件代码修改：直接修改 Kotlin 代码，点击 Apply Changes 或重新编译

## 常见问题

### Q1: 找不到 UTS 编译插件？
A: 从 DCloud 离线 SDK 的 `plugins/` 目录复制 JAR 文件，如果没有则需要申请 DCloud 企业版授权获取。

### Q2: 编译报错 "Cannot find implementation for io.dcloud.uts.kotlin"？
A: 确保 `build.gradle` 中的 classpath 正确引用了 UTS 插件 JAR 文件路径。

### Q3: 运行时提示 "找不到插件"？
A: 确保 `settings.gradle` 中正确包含了 `':uni_modules:easemob-im'` 模块。

## 联系方式

遇到问题可以：
1. 查看 DCloud 官方文档：https://nativesupport.dcloud.net.cn/
2. 环信技术支持：https://www.easemob.com/support
