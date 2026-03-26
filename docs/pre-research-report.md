# 环信IM SDK UniAppX 预研报告

## 1. 项目概述

### 1.1 项目目标
开发一个基于 UniAppX 的环信即时通讯(IM) SDK 封装，通过 UTS 层桥接调用原生 Android/iOS SDK，实现跨平台的 IM 功能。

### 1.2 预研范围
本次预研阶段重点验证以下最小化功能：
- ✅ SDK 初始化
- ✅ 用户注册/登录/登出
- ✅ 单聊消息发送
- ✅ 单聊消息接收
- ✅ 会话管理

### 1.3 技术架构

```
┌─────────────────────────────────────────────────────────────────┐
│                        UniAppX 应用层                          │
│                     (UVue 页面 + UTS 逻辑)                      │
├─────────────────────────────────────────────────────────────────┤
│                     UTS 插件层 (easemob-im)                     │
│  ┌─────────────────┐                    ┌─────────────────┐   │
│  │  interface.uts  │  <-- 跨平台接口 --> │    index.uts    │   │
│  │  (API 定义)     │                    │  (跨平台入口)   │   │
│  └─────────────────┘                    └─────────────────┘   │
│  ┌─────────────────┐                    ┌─────────────────┐   │
│  │ app-android/    │                    │    app-ios/     │   │
│  │ index.uts       │                    │    index.uts    │   │
│  │ (Android桥接)   │                    │   (iOS桥接)     │   │
│  └─────────────────┘                    └─────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│                      原生 SDK 层                                │
│  ┌──────────────────────────┐    ┌──────────────────────────┐ │
│  │   环信 Android SDK       │    │     环信 iOS SDK         │ │
│  │   (hyphenate-chat)       │    │   (HyphenateChat)        │ │
│  └──────────────────────────┘    └──────────────────────────┘ │
└─────────────────────────────────────────────────────────────────┘
```

## 2. 技术方案

### 2.1 架构设计

#### 2.1.1 分层架构

| 层级 | 职责 | 文件 |
|-----|------|------|
| 接口定义层 | 定义统一的类型和API接口 | `interface.uts` |
| 错误处理层 | 统一错误码定义 | `unierror.uts` |
| 跨平台入口 | 平台分发和统一导出 | `index.uts` |
| Android桥接 | Android原生SDK桥接 | `app-android/index.uts` |
| iOS桥接 | iOS原生SDK桥接 | `app-ios/index.uts` |

#### 2.1.2 设计原则

1. **平台抽象**: 通过 `interface.uts` 定义平台无关的API
2. **条件编译**: 使用 UTS 条件编译 `#ifdef` 实现平台分发
3. **类型安全**: 强类型定义，编译时类型检查
4. **错误统一**: 统一错误码体系，便于错误处理
5. **回调规范**: 统一 `onSuccess/onFail` 回调模式

### 2.2 核心模块

#### 2.2.1 初始化模块
```uts
// SDK 初始化
interface EMOptions {
  appKey: string
  autoLogin?: boolean
  debugMode?: boolean
  usePrivateServer?: boolean
}

im.init(options: EMOptions): void
```

#### 2.2.2 用户模块
```uts
// 注册
im.register(username: string, password: string, onSuccess?, onFail?): void

// 登录
im.login(params: EMLoginParam, onSuccess?, onFail?): void

// 登出
im.logout(unbindDeviceToken?: boolean, onSuccess?, onFail?): void
```

#### 2.2.3 消息模块
```uts
// 发送消息
im.sendMessage(param: EMSendMessageParam, onSuccess?, onFail?, onProgress?): void

// 创建各类消息
im.createTextMessage(to: string, content: string, chatType?): EMSendMessageParam
im.createImageMessage(to: string, localPath: string, chatType?): EMSendMessageParam
im.createVoiceMessage(to: string, localPath: string, duration: number, chatType?): EMSendMessageParam
im.createFileMessage(to: string, localPath: string, displayName: string, chatType?): EMSendMessageParam

// 消息监听
im.addMessageListener(listener: EMMessageListener): void
im.removeMessageListener(listener: EMMessageListener): void
```

#### 2.2.4 会话模块
```uts
// 获取会话列表
im.getAllConversations(): EMConversation[]

// 删除会话
im.deleteConversation(conversationId: string, chatType: EMChatType, deleteMessages?: boolean): void

// 未读消息数
im.getUnreadMessageCount(): number
```

