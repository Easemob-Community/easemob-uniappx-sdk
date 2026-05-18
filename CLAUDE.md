# CLAUDE.md

This file provides guidance to Claude Code (claude.ai/code) when working with code in this repository.

## 项目概览

环信 IM SDK 的 Uni-App X UTS 插件封装，**本仓库当前只关注 iOS 端实现**。本仓库不是常规 npm/gradle 工程，**所有构建和运行都通过 HBuilderX IDE 进行**（没有命令行 build/test 命令）。

- **运行方式**：HBuilderX 4.0+ → 打开 `uts-sdk-demo/` → 运行到 iOS（需自定义调试基座）
- **iOS 依赖**：CocoaPods（`HyphenateChat 4.20.0`，需配合 HBuilderX 内置的 pod 调用环境）
- **iOS 部署**：deploymentTarget = 12

构建日志解析工具:`node parse-build-log.js <html日志文件>`(用于解析 HBuilderX 导出的 iOS 打包日志)。

## 关键目录结构

```
uts-sdk-demo/                                          # uni-app x 演示工程（HBuilderX 项目根）
├── pages/
│   ├── sdk-demo-ios/                                  # ★ iOS 演示页面（sdk-demo, conversation, message, message-operation）
│   └── ...
├── uni_modules/
│   └── easemob-uts-sdk-beta/                          # ★ 当前活跃开发的 SDK 模块
│       └── utssdk/
│           ├── interface.uts                          # 跨平台类型定义（API 契约）
│           ├── index.uts                              # 跨平台入口（条件编译分发）
│           └── app-ios/
│               ├── index.uts                          # iOS UTS 实现（调用 Swift 桥接）
│               ├── UNIEMClient.swift                  # ★ Swift 桥接层（实现 EMClientDelegate / EMChatManagerDelegate）
│               ├── Frameworks/                        # 本地 xcframework（已 .gitignore）
│               └── config.json                        # iOS 依赖（HyphenateChat.xcframework）
docs/                                                  # 完整开发文档
```

> 注意：`uts-sdk-demo/uni_modules/easemob-uts-sdk` 和 `iOS_IM_SDK_V4.16.2/`、`uts-em-chat-main/` 都在 `.gitignore` 中。**修改代码请定位到 `easemob-uts-sdk-beta/utssdk/app-ios/`**。

## iOS 桥接架构

```
┌────────────────────────────────────────────┐
│  .uvue 页面（pages/sdk-demo-ios/*）        │
└────────────────┬───────────────────────────┘
                 │  调用 export function
┌────────────────┴───────────────────────────┐
│  app-ios/index.uts                         │
│  - 业务逻辑、Promise 封装                   │
│  - 调用 UNIEMClient.swift 暴露的顶层 func  │
└────────────────┬───────────────────────────┘
                 │  直接调用 Swift 顶层函数（无需 declare）
┌────────────────┴───────────────────────────┐
│  UNIEMClient.swift                         │
│  - 实现 EMClientDelegate 协议（连接事件）  │
│  - 实现 EMChatManagerDelegate 协议（消息） │
│  - 模块级单例持有 delegate                  │
│  - 暴露 emBridgeXxx 顶层 func              │
└────────────────┬───────────────────────────┘
                 │  调用 HyphenateChat
┌────────────────┴───────────────────────────┐
│  HyphenateChat.xcframework（环信 iOS SDK） │
└────────────────────────────────────────────┘
```

**为什么必须有 Swift 桥接层**：UTS 编译器虽然能 `import 'HyphenateChat'` 的类，但**无法在 UTS 层直接实现 Swift/ObjC 协议（如 `EMClientDelegate`）并注册为代理**（类型系统和编译限制）。同目录下的 `.swift` 文件由 UTS 编译器自动识别其顶层 `func`，无需 `declare`。

### Swift 桥接层设计原则

1. **代理类用 `fileprivate`**：避免与 UTS 侧类型名冲突
2. **模块级单例持有代理**：`private var _emDelegate: EMDelegateNative?`,防止 ARC 释放
3. **暴露独立顶层函数**给 UTS:`func emBridgeSetupDelegate()`、`func emBridgeSetOnConnected(callback: (() -> Void)?)` 等
4. **回调参数用 `NSNumber`**:Swift→UTS 传数字时必须用 `NSNumber`,UTS 侧自动接收为 `number`

### 新增 iOS 代理回调的标准流程

1. 在 `UNIEMClient.swift` 的 delegate 类(如 `EMDelegateNative`)中实现协议方法
2. 添加 callback 属性:`var onNewEventCb: ((NSNumber) -> Void)?`
3. 添加桥接函数:`func emBridgeSetOnNewEvent(callback: ((NSNumber) -> Void)?)`
4. 在 `app-ios/index.uts` 的 `EMClientImpl` 中调用该桥接函数
5. 在 `app-ios/index.uts` 末尾导出对应的公开 function

## 事件传递:必须使用 uni.$emit / uni.$on

**iOS 上无法将含闭包的对象字面量从 `.uvue` 传给 UTS 函数**(运行时崩溃)。SDK 内部用 delegate 接收原生事件,统一通过 `uni.$emit` 广播,业务代码用 `uni.$on` 订阅。

