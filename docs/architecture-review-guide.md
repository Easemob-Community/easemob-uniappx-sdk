# 环信 UTS SDK 架构评审指南

> **会议目标**：与 Android/iOS 原生端同事进行代码 Review 和架构可行性讨论
> **文档用途**：会前预习材料 + 会中讨论提纲

---

## 一、项目背景与目标

### 1.1 项目定位
- **产品名称**：easemob-uts-sdk（环信 UniAppX UTS 插件）
- **技术栈**：UniAppX + UTS（Unified TypeScript）
- **目标平台**：Android / iOS 双端
- **核心使命**：在 UniAppX 生态中提供环信 IM SDK 的跨平台封装

### 1.2 为什么要做 UTS 插件？
| 痛点 | 解决方案 |
|------|----------|
| 原生 SDK 无法直接在 uni-app 中使用 | 通过 UTS 层桥接原生 SDK |
| 需要同时维护两套原生代码 | 一套 UTS 代码，条件编译分发到双端 |
| 类型安全与开发体验 | UTS 提供 TypeScript 类型支持 |

---

## 二、整体架构概览

### 2.1 分层架构图

```
┌─────────────────────────────────────────────────────────────────┐
│                     UniAppX 应用层 (.uvue)                       │
│                   开发者直接调用的业务代码                        │
├─────────────────────────────────────────────────────────────────┤
│                     UTS 插件层 (easemob-uts-sdk)                 │
│  ┌─────────────────┐                    ┌─────────────────┐    │
│  │  interface.uts  │  <-- 跨平台接口 --> │    index.uts    │    │
│  │  (类型定义)      │                    │  (跨平台入口)    │    │
│  └─────────────────┘                    └─────────────────┘    │
│  ┌─────────────────────────────────────────────────────────┐   │
│  │              app-android/        app-ios/               │   │
│  │              (Android实现)       (iOS实现)              │   │
│  │  ┌─────────┐ ┌─────────┐ ┌─────────┐                   │   │
│  │  │  core/  │ │  auth/  │ │message/ │  ... 模块化目录   │   │
│  │  └─────────┘ └─────────┘ └─────────┘                   │   │
│  │  ┌─────────┐ ┌─────────┐                               │   │
│  │  │*.swift  │ │*.kt(辅助)│  原生桥接代码                │   │
│  │  └─────────┘ └─────────┘                               │   │
│  └─────────────────────────────────────────────────────────┘   │
├─────────────────────────────────────────────────────────────────┤
│                      原生 SDK 层                                │
│  ┌──────────────────────────┐    ┌──────────────────────────┐  │
│  │   环信 Android SDK       │    │     环信 iOS SDK         │  │
│  │   (hyphenate-chat)       │    │   (HyphenateChat)        │  │
│  └──────────────────────────┘    └──────────────────────────┘  │
└─────────────────────────────────────────────────────────────────┘
```

### 2.2 目录结构

```
utssdk/
├── interface.uts              # 跨平台类型定义（接口契约）
├── index.uts                  # 跨平台入口（条件编译导出）
├── unierror.uts               # 错误码定义
├── common/
│   └── event-constants.uts    # 事件常量定义（uni.$emit 事件名）
├── app-android/
│   ├── index.uts              # Android 平台入口（API 导出）
│   ├── core/init.uts          # SDK 初始化
│   ├── auth/login.uts         # 登录/登出
│   ├── message/
│   │   ├── listener.uts       # 消息监听
│   │   └── sender.uts         # 消息发送
│   ├── connection/listener.uts# 连接监听
│   ├── file/picker.uts        # 文件选择
│   ├── MessageHelper.kt       # Kotlin 辅助类
│   └── FilePickerHelper.kt    # 文件选择辅助类
└── app-ios/
    ├── index.uts              # iOS 平台入口
    ├── core/init.uts          # SDK 初始化
    ├── auth/login.uts         # 登录/登出
    ├── message/
    │   ├── listener.uts       # 消息监听
    │   ├── sender.uts         # 消息发送
    │   └── EMMessageBridge.swift  # Swift 桥接类
    ├── connection/listener.uts# 连接监听
    ├── file/picker.uts        # 文件选择
    ├── auth/EMAuthBridge.swift    # 认证桥接
    └── file/FilePickerHelper.swift# 文件选择辅助
```