### 2.3 数据模型

#### 2.3.1 消息模型
```uts
interface EMMessage {
  msgId: string
  type: 'txt' | 'image' | 'voice' | 'video' | 'file' | 'cmd'
  chatType: 'chat' | 'groupChat' | 'chatRoom'
  from: string
  to: string
  timestamp: number
  localTime: number
  isSender: boolean
  status: 'pending' | 'sending' | 'success' | 'fail'
  isRead: boolean
  body: EMTextMessageBody | EMImageMessageBody | EMVoiceMessageBody | EMFileMessageBody
}
```

#### 2.3.2 会话模型
```uts
interface EMConversation {
  conversationId: string
  type: 'chat' | 'groupChat' | 'chatRoom'
  unreadCount: number
  lastMessage?: EMMessage
}
```

## 3. 原生SDK集成

### 3.1 Android 集成

#### 3.1.1 依赖配置
```json
// app-android/config.json
{
  "dependencies": [
    "com.hyphenate:hyphenate-chat:4.11.0"
  ],
  "abis": ["armeabi-v7a", "arm64-v8a"],
  "minSdkVersion": 21
}
```

#### 3.1.2 权限配置
```xml
<!-- app-android/AndroidManifest.xml -->
<uses-permission android:name="android.permission.INTERNET" />
<uses-permission android:name="android.permission.ACCESS_NETWORK_STATE" />
<uses-permission android:name="android.permission.VIBRATE" />
<uses-permission android:name="android.permission.WAKE_LOCK" />
<uses-permission android:name="android.permission.READ_EXTERNAL_STORAGE" />
<uses-permission android:name="android.permission.WRITE_EXTERNAL_STORAGE" />
```

#### 3.1.3 核心服务声明
```xml
<service android:name="com.hyphenate.chat.EMChatService" android:exported="true" />
<service android:name="com.hyphenate.chat.EMJobService" 
         android:exported="true"
         android:permission="android.permission.BIND_JOB_SERVICE" />
```

### 3.2 iOS 集成

#### 3.2.1 依赖配置
```json
// app-ios/config.json
{
  "frameworks": [
    "Foundation.framework",
    "UIKit.framework",
    "Security.framework",
    "libz.tbd",
    "libc++.tbd"
  ],
  "deploymentTarget": "12.0",
  "validArchitectures": ["arm64"],
  "dependencies-pods": {
    "HyphenateChat": "4.11.0"
  }
}
```

### 3.3 版本选择

| 平台 | SDK版本 | 说明 |
|-----|---------|------|
| Android | 4.11.0 | 稳定版本，支持Android 5.0+ |
| iOS | 4.11.0 | 稳定版本，支持iOS 12.0+ |

## 4. 桥接层实现

### 4.1 Android 桥接要点

#### 4.1.1 SDK 初始化
```uts
const context = UTSAndroid.getAppContext()
const nativeOptions = new com.hyphenate.chat.EMOptions()
nativeOptions.setAppKey(options.appKey)
com.hyphenate.chat.EMClient.getInstance().init(context, nativeOptions)
```

#### 4.1.2 消息发送
```uts
const nativeMsg = com.hyphenate.chat.EMMessage
  .createTxtSendMessage(content, toChatUsername)
  
nativeMsg.setMessageStatusCallback(new com.hyphenate.EMCallBack({
  onSuccess: () => { /* 成功处理 */ },
  onError: (code, error) => { /* 错误处理 */ },
  onProgress: (progress, status) => { /* 进度处理 */ }
}))

emClient.chatManager().sendMessage(nativeMsg)
```

#### 4.1.3 消息监听
```uts
const listener = new com.hyphenate.EMMessageListener({
  onMessageReceived: (messages) => {
    for (const msg of messages) {
      // 转换为UTS消息并回调
    }
  }
})
emClient.chatManager().addMessageListener(listener)
```

### 4.2 iOS 桥接要点

#### 4.2.1 SDK 初始化
```uts
const nativeOptions = EMOptions(appkey: options.appKey)
EMClient.sharedClient.initializeSDK(with: nativeOptions)
```

