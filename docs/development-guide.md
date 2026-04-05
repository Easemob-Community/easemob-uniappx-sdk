# 环信IM SDK UniAppX 开发指南

## 开发环境搭建

### 1. 安装 HBuilderX

- 下载并安装 HBuilderX 4.0+ 版本
- 安装 UTS 编译运行插件

### 2. 配置 Android 开发环境

```
1. 安装 Android Studio
2. 配置 ANDROID_HOME 环境变量
3. 在 HBuilderX 设置中配置 Android SDK 路径
   工具 -> 设置 -> 运行配置 -> Android
```

### 3. 配置 iOS 开发环境（Mac）

```
1. 安装 Xcode 13.0+
2. 安装 CocoaPods
3. 在 HBuilderX 设置中配置 Xcode 路径
   工具 -> 设置 -> 运行配置 -> iOS
```

## 项目结构说明

### UTS 插件目录结构

```
uni_modules/easemob-uts-sdk/
├── package.json                # 插件配置信息
├── utssdk/
│   ├── interface.uts           # 接口定义
│   ├── index.uts               # 跨平台入口（条件编译）
│   ├── app-android/
│   │   ├── index.uts           # Android 平台入口（导出所有API）
│   │   ├── MessageHelper.kt    # Kotlin 辅助类
│   │   ├── config.json         # Android 依赖配置
│   │   ├── AndroidManifest.xml # Android 权限配置
│   │   ├── connection/         # 连接模块
│   │   │   └── listener.uts    # 连接状态监听
│   │   ├── message/            # 消息模块
│   │   │   ├── listener.uts    # 消息监听实现
│   │   │   └── sender.uts      # 消息发送实现
│   │   ├── auth/               # 认证模块
│   │   │   └── login.uts       # 登录/登出实现
│   │   └── core/               # 核心模块
│   │       └── init.uts        # SDK 初始化
│   └── app-ios/
│       ├── index.uts           # iOS 实现
│       └── config.json         # iOS 依赖配置
```

### 文件职责

| 文件 | 职责 | 修改频率 |
|-----|------|---------|
| interface.uts | 定义对外 API 接口、类型 | 低（架构变更时） |
| index.uts | 跨平台入口，条件编译分发，导出所有API | 低 |
| app-android/index.uts | Android 平台入口，导出所有公开API | 中 |
| connection/listener.uts | 连接状态监听实现 | 低 |
| message/listener.uts | 消息监听实现 | 中 |
| message/sender.uts | 消息发送实现 | 中 |
| auth/login.uts | 登录/登出实现 | 低 |
| core/init.uts | SDK 初始化 | 低 |
| MessageHelper.kt | Kotlin 辅助类（解决UTS调用限制） | 中 |
| app-ios/index.uts | iOS 桥接实现 | 中 |

### 模块划分说明

| 模块 | 目录 | 功能 |
|-----|------|------|
| 核心模块 | core/ | SDK初始化 |
| 连接模块 | connection/ | 连接状态监听 |
| 消息模块 | message/ | 消息收发、监听 |
| 认证模块 | auth/ | 登录、登出、用户状态 |

## 开发流程

### 1. 添加新功能的一般步骤

```
1. 在 interface.uts 中定义新接口
2. 在 app-android/index.uts 实现 Android 端
3. 在 app-ios/index.uts 实现 iOS 端
4. 在 index.uts 中添加跨平台分发
5. 更新示例页面测试
6. 更新文档
```

### 2. 示例：添加获取用户详情接口

#### Step 1: interface.uts

```uts
// 定义返回类型
export interface EMUserDetail {
  userId: string
  nickname: string
  avatarUrl: string
  email?: string
  phone?: string
}

// 在 EaseMobIM 接口中添加
export interface EaseMobIM {
  // ... 其他接口
  
  /**
   * 获取用户详情
   * @param userId 用户ID
   * @param onSuccess 成功回调
   * @param onFail 失败回调
   */
  getUserDetail(
    userId: string,
    onSuccess?: (user: EMUserDetail) => void,
    onFail?: EMFailCallback
  ): void
}
```

#### Step 2: app-android/index.uts

```uts
export function getUserDetail(
  userId: string,
  onSuccess?: (user: EMUserDetail) => void,
  onFail?: EMFailCallback
): void {
  if (!checkLogin(onFail)) return
  
  try {
    // 调用环信 Android SDK
    const userInfo = emClient!!.userInfoManager().fetchUserInfoByUserId(
      arrayOf(userId),
      object : com.hyphenate.EMValueCallBack<Map<String, com.hyphenate.chat.EMUserInfo>> {
        override fun onSuccess(value: Map<String, com.hyphenate.chat.EMUserInfo>?) {
          val info = value?.get(userId)
          if (info != null) {
            onSuccess?.({
              userId: info.userId,
              nickname: info.nickName,
              avatarUrl: info.avatarUrl,
              email: info.email,
              phone: info.phoneNumber
            })
          } else {
            onFail?.(EMErrorCode.USER_NOT_FOUND, 'User not found')
          }
        }
        
        override fun onError(errorCode: Int, errorMsg: String?) {
          onFail?.(errorCode, errorMsg ?: 'Failed to get user detail')
        }
      }
    )
  } catch (e) {
    onFail?.(EMErrorCode.GENERAL_ERROR, String(e))
  }
}
```