```ts
// App.uvue
onLaunch(() => {
  initEasemob({ appKey: 'easemob-demo#support' })
  enableEventBus()  // ★ 启用事件广播
})

// 任意页面
uni.$on('em:message:received', (data: any) => {
  const messages = JSON.parse(data as string) as any[]  // ★ 必须 JSON.parse
})
```

**事件名规范**(详见 docs/development-guide.md):
- 连接类:`em:connection:connected/disconnected/logout/token_will_expire/token_expired/offline_sync_start/offline_sync_finish`
- 消息类:`em:message:received/cmd_received/read/delivered/recalled`(payload 为 JSON 字符串)

## UTS / Swift 互操作的硬性限制(必须遵守)

修改 `app-ios/index.uts` 或 `UNIEMClient.swift` 时关系到能否编译通过,bug 复现成本高:

1. **`uni.$emit` 不能传 UTS 自定义 type 对象**:经传递后字段会丢失(数组 length 变 0)。**必须发送端 `JSON.stringify`,接收端 `JSON.parse`**。

2. **iOS UTS 闭包字面量崩溃**:在 `.uvue` 层构建 `{ onConnected: () => {} }` 等含闭包的对象字面量直接崩溃。所以监听只能走 `uni.$on`,不能走传 callbacks 对象的 API。

3. **不能强转 `UTSJSONObject` 到自定义 type**(`as Message` / `as any`):运行时会出错。必须逐字段读取后用对象字面量重构。

4. **同名类型从多个文件导入会触发编译器 panic**(`Multiple identifiers equivalent up to span hygiene`)。**统一从 `interface.uts` 导入**,不要在 `app-ios/index.uts` 内联定义同名 type(踩坑过 `ConnectionListenerCallbacks`)。

5. **同名导入再导出会编译失败**(`Function invocation 'getCurrentUser()' expected`)。要么直接 `export { foo } from './sub.uts'`,要么导入时换别名。

6. **UTS 不支持函数提升**:被调用函数必须在调用点之前定义(`<script setup>` 中尤其要注意顺序)。

7. **UTS 不支持泛型与 Map 解构**:`<T = any>`、`for (const [k, v] of someMap)` 都会编译失败。这也是放弃自封装 EventBus、改用 `uni.$emit` 的原因之一。

8. **条件表达式必须是 boolean**:`const x = parseInt(s) || 10` 报错,改用三元 + 显式判断(`Number.isNaN(parsed) ? 10 : parsed`)。

9. **Swift 侧的 `Int` 类型**:UTS 调用 Swift 函数传整数时,UTS 侧的 `number` 会自动转,但反向传 `Int` 给 UTS callback 时建议用 `NSNumber` 包装。

10. **iOS UTS 方法签名遇自定义 type 参数会运行时找不到方法**(`s_addConnectionListenerByJs` 找不到等)。所以原生 callbacks 入参用 `UTSJSONObject` 或基础类型,不要用 type 别名。

## 添加新 iOS API 的标准流程

详见 `docs/development-guide.md`,简述:
1. `interface.uts` 添加类型/方法签名(供 `EasemobClient` 接口或顶层 export 使用)
2. `UNIEMClient.swift` 添加 Swift 实现(若涉及代理协议,在 delegate 类里实现回调 + 添加 emBridge 顶层函数)
3. `app-ios/index.uts` 调用 Swift 函数,Promise 封装异步,在文件末尾 `export function xxx`
4. `index.uts` 顶层在 `// #ifdef APP-IOS` 分支中追加导出
5. 在 `pages/sdk-demo-ios/` 下添加或更新测试页面验证

## iOS 已实现的核心 API

参考 `interface.uts` 中的 `EasemobClient` 接口和 `index.uts` 的 `// #ifdef APP-IOS` 段:

- 初始化/登录/登出:`initEasemob`、`login`、`logout`、`getCurrentUser`、`isLoggedIn`、`isConnected`、`getVersion`
- 消息发送:`sendTextMessage`、`sendCmdMessage`、`sendCustomMessage`
- 会话:`getAllConversationsBySort`、`getAllConversations`、`fetchConversationsFromServer`、`deleteConversation`、`deleteConversationFromServer`
- 已读/未读:`sendConversationReadAck`、`markAllMessagesAsRead`、`markAllConversationsAsRead`、`getUnreadMessageCount`、`getConversationUnreadMsgCount`
- 历史消息:`fetchHistoryMessages`、`searchLocalMessagesByKeywords`、`searchLocalMessagesByTimeRange`、`loadLocalMessages`
- 监听:`addMessageListener`、`removeMessageListener`、`removeAllMessageListeners`(连接类事件统一走 `uni.$on`)

## CocoaPods 集成排查

iOS 编译报 `pod install 失败` 时,先看 `docs/hbuilderx-pod-install-troubleshooting.md`。常见原因是 HBuilderX 调用 pod 时的环境变量与终端不一致。

## 文档索引

工作前先查阅 `docs/`:
- `development-guide.md` - iOS 桥接层设计、事件总线方案演进、踩坑记录(权威参考)
- `quick-reference.md` - API 速查
- `uni-app-x-common-issues.md` - iOS UTS 闭包崩溃、轮询架构等通用问题
- `uts-module-export-guide.md` - UTS 导出语法规范
- `architecture-review-guide.md` - 架构评审材料
- `hbuilderx-pod-install-troubleshooting.md` - iOS pod install 失败排查
- `version-update-4.15.1.md` - 版本更新说明
