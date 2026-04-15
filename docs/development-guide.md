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
│       ├── index.uts           # iOS UTS 层实现
│       ├── em_bridge.swift     # iOS Swift 桥接层（EMClientDelegate 等原生代理）
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
| app-ios/index.uts | iOS UTS 层实现，调用 Swift 桥接函数 | 中 |
| app-ios/em_bridge.swift | iOS Swift 桥接层，实现原生代理和独立回调函数 | 中 |

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
// 3. 可在 em_bridge.swift 和编译后的 Swift 代码中打断点
```

### 3. iOS Swift 桥接层（em_bridge.swift）

#### 为什么需要 em_bridge.swift？

iOS 环信 SDK 的 `EMClientDelegate` 是一个 Objective-C/Swift 协议，包含多个回调方法（如 `connectionStateDidChange`、`tokenDidExpire`、`userAccountDidForcedToLogout` 等）。UTS 编译器虽然可以直接 import `HyphenateChat` 的类，但**无法在 UTS 层直接实现 Swift/ObjC 协议并注册为代理**（存在类型系统和编译限制）。

因此采用 **UTS + Swift 混合桥接** 架构：
- `em_bridge.swift`：纯 Swift 文件，实现 `EMClientDelegate`，管理代理单例，暴露独立函数给 UTS
- `app-ios/index.uts`：调用 Swift 暴露的独立函数，用 Promise 封装异步操作

#### 桥接层设计原则

1. **代理类用 `fileprivate`**：避免与 UTS 侧类型名冲突
2. **模块级单例持有代理**：`private var _emDelegate: EMDelegateNative?`
3. **暴露独立函数**：UTS 编译器自动识别同目录下的 `.swift` 文件，无需 `declare`
4. **回调参数用 `NSNumber`**：Swift→UTS 传递数字时必须用 `NSNumber`，UTS 侧接收为 `number`

#### 典型函数列表

```swift
func emBridgeSetupDelegate()                          // 注册代理
func emBridgeTeardownDelegate()                        // 移除代理
func emBridgeSetOnConnected(callback: (() -> Void)?)   // 设置连接成功回调
func emBridgeSetOnDisconnected(callback: ((NSNumber) -> Void)?)  // 设置断开回调
func emBridgeSetOnLogout(callback: ((NSNumber) -> Void)?)        // 设置登出回调
func emBridgeSetOnTokenWillExpire(callback: (() -> Void)?)       // Token 即将过期
func emBridgeSetOnTokenExpired(callback: (() -> Void)?)          // Token 已过期
```

#### UTS 侧调用方式

```uts
import { EMClient, EMOptions } from 'HyphenateChat';

// initSDK 中初始化并注册代理
EMClient.shared().initializeSDK(with = options);
emBridgeSetupDelegate();

// 设置监听
onConnected(listener: (() => void) | null): void {
  emBridgeSetOnConnected(callback = listener);
}
```

#### 新增 iOS 代理方法时的标准流程

1. 在 `em_bridge.swift` 的 `EMDelegateNative` 中实现代理方法
2. 添加新的 callback 属性（如 `var onNewEventCb: ((NSNumber) -> Void)?`）
3. 添加新的桥接函数（如 `func emBridgeSetOnNewEvent(...)`）
4. 在 `app-ios/index.uts` 的 `EMClientImpl` 中调用该桥接函数
5. 在 `app-ios/index.uts` 末尾导出对应的公开函数

### 4. 常见问题排查

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

### 事件总线方案演进：从双平台差异到统一

#### 背景：为什么放弃自定义 EventBus，改用 uni.$emit/uni.$on

**最初尝试**：在 SDK 内部封装一个跨平台的事件总线（`event-bus.uts`），提供 `EMEventBus.on/once/emit/off` 等 API。

**遇到的问题（UTS 编译限制）**：
1. **不支持泛型**：`EventHandler<T = any>`、`on<T = any>()` 等语法编译失败
2. **不支持 Map 解构**：`for (const [eventName, handlers] of this.atomicListeners)` 在 Android 上类型丢失
3. **uts-proxy 仅支持 `export function`**：`EMEventBus` 是 class 实例 const，无法通过 uts-proxy 导出到 .uvue 层
4. **类型重复声明导致编译器 panic**：同名类型从多个文件导入触发 "Multiple identifiers equivalent up to span hygiene"
5. **iOS UTS 闭包字面量崩溃**：`.uvue` 层构建 `{ onConnected: () => {} }` 对象字面量直接崩溃

**决策**：放弃自封装 EventBus，采用 uni-app X 原生 `uni.$emit` / `uni.$on` 机制。这是唯一能在双平台稳定运行的事件通信方案。

#### 最终方案：`enableEventBus()` + `uni.$on`

```ts
// App.uvue 初始化
import { initSDK, enableEventBus } from "./uni_modules/easemob-uts-sdk"