---

## 三、双端实现差异对比（重点讨论）

### 3.1 架构模式对比

| 维度 | Android 端 | iOS 端 | 评审关注点 |
|------|-----------|--------|-----------|
| **桥接方式** | UTS 直接调用原生 SDK | UTS → Swift 桥接类 → SDK | iOS 为何需要中间桥接层？ |
| **回调机制** | 直接传递 Kotlin 回调 | 轮询模式（ResultMap） | 两种模式的优劣对比 |
| **辅助类** | Kotlin（MessageHelper.kt） | Swift（EMMessageBridge.swift） | 职责划分是否一致？ |
| **线程处理** | setTimeout 主线程调度 | 原生线程管理 | 是否需要统一？ |

### 3.2 消息发送实现差异

#### Android 端（直接调用）
```uts
// UTS 直接调用 Android SDK
const message = EMMessage.createTxtSendMessage(content, to);
message.setMessageStatusCallback(new EMCallBack({
  onSuccess: () => { /* 直接回调 */ },
  onError: (code, error) => { /* 直接回调 */ },
  onProgress: (progress, status) => { /* 直接回调 */ }
}));
EMClient.getInstance().chatManager().sendMessage(message);
```

#### iOS 端（桥接 + 轮询）
```uts
// UTS 调用 Swift 桥接类
EMMessageBridge.sendTextMessage(paramsJson: jsonString);

// 轮询获取结果
while (status == "pending") {
  status = EMMessageBridge.getResultStatus(callbackId);
  // 同时获取进度：EMMessageBridge.getResultProgress(callbackId)
}
```

**讨论问题**：
1. iOS 端为何采用轮询而非直接回调？（UTS 闭包传递限制）
2. 轮询模式对性能的影响？（当前轮询间隔 100ms）
3. 是否可以统一为同一种模式？

### 3.3 事件监听机制

| 方案 | 实现方式 | 适用场景 |
|------|----------|----------|
| **方案 A** | `addMessageListener(callbacks)` | UTS 层内部使用 |
| **方案 B** | `uni.$emit` + `uni.$on` | .uvue 页面层使用 |

**统一事件命名规范**：
```
em:connection:connected
em:connection:disconnected
em:connection:logout
em:message:received
em:message:cmd_received
em:message:read
...
```

**讨论问题**：
1. 双端事件分发是否完全一致？
2. 消息数据 JSON 序列化的必要性？（UTS 自定义类型传递会丢失字段）

---

## 四、关键技术决策（需评审确认）

### 4.1 决策 1：iOS 轮询架构

**背景**：iOS UTS 编译器无法将闭包作为参数传递给原生 Swift 代码，直接回调会导致崩溃。

**当前方案**：
- Swift 侧维护 `msgResultMap` 和 `msgProgressMap`
- UTS 侧轮询查询结果状态
- 回调 ID 作为关联键

**待讨论**：
- [ ] 轮询间隔是否合理？（当前 100ms）
- [ ] 内存泄漏风险？（已添加 clearResult 清理）
- [ ] 是否可以优化为通知机制？

### 4.2 决策 2：双端模块化目录

**当前结构**：
```
app-android/          app-ios/
├── core/             ├── core/
├── auth/             ├── auth/
├── message/          ├── message/
├── connection/       ├── connection/
└── file/             └── file/
```

**待讨论**：
- [ ] 目录结构是否清晰？
- [ ] 模块划分是否与原生 SDK 一致？
- [ ] 是否需要增加 utils/、constants/ 等公共目录？

### 4.3 决策 3：Kotlin/Swift 辅助类职责

**Android - MessageHelper.kt**：
- 解决 UTS 无法直接设置消息回调的问题
- 提供 `setMessageStatusCallback` 方法
- 处理消息扩展字段（ext）的 JSON 序列化

**iOS - EMMessageBridge.swift**：
- 封装所有消息发送操作
- 维护线程安全的结果存储（NSLock）
- 提供轮询查询接口

**待讨论**：
- [ ] 辅助类职责是否一致？
- [ ] 是否有代码重复可以抽取？
- [ ] 单元测试如何覆盖？