#### Step 3: app-ios/index.uts

```uts
export function getUserDetail(
  userId: string,
  onSuccess?: (user: EMUserDetail) => void,
  onFail?: EMFailCallback
): void {
  if (!checkLogin(onFail)) return
  
  EMClient.sharedClient.userInfoManager.fetchUserInfo(byId: [userId]) { [weak self] userInfos, error in
    if let error = error {
      onFail?.(error.code, error.errorDescription ?? 'Failed to get user detail')
      return
    }
    
    if let info = userInfos?[userId] {
      onSuccess?({
        userId: info.userId,
        nickname: info.nickname,
        avatarUrl: info.avatarUrl,
        email: info.email,
        phone: info.phone
      })
    } else {
      onFail?.(EMErrorCode.USER_NOT_FOUND, 'User not found')
    }
  }
}
```

#### Step 4: index.uts

```uts
// #ifdef APP-ANDROID
import { getUserDetail as androidGetUserDetail } from './app-android/index.uts'
// #endif

// #ifdef APP-IOS
import { getUserDetail as iosGetUserDetail } from './app-ios/index.uts'
// #endif

export const im = {
  // ... 其他接口
  
  getUserDetail: (userId: string, onSuccess?: any, onFail?: any) => {
    // #ifdef APP-ANDROID
    return androidGetUserDetail(userId, onSuccess, onFail)
    // #endif
    // #ifdef APP-IOS
    return iosGetUserDetail(userId, onSuccess, onFail)
    // #endif
  }
}
```

## 调试技巧

### 1. Android 调试

```uts
// 打印日志
console.log('[EaseMobIM] Debug info:', value)

// Android Studio 断点调试
// 1. HBuilderX 运行到 Android
// 2. 在 Android Studio 中 Attach Debugger
// 3. 在编译后的 Kotlin 代码中打断点
```

### 2. iOS 调试

```uts
// Xcode 断点调试
// 1. HBuilderX 运行到 iOS
// 2. 在 Xcode 中打开生成的工程
// 3. 在编译后的 Swift 代码中打断点
```

### 3. 常见问题排查

#### 问题：SDK 初始化失败

排查步骤：
1. 检查 AppKey 是否正确
2. 检查网络权限是否配置
3. 查看控制台日志输出

#### 问题：消息发送失败

排查步骤：
1. 检查是否已登录
2. 检查接收方 ID 是否正确
3. 检查网络连接状态

#### 问题：收不到消息

排查步骤：
1. 检查是否添加了消息监听器
2. 检查监听器是否被正确移除后未重新添加
3. 检查连接状态

## 代码规范

### 1. 命名规范

- 接口名：PascalCase，如 `EMMessage`, `EMConversation`
- 函数名：camelCase，如 `sendMessage`, `getConversation`
- 常量：UPPER_SNAKE_CASE，如 `EMErrorCode.GENERAL_ERROR`
- 文件：kebab-case，如 `interface.uts`, `unierror.uts`

### 2. 注释规范

```uts
/**
 * 发送消息
 * @param param 发送参数
 * @param onSuccess 成功回调，返回消息对象
 * @param onFail 失败回调
 * @param onProgress 进度回调（用于文件消息）
 */
export function sendMessage(
  param: EMSendMessageParam,
  onSuccess?: (message: EMMessage) => void,
  onFail?: EMFailCallback,
  onProgress?: EMProgressCallback
): void
```

### 3. 错误处理规范

```uts
// 统一错误回调格式
export type EMFailCallback = (code: number, message: string) => void

// 使用时始终提供错误回调
im.sendMessage(
  param,
  (msg) => { /* success */ },
  (code, msg) => {
    // 错误处理
    console.error(`[EMError:${code}] ${msg}`)
  }
)
```

## 发布流程

### 1. 版本号规范

采用语义化版本号：`主版本.次版本.修订号`

- 主版本：不兼容的 API 修改
- 次版本：向下兼容的功能新增
- 修订号：向下兼容的问题修复

### 2. 发布前检查清单

```
□ 所有功能已测试通过
□ 文档已更新
□ 版本号已更新
□ CHANGELOG 已更新
□ 示例代码已更新
```

## 性能优化

### 1. 消息列表优化

- 使用分页加载，避免一次性加载过多消息
- 消息缓存，避免重复转换
- 图片/文件懒加载

### 2. 内存管理

- 及时移除不需要的监听器
- 清理过期的消息缓存
- 避免循环引用

```uts
// 页面卸载时清理
onUnmounted(() => {
  easemobIM.im.removeMessageListener(messageListener)
  easemobIM.im.removeConnectionListener(connectionListener)
})
```

## 相关资源