onLaunch(() => {
  initSDK({ appKey: 'easemob-demo#support' })
  enableEventBus()  // 双平台统一启用事件广播
  
  // 连接事件
  uni.$on('em:connection:connected', () => { /* 已连接 */ })
  uni.$on('em:connection:disconnected', (errorCode: any) => { /* 断开 */ })
  
  // 消息事件（JSON 字符串需解析）
  uni.$on('em:message:received', (data: any) => {
    const messages = JSON.parse(data as string) as any[]
    messages.forEach(msg => {
      console.log(`来自: ${msg['from']}, 类型: ${msg['body']['type']}`)
    })
  })
})
```

#### 事件名规范

| 类别 | 事件名 | 参数 | 说明 |
|---|---|---|---|
| 连接 | `em:connection:connected` | `null` | 连接建立 |
| 连接 | `em:connection:disconnected` | `number` (errorCode) | 连接断开 |
| 连接 | `em:connection:logout` | `number` (errorCode) | 被登出 |
| 连接 | `em:connection:token_will_expire` | `null` | Token 即将过期 |
| 连接 | `em:connection:token_expired` | `null` | Token 已过期 |
| 连接 | `em:connection:offline_sync_start` | `null` | 开始同步离线消息 |
| 连接 | `em:connection:offline_sync_finish` | `null` | 离线消息同步完成 |
| 消息 | `em:message:received` | `string` (JSON) | 收到普通消息 |
| 消息 | `em:message:cmd_received` | `string` (JSON) | 收到 CMD 消息 |
| 消息 | `em:message:read` | `string` (JSON) | 消息已读 |
| 消息 | `em:message:delivered` | `string` (JSON) | 消息已送达 |
| 消息 | `em:message:recalled` | `string` (JSON) | 消息被撤回 |

> 消息类事件传递的是 JSON 字符串，需在接收端 `JSON.parse()`。原因是 UTS 自定义 type 对象经 `uni.$emit` 传递后字段会丢失（iOS length 变 0，Android 强转 UTSJSONObject 抛 ClassCastException）。

---

## enableEventBus() 双端修复记录

### 问题 1：iOS `ConnectionListenerCallbacks__1` 类型不兼容

**现象**：编译报错 `cannot convert value of type 'ConnectionListenerCallbacks__1' to expected argument type 'ConnectionListenerCallbacks'`

**原因**：`app-ios/index.uts` 本地内联定义了 `ConnectionListenerCallbacks`，与 `connection/listener.uts` 导出的同名类型被编译器视为不同类型。

**修复**：删除本地类型定义，从 `./connection/listener.uts` 导入：
```ts
import { ConnectionListenerCallbacks } from './connection/listener.uts'
```

### 问题 2：Android `String?` vs `String` 参数类型不匹配

**现象**：`参数类型不匹配：实际类型为 'String?'，预期类型为 'String'`

**原因**：模块级 `let` 变量声明为 `string | null`，Kotlin 不允许对 mutable property 进行 Smart Cast。

**修复**：用 `const` 局部变量捕获值后传参：
```ts
const connId = Date.now().toString() + ...
connectionListenerId = connId
addConnListenerImpl(connId, callbacks)  // 传局部变量，类型是 string

