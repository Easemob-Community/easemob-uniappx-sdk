# 环信 IM SDK UniAppX 工程版（Beta）

> **版本**: Beta 1.0.0  
> **环信原生 SDK**: 4.20.0 (Android & iOS)  
> **适用平台**: Android / iOS (UniAppX)  
> **更新日期**: 2026-04-28

环信即时通讯 SDK 的 Uni-App X 版本，通过 UTS 插件桥接原生 Android/iOS SDK，实现跨平台 IM 能力。

---

## ⚠️ 版本声明（必读）

本版本为**工程测试版（Beta）**，供接入方验证 UniAppX 环境下的环信 IM 基础能力。

**Android 与 iOS 当前能力差异极大，请务必阅读下表后再评估测试范围：**

| 功能模块 | Android | iOS | 说明 |
|---------|---------|-----|------|
| SDK 初始化 | ✅ | ✅ | 双端可用 |
| 用户登录/登出 | ✅ | ✅ | 密码登录、Token 登录均支持 |
| 连接状态监听 | ✅ | ✅ | onConnected / onDisconnected / onLogout |
| Token 过期监听 | ✅ | ✅ | onTokenWillExpire / onTokenExpired |
| 消息接收监听 | ✅ | ✅ | 普通消息、会话已读 |
| CMD 消息接收 | ✅ | ❌ | iOS 未实现 |
| **文本消息发送** | ✅ | ❌ | iOS 未实现 |
| **CMD 消息发送** | ✅ | ❌ | iOS 未实现 |
| **自定义消息发送** | ✅ | ❌ | iOS 未实现 |
| 图片/语音/视频/文件消息 | ❌ | ❌ | 双端均未实现 |
| 会话列表（服务端） | ✅ | ❌ | iOS 未实现 |
| 会话列表（本地） | ✅ | ❌ | iOS 未实现 |
| 未读消息数 | ✅ | ❌ | iOS 未实现 |
| 已读回执/清零 | ✅ | ✅ | 双端可用 |
| 删除会话 | ✅ | ❌ | iOS 未实现 |
| 历史消息（服务端） | ✅ | ❌ | iOS 未实现 |
| 消息搜索（本地） | ✅ | ❌ | iOS 未实现 |
| 状态查询（版本/连接/登录） | ❌ | ✅ | Android 为桩函数 |

**测试建议**：
- **Android**：可完整测试初始化 → 登录 → 收发消息 → 会话管理 → 历史消息全链路。
- **iOS**：当前仅能测试初始化 → 登录 → 连接监听 → 接收消息（需对方从 Android 或原生 SDK 发送）。

---

## 1. 环境要求

### 1.1 开发工具

| 工具 | 版本要求 | 说明 |
|------|---------|------|
| HBuilderX | 4.0+ | 必须支持 UniAppX / UTS 编译 |
| Android Studio | 最新稳定版 | Android 原生调试 |
| Xcode | 14.0+ | iOS 原生调试 |
| CocoaPods | 1.10.1+ | iOS 依赖管理（必需） |

### 1.2 系统版本

| 平台 | 最低版本 | 架构 |
|------|---------|------|
| Android | API 21 (Android 5.0) | armeabi-v7a, arm64-v8a |
| iOS | 12.0 | arm64 |

---

## 2. 快速接入（3 步）

### Step 1: 复制 SDK 模块到项目

将 `uts-sdk-demo/uni_modules/easemob-uts-sdk-beta/` **整体复制**到你的 UniAppX 项目根目录下：

```
your-uniappx-project/
├── uni_modules/
│   └── easemob-uts-sdk-beta/     ← 复制到这里
├── pages/
├── manifest.json
├── App.uvue
└── ...
```

模块内部结构：

```
easemob-uts-sdk-beta/
├── package.json
├── readme.md
└── utssdk/
    ├── index.uts              ← 统一入口（create / getClient）
    ├── interface.uts          ← 类型定义
    ├── app-android/
    │   ├── index.uts          ← Android 实现
    │   ├── MessageHelper.kt   ← Kotlin 辅助类
    │   ├── FilePickerHelper.kt
    │   └── config.json        ← Android 依赖（io.hyphenate:hyphenate-chat:4.20.0）
    └── app-ios/
        ├── index.uts          ← iOS UTS 实现
        ├── em_bridge.swift    ← Swift 桥接层
        ├── FilePickerHelper.swift
        └── config.json        ← iOS Pods 配置（HyphenateChat 4.20.0）
```

### Step 2: 配置 manifest.json

在 `manifest.json` 中确认以下配置：

```json
{
  "app-android": {
    "distribute": {
      "modules": {
        "Camera": {},
        "Gallery": {}
      }
    }
  }
}
```