- [环信 Android SDK API](https://doc.easemob.com/android_product_overview.html)
- [环信 iOS SDK API](https://doc.easemob.com/ios_product_overview.html)
- [UTS 语法参考](https://doc.dcloud.net.cn/uni-app-x/uts/)
- [UTS 插件开发](https://doc.dcloud.net.cn/uni-app-x/plugin/uts-plugin.html)

---

## iOS 监听模块设计实录：事件总线方案

### 背景与问题

iOS 平台的 UTS 编译器在处理回调时存在一个硬性限制：
**在 `.uvue` 页面层中，无法将包含闭包的对象字面量（如 `{ onConnected: () => {} }`）传递给 UTS 插件函数。**

常规写法（Android 测通，但 iOS 崩溃）：

```ts
// 这在 iOS 上会导致 App 崩溃
addConnectionListener({
  onConnected: () => { console.log('已连接') },
  onDisconnected: (code) => { ... }
})
```

根本原因是 iOS UTS 运行时无法在构建对象字面量时将闭包封装成 Swift 对象，传递过程中就就会崩溃。

### 当前 iOS 实现：uni.$emit 事件总线

SDK 内部实现 `class EMConnectionDelegate implements EMClientDelegate` 和 `class EMMessageDelegate implements EMChatManagerDelegate`，由这两个 delegate 接收原生 SDK 事件，然后内部调用 `uni.$emit` 广播。

用户在任意 `.uvue` 页面里用 `uni.$on` 订阅：

```ts
// App.uvue 初始化时开启广播
startConnectionEmit()   // 开启连接事件广播
startMessageEmit()      // 开启消息事件广播

// 任意页面订阅
uni.$on('em_connected', () => { /* 已连接 */ })
uni.$on('em_disconnected', (data: UTSJSONObject) => { /* 断开 */ })
uni.$on('em_message_received', (data: UTSJSONObject) => {
  const messages = JSON.parse(data['messagesJson'] as string) as UTSJSONObject[]
  // 处理消息...
})
```

**广播事件列表：**

| 事件名 | 参数 | 说明 |
|---|---|---|
| `em_connected` | 无 | 连接建立 |
| `em_disconnected` | `{ errorCode: number }` | 连接断开 |
| `em_logout` | `{ errorCode: number }` | 被登出 |
| `em_token_will_expire` | `{ errorCode: number }` | Token 即将过期 |
| `em_token_expired` | `{ errorCode: number }` | Token 已过期 |
| `em_offline_sync_start` | 无 | 开始同步离线消息 |
| `em_offline_sync_finish` | 无 | 离线消息同步完成 |
| `em_message_received` | `{ messagesJson: string }` | 收到普通消息 |
| `em_cmd_message_received` | `{ messagesJson: string }` | 收到透传消息 |
| `em_message_read` | `{ messagesJson: string }` | 消息已读 |
| `em_message_delivered` | `{ messagesJson: string }` | 消息已送达 |
| `em_message_recalled` | `{ messagesJson: string }` | 消息被撤回 |

> 消息类事件的 `messagesJson` 是 JSON 字符串，需先 `JSON.parse()` 再使用。原因是 UTS 自定义 type 对象经过 `uni.$emit` 传递后内部字段会丢失，序列化为字符串是当前可靠的中转方式。

### 深层驱动因素

1. **iOS UTS 闭包传递崩溃**：`.uvue` 层构建包含闭包的对象字面量本身就会崩溃，无法绕过
2. **iOS UTS 方法签名限制**：自定义 type 作为参数时，运行时找不到对应方法（`s_addConnectionListenerByJs` 报错）
3. **uni.$emit 传递 UTS type 字段丢失**：自定义 type 对象不会自动序列化，需手动 JSON 中转

### 待探讨：是否把 Android 也迁移到事件总线模式

#### 现状 (Android)

```ts
// 用户必须按平台写不同的监听代码
addConnectionListener({
  onConnected: () => { ... },
  onDisconnected: (code) => { ... }
})
```

#### 如果迁移到事件总线 (Android)

```ts
// 双平台一致的使用方式
startConnectionEmit()  // 不分平台
uni.$on('em_connected', () => { ... })
uni.$on('em_disconnected', (data) => { ... })
```

#### 迁移的优势

- 双平台 API 一致，用户没有平台差异感知
- `uni.$on` / `uni.$off` 是标准的 uni-app 订阅模式，监听具名化、可中途取消
- 减少用户的心智负担：不需要了解 `addConnectionListener` vs `addConnectionListenerIOS` 的平台差异
- 页面组件层不需要 import 任何 SDK 类型，陆陆续续 `UTSJSONObject` 即可

#### 迁移的代价

- 迁移层面需要在 SDK 内部封装一层事件分发逻辑，增加复杂度
- 消息类事件传递数据时需要 JSON 序列化 / 反序列化，有一定性能开销
- `uni.$on` 全局事件需要小心管理，避免重复注册涉及的内存泄漏

#### 建议

如果 SDK 后续要进行更大范围的跨平台统一重构，可以考虑将连接监听和消息监听全面迁移到事件总线模式。但目前 Android 端的对象字面量传递方式已经测通且简洁，在没有明确需求之前不建议改动。