---

## 五、当前实现状态

### 5.1 已完成功能

| 模块 | Android | iOS | 备注 |
|------|---------|-----|------|
| SDK 初始化 | ✅ | ✅ | 双端完成 |
| 登录/登出 | ✅ | ✅ | 支持密码/Token 登录 |
| 连接监听 | ✅ | ✅ | 事件总线统一 |
| 文本消息发送 | ✅ | ✅ | 双端完成 |
| 图片消息发送 | ✅ | ✅ | 支持进度回调 |
| 视频消息发送 | ✅ | ✅ | 支持进度回调 |
| 文件消息发送 | ✅ | ✅ | 支持进度回调 |
| CMD 消息发送 | ✅ | ✅ | 双端完成 |
| 自定义消息 | ✅ | ✅ | 双端完成 |
| 消息监听 | ✅ | ✅ | JSON 序列化中转 |
| 文件选择器 | ✅ | ✅ | 原生文件选择 |

### 5.2 待实现功能

| 模块 | 优先级 | 依赖 |
|------|--------|------|
| 会话管理（Conversation） | 高 | 需要原生 SDK 支持 |
| 群组功能（Group） | 中 | 需要原生 SDK 支持 |
| 聊天室功能（ChatRoom） | 中 | 需要原生 SDK 支持 |
| 消息搜索 | 低 | 需要原生 SDK 支持 |
| 离线推送 | 低 | 需要配置推送证书 |

---

## 六、评审讨论提纲

### 6.1 架构层面

1. **整体架构合理性**
   - 分层是否清晰？
   - 模块划分是否符合原生 SDK 设计？
   - 扩展性如何？新增功能是否容易？

2. **双端一致性**
   - Android 直接调用 vs iOS 桥接轮询，是否可以统一？
   - 事件分发机制是否完全一致？
   - 类型定义是否双端对齐？

3. **性能考量**
   - iOS 轮询模式对电池/性能的影响？
   - 消息大数据量传输的优化方案？
   - 内存管理（监听器清理、缓存策略）

### 6.2 代码层面

1. **Android 端评审点**
   ```
   - MessageHelper.kt 的必要性？是否可以简化？
   - setTimeout 主线程调度的可靠性
   - 并发场景下的线程安全问题
   ```

2. **iOS 端评审点**
   ```
   - EMMessageBridge.swift 的设计模式
   - NSLock 锁的粒度是否合理？
   - 轮询间隔 100ms 是否可以调整？
   ```

3. **UTS 层评审点**
   ```
   - 类型定义是否完整？
   - 错误处理机制是否统一？
   - 事件常量命名是否规范？
   ```

### 6.3 协作流程

1. **新增功能的开发流程**
   ```
   1. interface.uts 定义接口（UTS 侧主导）
   2. Android 端实现（Android 同事）
   3. iOS 端实现（iOS 同事）
   4. 双端联调测试
   5. 文档更新
   ```

2. **代码审查规范**
   - 是否需要双端都 Review？
   - 测试用例的覆盖要求？
   - 文档同步机制？

3. **问题响应机制**
   - 线上问题如何定位？（日志、复现步骤）
   - 紧急修复的发布流程？

---

## 七、风险与建议

### 7.1 已知风险

| 风险 | 影响 | 缓解措施 |
|------|------|----------|
| UTS 编译器限制 | 高 | 及时跟进 DCloud 更新，使用 workaround |
| iOS 轮询性能 | 中 | 优化轮询间隔，考虑通知机制 |
| 双端实现差异 | 中 | 建立统一的开发规范和检查清单 |
| 原生 SDK 版本升级 | 中 | 制定版本升级流程，充分回归测试 |

### 7.2 优化建议

1. **短期优化**
   - [ ] 统一双端回调模式（评估可行性）
   - [ ] 完善错误码体系
   - [ ] 增加日志输出规范

2. **中期优化**
   - [ ] 建立自动化测试覆盖
   - [ ] 性能基准测试
   - [ ] 文档自动生成

3. **长期优化**
   - [ ] 考虑代码生成工具（从原生 SDK 生成 UTS 接口）
   - [ ] 插件市场发布
   - [ ] 社区生态建设