> `Camera` 和 `Gallery` 模块用于后续图片消息功能（当前版本虽未实现，但建议提前配置）。

Android 权限说明：SDK 模块内部已包含必需的 `AndroidManifest.xml` 权限（INTERNET、ACCESS_NETWORK_STATE 等），通常无需手动添加。

### Step 3: 初始化 SDK

在项目的 `App.uvue` 中进行全局初始化：

```uts
<script setup lang="uts">
import { create } from '@/uni_modules/easemob-uts-sdk-beta'

onLaunch(() => {
  // 替换为你的环信 AppKey
  const emClient = create({
    appKey: 'your-app-key#your-org',
    autoLogin: false
  })

  // 注册连接监听（建议在 App 初始化时统一注册）
  emClient.onConnected(() => {
    console.log('[App] 环信连接已建立')
  })

  emClient.onDisconnected((errorCode) => {
    console.log('[App] 环信连接已断开, code=' + errorCode)
  })

  emClient.onLogout((errorCode) => {
    console.log('[App] 被强制登出, code=' + errorCode)
  })

  // 注册消息接收监听
  emClient.onMessageReceived((messages) => {
    for (const msg of messages) {
      console.log('[App] 收到消息:', msg.from, msg.body.type, msg.body.message)
    }
  })
})
</script>
```

---

## 3. 最小化使用示例

### 3.1 登录

```uts
<script setup lang="uts">
import { getClient } from '@/uni_modules/easemob-uts-sdk-beta'

const userId = ref('testuser')
const password = ref('123456')

async function handleLogin(): Promise<void> {
  const client = getClient()
  if (client == null) {
    uni.showToast({ title: 'SDK 未初始化', icon: 'none' })
    return
  }

  try {
    await client.login({
      userId: userId.value,
      password: password.value,
      useToken: false
    })
    uni.showToast({ title: '登录成功', icon: 'success' })
  } catch (error) {
    uni.showToast({ title: '登录失败: ' + error, icon: 'none' })
  }
}
</script>
```

### 3.2 发送文本消息（Android 可用）

```uts
import { getClient } from '@/uni_modules/easemob-uts-sdk-beta'

async function sendText(): Promise<void> {
  const client = getClient()
  if (client == null) return

  try {
    const message = await client.sendTextMessage({
      content: '你好，这是测试消息',
      to: 'target_user_id',
      chatType: 'single',     // 'single' | 'group' | 'chatroom'
      ext: { orderId: '12345' } as UTSJSONObject   // 可选：扩展字段
    })
    console.log('发送成功, msgId:', message.msgId)
  } catch (error) {
    console.error('发送失败:', error)
  }
}
```

### 3.3 发送 CMD 消息（Android 可用）

```uts
const message = await client.sendCmdMessage({
  action: 'kick_user',
  to: 'target_user_id',
  chatType: 'single'
})
```

### 3.4 发送自定义消息（Android 可用）

```uts
const message = await client.sendCustomMessage({
  event: 'taobao_order',
  params: { orderId: '12345', shopName: '测试店铺' } as UTSJSONObject,
  to: 'target_user_id',
  chatType: 'single'
})
```

### 3.5 登出

```uts
async function handleLogout(): Promise<void> {
  const client = getClient()
  if (client == null) return
  await client.logout()
}
```

---

## 4. API 功能参考

### 4.1 初始化与实例获取

| API | 参数 | 返回值 | 平台 |
|-----|------|--------|------|
| `create(config)` | `EMInitConfig` | `EasemobClient` | Android / iOS |
| `getClient()` | 无 | `EasemobClient \| null` | Android / iOS |

```uts
export type EMInitConfig = {
  appKey: string;       // 环信控制台获取的 AppKey
  autoLogin?: boolean;  // 是否自动登录，默认 false
}
```

### 4.2 事件监听（注册后持续生效）

| API | 回调参数 | Android | iOS |
|-----|---------|---------|-----|
| `onConnected(listener)` | `() => void` | ✅ | ✅ |
| `onDisconnected(listener)` | `(errorCode: number) => void` | ✅ | ✅ |
| `onLogout(listener)` | `(errorCode: number) => void` | ✅ | ✅ |
| `onTokenWillExpire(listener)` | `() => void` | ✅ | ✅ |
| `onTokenExpired(listener)` | `() => void` | ✅ | ✅ |
| `onMessageReceived(listener)` | `(messages: Message[]) => void` | ✅ | ✅ |
| `onCmdMessageReceived(listener)` | `(messages: Message[]) => void` | ✅ | ❌ |
| `onConversationRead(listener)` | `(from: string, to?: string) => void` | ✅ | ✅ |

> **注意**：所有 `onXxx` 监听注册后**持续生效**，不需要重复注册。建议在 `App.uvue` 的 `onLaunch` 中一次性注册。