// null 检查时
const connIdToRemove = connectionListenerId
if (connIdToRemove != null) {
  removeConnListenerImpl(connIdToRemove)
}
```

### 问题 3：Android `ClassCastException`（Message → UTSJSONObject）

**现象**：`uts.sdk.modules.easemobUtsSdk.Message cannot be cast to io.dcloud.uts.UTSJSONObject`

**原因**：UTS 自定义 type 编译为 Kotlin class，不是 UTSJSONObject 子类。

**修复**：`enableEventBus` 中 emit 前 `JSON.stringify`，App.uvue 接收端 `JSON.parse`。

### 问题 4：iOS `uni.$emit` 传递 `Message[]` 后 length 变为 0

**现象**：`[EventBus] 收到消息, 数量: [Number] 0`，但 SDK 日志显示 count: 1

**原因**：UTS 自定义 type 对象经 `uni.$emit` 传到 `.uvue` 层时字段丢失。

**修复**：同问题 3，JSON 序列化中转。

### 问题 5：Android `ConcurrentModificationException`

**现象**：收到消息后崩溃 `java.util.ConcurrentModificationException`

**原因**：环信 SDK 监听回调运行在非主线程，`uni.$emit` 触发 Vue 响应式系统并发修改。

**修复**：所有 `uni.$emit` 包裹在 `setTimeout(() => {}, 0)` 中调度到主线程：
```ts
onMessageReceived: (messages: Message[]) => {
  const json = JSON.stringify(messages)
  setTimeout(() => { uni.$emit(EMMessageEvent.RECEIVED, json) }, 0)
}
```

### 修改文件

- `app-android/index.uts`：导入源类型、局部变量捕获 nullable、JSON.stringify + setTimeout
- `app-ios/index.uts`：导入 ConnectionListenerCallbacks、局部变量捕获 nullable、JSON.stringify
- `App.uvue`：消息事件 `JSON.parse(data as string)` 接收

---

## Android 会话列表拉取实现规范

### 背景

Android 平台实现 `fetchConversationsFromServer` 时，需要把 Kotlin 侧的 `EMCursorResult<EMConversation>` 转换为 UTS 侧可用的 `EMCursorResult<EMConversation>`。由于涉及**数组传递**、**嵌套复杂对象**（`lastMessage` 为 `Message` 类型）以及**UVue 页面渲染**，踩坑点较多，已形成标准规范。

### 接口定义

```uts
// interface.uts
export type MessageBody = {
  type: string;
  message: string | null;
}

export type Message = {
  msgId: string;
  from: string;
  to: string;
  conversationId: string;
  chatType: number;
  body: MessageBody;
}

export type EMConversation = {
  conversationId: string;
  type: number;
  unreadMsgCount: number;
  lastMessage?: Message | null;
}

export type EMCursorResult<T> = {
  data: T[];
  cursor: string;
}

abstract fetchConversationsFromServer(limit: number, cursor: string): Promise<EMCursorResult<EMConversation>>;

abstract getAllConversationsBySort(): Promise<EMConversation[]>;

abstract getAllConversations(): Promise<EMConversation[]>;
```

### Kotlin 辅助类（MessageHelper.kt）

#### 1. 回调签名：数组类型必须用 `UTSArray<UTSJSONObject>`

```kotlin
import io.dcloud.uts.UTSArray
import io.dcloud.uts.UTSJSONObject