---

## 九、架构演进规划（参考即构 ZIM SDK）

### 9.1 当前架构痛点

基于实际开发体验和与即构 ZIM SDK 的对比分析，当前架构存在以下问题：

| 问题 | 当前实现 | 痛点描述 |
|------|----------|----------|
| **初始化方式** | `initSDK(config)` 函数式 | 无返回值，无法链式调用 |
| **监听挂载** | `uni.$on('em_connected', ...)` | 需在 App.uvue 中提前挂载，与 SDK 生命周期解耦 |
| **消息发送** | 回调函数式 `sendTextMessage(..., { onSuccess, onError })` | 无法使用 async/await，代码嵌套深 |
| **iOS 轮询** | `setInterval` 轮询查询结果 | 性能开销，代码复杂 |

### 9.2 参考方案：即构 ZIM SDK 设计

**即构的优雅设计：**

```typescript
// 1. 单例初始化
const zim = ZIM.create({ appID: 123, appSign: 'xxx' });
// 或获取已有实例
const zim = ZIM.getInstance();

// 2. 实例方法挂载监听（与 SDK 生命周期绑定）
zim.onConnectionStateChanged((data) => {
  console.log('连接状态变更', data);
});

zim.onPeerMessageReceived((data) => {
  console.log('收到消息', data);
});

// 3. Promise 化 API
await zim.login('userID', { userName: 'xxx', token: '' });

await zim.sendMessage(message, 'toUserID', 0, config, {
  onMessageAttached: (msg) => console.log('消息已附加'),
  onMessageUploadingProgress: (msg, current, total) => console.log('上传进度', current/total)
});
```

**即构架构优势：**
- **单例模式**：`create()` / `getInstance()` 管理实例生命周期
- **实例监听**：监听与实例绑定，无需全局事件总线
- **Promise 化**：`async/await` 支持，代码更简洁
- **原生回调**：iOS 端直接使用 SDK 委托回调，无需轮询

### 9.3 改造方案（渐进式）

#### 阶段 1：新增 EMClient 类（保持兼容）

```typescript
// utssdk/app-ios/EMClient.uts
export class EMClient {
  private static _instance: EMClient | null = null;
  private _eventHandler: EMEventHandlerImpl;
  
  static create(config: EMInitConfig): EMClient {
    if (this._instance == null) {
      initEMClient(config.appKey);
      this._instance = new EMClient();
    }
    return this._instance;
  }
  
  static getInstance(): EMClient {
    return this._instance!;
  }
  
  // 实例方法监听（替代 uni.$on）
  @UTSJS.keepAlive
  onConnected(callback: (() => void) | null): void {
    this._eventHandler.eventMap.onConnected = callback;
  }
  
  @UTSJS.keepAlive
  onMessageReceived(callback: ((messages: Message[]) => void) | null): void {
    this._eventHandler.eventMap.onMessageReceived = callback;
  }
  
  // Promise 化登录（评估可行性）
  login(userId: string, password: string): Promise<void> {
    return new Promise((resolve, reject) => {
      loginEMClient(userId, password, 
        () => resolve(), 
        (code, msg) => reject(new UniError('em-login-error', code, msg))
      );
    });
  }
}
```

#### 阶段 2：改造 iOS Bridge（长期规划）

**目标**：用原生委托回调替代轮询

```swift
// EMMessageBridge.swift 新增委托实现
class EMMessageBridge: NSObject, EMMessageManagerDelegate {
    func messageDidSend(_ aMessage: EMMessage?, error: EMError?) {
        // 直接回调到 UTS 层，无需轮询
    }
    
    func messageUploadProgressUpdate(_ aMessage: EMMessage?, progress: Float) {
        // 实时进度回调
    }
}
```

**阻碍因素：**
- 环信 iOS SDK (`HyphenateChat`) 的回调机制需要评估
- 需要改造 Swift Bridge 层，工作量较大

### 9.4 可行性评估