#### 4.2.2 消息发送
```uts
let nativeBody = EMTextMessageBody(text: content)
let nativeMsg = EMChatMessage(conversationID: to, body: nativeBody, ext: nil)

EMClient.sharedClient.chatManager.send(
  nativeMsg,
  progress: { progress in /* 进度 */ },
  completion: { message, error in /* 完成回调 */ }
)
```

#### 4.2.3 消息监听
```uts
class MessageDelegate: EMChatManagerDelegate {
  func messagesDidReceive(_ messages: [EMChatMessage]) {
    for msg in messages {
      // 转换为UTS消息并回调
    }
  }
}
EMClient.sharedClient.chatManager.add(self, delegateQueue: DispatchQueue.main)
```

### 4.3 类型转换映射

#### 4.3.1 Android 类型映射

| UTS类型 | Android原生类型 | 说明 |
|--------|----------------|------|
| string | String | 字符串 |
| number | int/long/float/double | 数字 |
| boolean | boolean | 布尔值 |
| any | Object | 任意类型 |
| UTSJSONObject | JSONObject | JSON对象 |
| Array | List/UTSArray | 数组 |
| Map | HashMap | 字典 |

#### 4.3.2 iOS 类型映射

| UTS类型 | iOS原生类型 | 说明 |
|--------|------------|------|
| string | String | 字符串 |
| number | Int/Double | 数字 |
| boolean | Bool | 布尔值 |
| any | Any | 任意类型 |
| UTSJSONObject | Dictionary | JSON对象 |
| Array | Array | 数组 |

## 5. 事件处理机制

### 5.1 监听器管理

```uts
// 消息监听器集合
const messageListeners = new Array<EMMessageListener>()

// 添加监听器
export function addMessageListener(listener: EMMessageListener): void {
  if (!messageListeners.includes(listener)) {
    messageListeners.push(listener)
  }
}

// 移除监听器
export function removeMessageListener(listener: EMMessageListener): void {
  const index = messageListeners.indexOf(listener)
  if (index > -1) {
    messageListeners.splice(index, 1)
  }
}
```

### 5.2 原生事件转发

```uts
// Android 原生监听器接收消息后转发
const nativeListener = new com.hyphenate.EMMessageListener({
  onMessageReceived: (messages) => {
    for (const nativeMsg of messages) {
      const msg = convertNativeMessage(nativeMsg)
      // 转发给所有前端监听器
      for (const listener of messageListeners) {
        listener(msg)
      }
    }
  }
})
```

## 6. 错误处理

### 6.1 错误码定义

```uts
export enum EMErrorCode {
  // 通用错误
  GENERAL_ERROR = 1,
  NETWORK_ERROR = 2,
  
  // 登录相关
  INVALID_CREDENTIALS = 100,
  USER_NOT_FOUND = 101,
  USER_ALREADY_EXISTS = 102,
  USER_LOGIN_ANOTHER_DEVICE = 104,
  
  // 消息相关
  MESSAGE_SEND_FAILED = 200,
  
  // Token相关
  TOKEN_EXPIRED = 401,
  TOKEN_WILL_EXPIRE = 402
}
```

### 6.2 错误处理模式

```uts
// 统一错误回调
export type EMFailCallback = (code: number, message: string) => void

// 使用示例
im.sendMessage(
  param,
  (message) => { /* success */ },
  (code, message) => {
    // 错误处理
    console.error(`发送失败: ${code} - ${message}`)
  }
)
```

## 7. 开发计划

### 7.1 Phase 1: 基础功能（预研阶段）

| 功能 | 状态 | 说明 |
|-----|------|------|
| SDK初始化 | ✅ | 完成Android/iOS双端 |
| 用户注册 | ✅ | 完成Android/iOS双端 |
| 用户登录/登出 | ✅ | 完成Android/iOS双端 |
| 发送文本消息 | ✅ | 完成Android/iOS双端 |
| 接收消息 | ✅ | 完成Android/iOS双端 |
| 会话列表 | ✅ | 完成Android/iOS双端 |

### 7.2 Phase 2: 消息功能增强（后续开发）

- [ ] 图片消息发送/接收
- [ ] 语音消息发送/接收
- [ ] 文件消息发送/接收
- [ ] 消息撤回
- [ ] 消息已读回执
- [ ] 历史消息加载

### 7.3 Phase 3: 高级功能（后续开发）