fun fetchConversationsFromServer(
    limit: Int,
    cursor: String,
    onSuccess: (conversations: UTSArray<UTSJSONObject>, nextCursor: String) -> Unit,
    onError: (code: Int, message: String) -> Unit
) {
    EMClient.getInstance().chatManager().asyncFetchConversationsFromServer(
        limit,
        cursor,
        object : EMValueCallBack<EMCursorResult<EMConversation>> {
            override fun onSuccess(result: EMCursorResult<EMConversation>) {
                val conversations = result.data ?: emptyList()
                val conversationList = conversations.map { conv ->
                    // 构建 lastMessage 嵌套对象
                    val lastMessageJson = conv.getLastMessage()?.let { lastMsg ->
                        val body = lastMsg.getBody()
                        val bodyObj = UTSJSONObject()
                        when (body) {
                            is com.hyphenate.chat.EMTextMessageBody -> {
                                bodyObj["type"] = "txt"
                                bodyObj["message"] = body.getMessage()
                            }
                            is com.hyphenate.chat.EMImageMessageBody -> {
                                bodyObj["type"] = "img"
                                bodyObj["message"] = body.getRemoteUrl() ?: body.getLocalUrl()
                            }
                            is com.hyphenate.chat.EMVoiceMessageBody -> {
                                bodyObj["type"] = "voice"
                                bodyObj["message"] = body.getRemoteUrl() ?: body.getLocalUrl()
                            }
                            is com.hyphenate.chat.EMVideoMessageBody -> {
                                bodyObj["type"] = "video"
                                bodyObj["message"] = body.getRemoteUrl() ?: body.getLocalUrl()
                            }
                            is com.hyphenate.chat.EMLocationMessageBody -> {
                                bodyObj["type"] = "location"
                                bodyObj["message"] = body.getAddress()
                            }
                            is com.hyphenate.chat.EMFileMessageBody -> {
                                bodyObj["type"] = "file"
                                bodyObj["message"] = body.getRemoteUrl() ?: body.getLocalUrl()
                            }
                            is com.hyphenate.chat.EMCmdMessageBody -> {
                                bodyObj["type"] = "cmd"
                                bodyObj["message"] = body.action()
                            }
                            is com.hyphenate.chat.EMCustomMessageBody -> {
                                bodyObj["type"] = "custom"
                                bodyObj["message"] = body.event()
                            }
                            else -> {
                                bodyObj["type"] = "unknown"
                                bodyObj["message"] = ""
                            }
                        }
                        val msgObj = UTSJSONObject()
                        msgObj["msgId"] = lastMsg.getMsgId()
                        msgObj["from"] = lastMsg.getFrom()
                        msgObj["to"] = lastMsg.getTo()
                        msgObj["conversationId"] = lastMsg.conversationId()
                        msgObj["chatType"] = lastMsg.getChatType().ordinal
                        msgObj["body"] = bodyObj
                        msgObj
                    }
                    val obj = UTSJSONObject()
                    obj["conversationId"] = conv.conversationId()
                    obj["type"] = conv.getType().ordinal
                    obj["unreadMsgCount"] = conv.getUnreadMsgCount()
                    obj["lastMessage"] = lastMessageJson
                    obj
                }
                // 关键：List 必须转为 UTSArray
                val conversationArray = UTSArray<UTSJSONObject>()
                conversationArray.addAll(conversationList)
                onSuccess(conversationArray, result.cursor ?: "")
            }

            override fun onError(error: Int, errorMsg: String) {
                onError(error, errorMsg)
            }
        }
    )
}
```

#### 核心要点

| 要点 | 说明 |
|------|------|
| 数组签名 | Kotlin 侧用 `UTSArray<UTSJSONObject>`，不能用 `List<UTSJSONObject>` |
| 数组转换 | 用 `UTSArray<UTSJSONObject>()` + `addAll(conversationList)` 把 `List` 转成 `UTSArray` |
| 嵌套对象 | 每一层都用 `UTSJSONObject`，不要用 `org.json.JSONObject`，否则 UTS 侧访问会丢失类型 |
| 消息类型 | `type` 字段用字符串标识：`txt/img/voice/video/location/file/cmd/custom/unknown` |

### UTS Android 实现（app-android/index.uts）

```uts
fetchConversationsFromServer(limit: number, cursor: string): Promise<EMCursorResult<EMConversation>> {
  return new Promise((resolve, reject) => {
    try {
      fetchConversationsFromServer(
        limit as Int,  // 注意：Int 大写
        cursor as string,
        (conversations: UTSJSONObject[], nextCursor: string) => {
          const conversationList: EMConversation[] = [];
          const list = conversations;
          for (let i = 0; i < list.length; i++) {
            const conv = list[i];
            const conversation: EMConversation = {
              conversationId: conv['conversationId'] as string,
              type: conv['type'] as number,
              unreadMsgCount: conv['unreadMsgCount'] as number,
            };
            const lastMessage = conv['lastMessage'] as UTSJSONObject | null;
            if (lastMessage != null) {
              const body = lastMessage['body'] as UTSJSONObject | null;
              conversation.lastMessage = {
                msgId: lastMessage['msgId'] as string,
                from: lastMessage['from'] as string,
                to: lastMessage['to'] as string,
                conversationId: lastMessage['conversationId'] as string,
                chatType: lastMessage['chatType'] as number,
                body: {
                  type: body != null ? body['type'] as string : '',
                  message: body != null ? body['message'] as string | null : null,
                },
              };
            }
            conversationList.push(conversation);
          }
          const result: EMCursorResult<EMConversation> = {
            data: conversationList,
            cursor: nextCursor,
          };
          resolve(result);
        },
        (code: number, message: string) => {
          reject(new Error(`Fetch conversations failed: ${code} - ${message}`));
        }
      );
    } catch (error) {
      console.error('[Android] fetchConversationsFromServer error:', error);
      reject(error);
    }
  });
}
```

#### 核心要点

| 要点 | 说明 |
|------|------|
| 整数转换 | `limit as Int`（大写 I），`as int` 会报“找不到名称” |
| 回调参数 | 声明为 `UTSJSONObject[]`，不要用 `any`，否则 `['key']` 会被解析为 `String.get` |
| 禁止强转 | **绝对禁止** `lastMessage as any` 或 `JSON.parse(... ) as Message`，运行时会 `ClassCastException` |
| 对象重构 | 必须逐字段从 `UTSJSONObject` 读取，用对象字面量重新构造 `Message` |

#### 本地会话获取（getAllConversationsBySort / getAllConversations）

除了从服务端拉取，Android SDK 还支持直接从本地内存/数据库获取会话：

```uts
getAllConversationsBySort(): Promise<EMConversation[]> {
  return new Promise((resolve, reject) => {
    try {
      const conversations = getAllConversationsBySort();
      resolve(this.parseConversationList(conversations));
    } catch (error) {
      console.error('[Android] getAllConversationsBySort error:', error);
      reject(error);
    }
  });
}