### 4.3 登录/登出

| API | 参数 | Android | iOS |
|-----|------|---------|-----|
| `login(config)` | `UTSJSONObject` {userId, password, useToken?} | ✅ | ✅ |
| `logout()` | 无 | ✅ | ✅ |
| `destroy()` | 无 | ✅ | ✅ |

### 4.4 消息发送（Android 独享）

| API | 配置类型 | Android | iOS |
|-----|---------|---------|-----|
| `sendTextMessage(config)` | `SendTextMessageConfig` | ✅ | ❌ |
| `sendCmdMessage(config)` | `SendCmdMessageConfig` | ✅ | ❌ |
| `sendCustomMessage(config)` | `SendCustomMessageConfig` | ✅ | ❌ |

```uts
export type SendTextMessageConfig = {
  content: string;              // 消息内容
  to: string;                   // 接收方 ID
  chatType: string;             // 'single' | 'group' | 'chatroom'
  ext?: UTSJSONObject | null;   // 可选扩展字段
}

export type SendCmdMessageConfig = {
  action: string;               // 命令内容
  to: string;
  chatType: string;
  ext?: UTSJSONObject | null;
}

export type SendCustomMessageConfig = {
  event: string;                // 自定义事件名
  params?: UTSJSONObject;       // 自定义参数
  to: string;
  chatType: string;
  ext?: UTSJSONObject | null;
}
```

### 4.5 会话管理（Android 独享）

| API | 说明 | Android | iOS |
|-----|------|---------|-----|
| `fetchConversationsFromServer(limit, cursor)` | 从服务端分页拉取 | ✅ | ❌ |
| `getAllConversationsBySort()` | 本地获取（按活跃时间排序） | ✅ | ❌ |
| `getAllConversations()` | 本地获取（Map 转数组） | ✅ | ❌ |
| `sendConversationReadAck(convId)` | 发送会话已读回执 | ✅ | ✅ |
| `markAllMessagesAsRead(convId)` | 指定会话未读清零 | ✅ | ✅ |
| `markAllConversationsAsRead()` | 所有会话未读清零 | ✅ | ❌ |
| `getUnreadMessageCount()` | 获取总未读数 | ✅ | ❌ |
| `getConversationUnreadMsgCount(convId)` | 获取指定会话未读数 | ✅ | ❌ |
| `deleteConversationFromServer(convId, convType, isDeleteServerMessages)` | 删除服务端会话 | ✅ | ❌ |
| `deleteConversation(convId, withMessage)` | 删除本地会话 | ✅ | ❌ |

### 4.6 消息查询（Android 独享）

| API | 说明 | Android | iOS |
|-----|------|---------|-----|
| `fetchHistoryMessages(convId, convType, pageSize, startMsgId)` | 服务端历史消息 | ✅ | ❌ |
| `searchLocalMessagesByKeywords(...)` | 本地关键字搜索 | ✅ | ❌ |
| `searchLocalMessagesByTimeRange(...)` | 本地时间范围搜索 | ✅ | ❌ |
| `loadLocalMessages(convId, startMsgId, pageSize)` | 本地读取消息 | ✅ | ❌ |

### 4.7 状态查询（iOS 独享）

| API | 返回值 | Android | iOS |
|-----|--------|---------|-----|
| `getVersion()` | `string` | ❌（桩函数） | ✅ |
| `isConnected()` | `boolean` | ❌（桩函数） | ✅ |
| `isLoggedIn()` | `boolean` | ❌（桩函数） | ✅ |
| `getCurrentUser()` | `string \| null` | ❌（桩函数） | ✅ |

---

## 5. 核心类型定义

```uts
// 消息体
export type MessageBody = {
  type: string;        // 'txt' | 'img' | 'voice' | 'video' | 'location' | 'file' | 'cmd' | 'custom'
  message: string | null;
}

// 消息对象
export type Message = {
  msgId: string;
  from: string;
  to: string;
  conversationId: string;
  chatType: number;    // 0=单聊, 1=群组, 2=聊天室
  body: MessageBody;
}

// 会话对象
export type EMConversation = {
  conversationId: string;
  type: number;
  unreadMsgCount: number;
  lastMessage?: Message | null;
}

// 分页结果
export type EMCursorResult<T> = {
  data: T[];
  cursor: string;      // 下一页游标，空字符串表示没有更多
}
```

---

## 6. Demo 工程说明

`uts-sdk-demo/` 目录下提供完整的演示工程，包含 5 个测试页面：