| 改造项 | 可行性 | 工作量 | 优先级 |
|--------|--------|--------|--------|
| 单例初始化 (`EMClient.create`) | ✅ 高 | 2小时 | P0 |
| 实例方法监听 (`client.onXxx`) | ✅ 高 | 4小时 | P0 |
| Promise 化登录/登出 | ✅ 中 | 2小时 | P1 |
| Promise 化消息发送 | ⚠️ 低 | 1-2天 | P2（需改造 Swift Bridge）|
| 原生回调替代轮询 | ⚠️ 低 | 2-3天 | P2（依赖 SDK 支持）|

### 9.5 实施计划

**当前分支**：`feat/emclient-singleton`

```
Week 1: EMClient 单例 + 实例监听 ✅ 已完成
  - [x] 创建 EMClient.uts（iOS + Android 双平台）
  - [x] 实现 create/getInstance 单例模式
  - [x] 实现 onConnected/onDisconnected/onMessageReceived 等实例监听
  - [x] 保持旧 API 兼容（index.uts 同时导出新旧 API）
  - [ ] 编写迁移示例

Week 2: Promise 化（可选）
  - [ ] 评估 login/logout Promise 化
  - [ ] 评估消息发送 Promise 化可行性

Week 3+: 原生回调改造（长期）
  - [ ] 调研环信 iOS SDK 委托回调支持
  - [ ] 设计 UTS ↔ Swift 回调桥接方案
  - [ ] 替代轮询机制
```

**已完成文件**：
- `utssdk/app-ios/EMClient.uts` - iOS 平台单例实现
- `utssdk/app-android/EMClient.uts` - Android 平台单例实现

### 9.6 参考对比

| 维度 | 当前环信 UTS SDK | 即构 ZIM UTS SDK |
|------|------------------|------------------|
| **初始化** | `initSDK(config)` 函数 | `ZIM.create(config)` 单例 |
| **监听挂载** | `uni.$on('em_xxx', ...)` 全局 | `zim.onXxx(callback)` 实例 |
| **消息发送** | 回调函数式 | Promise + 回调混合 |
| **iOS 回调** | 轮询模式 | 原生委托回调 |
| **类型定义** | 简单 UTSJSONObject | 完整 TypeScript 接口 |

---

> **文档版本**：v1.2  
> **更新日期**：2026-04-13  
> **更新内容**：
> - 新增架构演进规划章节，参考即构 ZIM SDK 设计
> - 完成 EMClient 单例类双平台实现（iOS + Android）

---

## 八、附录

### 8.1 关键文件索引

| 文件 | 职责 | 评审重点 |
|------|------|----------|
| `utssdk/interface.uts` | 类型定义 | 接口设计是否合理 |
| `utssdk/app-android/index.uts` | Android 入口 | API 导出完整性 |
| `utssdk/app-ios/index.uts` | iOS 入口 | 与 Android 一致性 |
| `utssdk/app-android/EMClient.uts` | Android 单例类 | 新架构实现 |
| `utssdk/app-ios/EMClient.uts` | iOS 单例类 | 新架构实现 |
| `utssdk/app-android/message/sender.uts` | Android 消息发送 | 回调处理逻辑 |
| `utssdk/app-ios/message/EMMessageBridge.swift` | iOS 消息桥接 | 轮询实现细节 |
| `utssdk/common/event-constants.uts` | 事件常量 | 命名规范 |

### 8.2 参考文档

- [UTS 插件开发官方文档](https://doc.dcloud.net.cn/uni-app-x/plugin/uts-plugin.html)
- [环信 Android SDK API](https://doc.easemob.com/android_product_overview.html)
- [环信 iOS SDK API](https://doc.easemob.com/ios_product_overview.html)
- [development-guide.md](./development-guide.md) - 详细开发指南
- [pre-research-report.md](./pre-research-report.md) - 预研报告

### 8.3 会议记录模板

```markdown
## 评审会议记录 - YYYY/MM/DD

### 参会人员
- Android：xxx
- iOS：xxx
- UTS：xxx

### 讨论议题
1. xxx
2. xxx

### 决议事项
| 事项 | 负责人 | 截止日期 |
|------|--------|----------|
| xxx | xxx | xxx |

### 待跟进问题
- [ ] xxx
```

---

> **文档版本**：v1.0  
> **创建日期**：2026-04-13  
> **维护者**：UTS SDK 开发团队