getAllConversations(): Promise<EMConversation[]> {
  return new Promise((resolve, reject) => {
    try {
      const conversations = getAllConversations();
      resolve(this.parseConversationList(conversations));
    } catch (error) {
      console.error('[Android] getAllConversations error:', error);
      reject(error);
    }
  });
}
```

Kotlin 侧直接调用 `EMChatManager.getAllConversationsBySort()` / `getAllConversations()`，同样通过 `conversationToUTSJSONObject()` 转换，返回 `UTSArray<UTSJSONObject>`。`getAllConversationsBySort` 按活跃时间倒序返回，置顶会话在前；`getAllConversations` 将 `Map<String, EMConversation>` 的值转为数组返回。

### UVue 页面（conversation.uvue）

#### 1. 函数定义顺序

UTS 编译到 Kotlin 时不支持函数提升，**被调用的函数必须在调用者之前定义**。

```uts
// ✅ 正确
async function fetchConversations(limit: number, cursorValue: string): Promise<void> {
  // ...
}
async function handleFetch(): Promise<void> {
  await fetchConversations(limit, '');
}

// ❌ 错误：handleFetch 在前会报“找不到名称 fetchConversations”
```

#### 2. 条件表达式必须使用 boolean

```uts
// ❌ 编译错误
const limit = parseInt(limitStr.value) || 10;

// ✅ 正确
const parsedLimit = parseInt(limitStr.value);
const limit = Number.isNaN(parsedLimit) ? 10 : parsedLimit;
```

#### 3. 模板安全调用

```vue
<text class="last-msg-body">
  [{{ formatMsgType(item.lastMessage?.body?.type ?? '') }}]
  {{ item.lastMessage?.body?.message ?? '[无内容]' }}
</text>
```

### 完整踩坑记录

| 现象 | 根因 | 修复 |
|------|------|------|
| `参数类型不匹配：实际类型为 'Number'，预期类型为 'Int'` | UTS 侧写了 `limit as number` | 改为 `limit as Int` |
| `Unresolved reference: fun String.get(index: Number)` | `(conv as any)['key']` 被解析为 String.get | 声明为 `UTSJSONObject`，直接用 `conv['key']` |
| `ClassCastException: java.util.ArrayList cannot be cast to UTSArray` | Kotlin 回调返回 `List<UTSJSONObject>` | Kotlin 侧改为 `UTSArray` + `addAll` |
| `ClassCastException: UTSJSONObject cannot be cast to Message` | `lastMessage as any` 或 `as Message` 强转 | 逐字段读取，对象字面量重构 |
| `找不到名称“fetchConversations”` | `<script setup>` 中函数未先定义 | 调整函数定义顺序 |
| `Conditional statements must use boolean types` | `||` 用于非 boolean 类型 | 用三元表达式 + 显式布尔判断 |