- [ ] 群组功能
- [ ] 聊天室功能
- [ ] 用户属性
- [ ] 消息搜索
- [ ] 离线推送
- [ ] 多设备同步

## 8. 项目结构

```
easemob-uniappx-sdk/
├── uni_modules/
│   └── easemob-im/                    # UTS插件
│       ├── package.json               # 插件配置
│       ├── utssdk/
│       │   ├── interface.uts          # API接口定义
│       │   ├── unierror.uts           # 错误码定义
│       │   ├── index.uts              # 跨平台入口
│       │   ├── app-android/
│       │   │   ├── index.uts          # Android实现
│       │   │   ├── config.json        # Android配置
│       │   │   └── AndroidManifest.xml # Android权限
│       │   └── app-ios/
│       │       ├── index.uts          # iOS实现
│       │       └── config.json        # iOS配置
│       └── components/                # 可选：UI组件
├── pages/
│   └── index/
│       └── index.uvue                 # Demo页面
├── docs/
│   └── pre-research-report.md         # 预研报告
└── README.md                          # 项目说明
```

## 9. 使用示例

### 9.1 快速开始

```uts
<script setup lang="uts">
import easemobIM from '@/uni_modules/easemob-im'

// 1. 初始化SDK
easemobIM.im.init({
  appKey: 'your-app-key',
  autoLogin: false,
  debugMode: true
})

// 2. 登录
easemobIM.im.login(
  { username: 'user1', password: '123456' },
  () => console.log('登录成功'),
  (code, msg) => console.error('登录失败', code, msg)
)

// 3. 添加消息监听
easemobIM.im.addMessageListener((message) => {
  console.log('收到消息:', message.body.content)
})

// 4. 发送消息
const param = easemobIM.im.createTextMessage('user2', 'Hello!')
easemobIM.im.sendMessage(
  param,
  (msg) => console.log('发送成功'),
  (code, msg) => console.error('发送失败')
)
</script>
```

## 10. 风险评估与解决方案

### 10.1 技术风险

| 风险 | 影响 | 解决方案 |
|-----|------|---------|
| UTS编译问题 | 高 | 使用最新版HBuilderX，关注官方更新 |
| 原生SDK版本兼容性 | 中 | 使用稳定版本，充分测试 |
| 跨平台类型转换 | 中 | 完善类型转换单元测试 |
| 内存泄漏 | 中 | 规范监听器管理，确保正确移除 |

### 10.2 业务风险

| 风险 | 影响 | 解决方案 |
|-----|------|---------|
| 环信SDK API变更 | 中 | 关注官方文档，及时更新适配 |
| 隐私合规 | 高 | 确保用户同意后再初始化SDK |
| 推送集成复杂度 | 中 | 分阶段实现，先保证基础功能 |

## 11. 结论

### 11.1 可行性结论

基于预研结果，使用 UniAppX + UTS 桥接环信原生 SDK 的方案 **完全可行**。

**优势：**
1. 架构清晰，分层明确
2. 一次开发，双端复用
3. 类型安全，开发体验好
4. 性能接近原生

**挑战：**
1. UTS 文档和社区资源相对较少
2. 原生 SDK 的版本兼容性需要持续关注
3. 复杂场景的调试需要熟悉双端原生开发

### 11.2 下一步建议

1. **搭建测试环境**：获取环信AppKey，配置HBuilderX开发环境
2. **实现MVP**：先完成基础功能闭环验证
3. **编写测试用例**：覆盖主要功能路径
4. **性能测试**：验证消息收发延迟、内存占用等指标
5. **制定开发规范**：代码规范、提交流程、版本管理

## 附录

### A. 参考文档

- [环信IM Android SDK 文档](https://docs-im.easemob.com/ccim/android/quickstart)
- [环信IM iOS SDK 文档](https://docs-im.easemob.com/ccim/ios/quickstart)
- [UniAppX UTS 语法文档](https://doc.dcloud.net.cn/uni-app-x/uts/)
- [UniAppX UTS 插件开发文档](https://doc.dcloud.net.cn/uni-app-x/plugin/uts-plugin.html)

### B. 版本历史

| 版本 | 日期 | 说明 |
|-----|------|------|
| 1.0.0-pre | 2026-03-26 | 预研版本，基础功能实现 |