| 页面 | 路径 | 功能 | 可用平台 |
|------|------|------|---------|
| 首页 | `pages/index/index` | 导航入口 | Android / iOS |
| SDK 测试 | `pages/sdk-demo/sdk-demo` | 初始化 + 登录/登出 + 连接监听 + 消息接收 | Android / iOS |
| 发送消息 | `pages/message/message` | 文本 / CMD / 自定义消息发送 | **仅 Android** |
| 会话列表 | `pages/conversation/conversation` | 会话拉取 / 未读数 / 已读 / 删除 | **仅 Android** |
| 消息操作 | `pages/message-operation/message-operation` | 历史消息 / 消息搜索 | **仅 Android** |

**运行 Demo**：
1. 用 HBuilderX 打开 `uts-sdk-demo` 文件夹。
2. 点击「运行到手机或模拟器」→ 选择 Android 或 iOS。
3. iOS 首次运行会自动执行 `pod install`，请确保 CocoaPods 可用。

---

## 7. 已知问题与限制（Beta 版）

1. **iOS 消息发送全部未实现**
   - `sendTextMessage`、`sendCmdMessage`、`sendCustomMessage` 在 iOS 端会直接抛出 `Error: not implemented on iOS yet`。
   - 预计在后续版本中通过 Swift 桥接层补齐。

2. **iOS 会话管理全部未实现**
   - 拉取会话列表、删除会话、未读数统计、历史消息、消息搜索等均为桩函数。

3. **Android 状态查询为桩函数**
   - `getVersion()`、`isConnected()`、`isLoggedIn()`、`getCurrentUser()` 在 Android 端返回固定值或 `null`。
   - 如需在 Android 获取这些状态，可临时通过环信原生 API 自行桥接。

4. **图片/语音/视频/文件消息**
   - 双端均未实现，仅预留了消息体类型定义（`img`、`voice`、`video`、`file`）。

5. **iOS CMD 消息接收未实现**
   - 普通消息接收已通，但 CMD 消息接收为桩函数。

6. **消息发送无进度回调**
   - 当前 `sendTextMessage` 等 API 返回 `Promise<Message>`，在消息送达服务器后 resolve，无中间进度。

---

## 8. 常见问题排查

### Q1: Android 运行提示 "当前运行的基座未包含 api uni.chooseImage"

已在 `manifest.json` 中配置 `Camera` 和 `Gallery` 模块。如仍报错，请**重新制作自定义调试基座**：

```
HBuilderX → 运行 → 制作自定义调试基座
```

制作完成后，选择「使用自定义基座运行」。

### Q2: iOS 编译报错 "pod install 失败"

请按以下顺序排查：

1. 终端执行 `pod --version` 确认 CocoaPods 已安装。
2. 若使用 RVM 管理 Ruby，检查 `~/.bash_profile` 中是否存在多个 Ruby 版本冲突。
3. 模拟 HBuilderX 的调用方式验证：
   ```bash
   bash --login -c 'pod --version'
   ```
4. 若上述命令报错，请修复本地 Ruby 环境（参考 `docs/hbuilderx-pod-install-troubleshooting.md`）。

### Q3: 收不到消息

排查步骤：
1. 确认双方都已登录且 `onConnected` 已触发。
2. 确认发送方和接收方的 `appKey` 一致（同一应用下）。
3. 确认 `onMessageReceived` 已在 `App.uvue` 的 `onLaunch` 中注册。
4. 检查接收方 ID 是否拼写正确（区分大小写）。

### Q4: 登录失败，错误码 102

错误码 102 表示「用户不存在」。请先在环信控制台创建用户，或使用已注册的用户名密码。

---

## 9. 项目结构

```
.
├── docs/                               # 文档目录
│   ├── user-delivery-guide.md          # 本README的完整版（含更多细节）
│   ├── development-guide.md            # 内部开发指南
│   ├── uni-app-x-common-issues.md      # UTS 常见问题
│   ├── hbuilderx-pod-install-troubleshooting.md  # iOS CocoaPods 排错
│   └── ...
├── uts-sdk-demo/                       # SDK 演示项目
│   ├── pages/                          # 页面代码
│   ├── uni_modules/                    # UTS 模块
│   │   └── easemob-uts-sdk-beta/       # 环信 SDK 模块（交付物）
│   │       └── utssdk/
│   │           ├── app-android/        # Android 平台实现
│   │           └── app-ios/            # iOS 平台实现
│   ├── android/                        # Android 原生工程
│   └── ios/                            # iOS 原生工程
└── README.md                           # 本文件
```

---

## 10. 技术支持

如在本工程版测试过程中遇到问题，请提供以下信息以便定位：

1. 运行平台（Android / iOS）及系统版本
2. HBuilderX 版本号
3. 问题现象及完整报错日志
4. 复现步骤

---

> **⚠️ 免责声明（必读）**
>
> 本内容为开源集成环信 SDK UniAppX 的示例，不排除存在边界外问题。请谨慎使用于生产环境，如果出现问题后果属于预期并且由使用者自负。
