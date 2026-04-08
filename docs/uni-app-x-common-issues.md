# Uni-App X 常见问题与解决方案

## 一、uni.chooseImage API 使用问题

### 问题1：基座未包含 API

**错误信息：**
```
error: 当前运行的基座未包含api uni.chooseImage，请重新打包自定义基座再运行。
```

**原因：**
uni-app x 的 Android 端使用模块化架构，相机和相册功能需要在 `manifest.json` 中显式声明模块依赖。

**解决方案：**

在 `manifest.json` 的 `app-android.distribute.modules` 中添加 Camera 和 Gallery 模块：

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

**操作步骤：**
1. 修改 `manifest.json` 添加模块配置
2. 重新制作自定义调试基座（HBuilderX → 运行 → 制作自定义调试基座）
3. 使用自定义基座运行项目

---

### 问题2：类型定义找不到

**错误信息：**
```
error: 找不到名称"ChooseImageSuccessCallbackResult"
```

**原因：**
uni-app x 的 UTS 编译器对某些 API 的类型定义支持不完善，无法直接使用类型注解。

**解决方案：**

移除显式类型注解，让编译器自动推断：

```typescript
// ❌ 错误写法
uni.chooseImage({
  success: (res: ChooseImageSuccessCallbackResult) => {
    const path = res.tempFilePaths[0]
  }
})

// ✅ 正确写法
uni.chooseImage({
  success: (res) => {
    const paths = res.tempFilePaths
    if (paths != null && paths.length > 0) {
      const imagePath = paths[0]!
    }
  }
})
```

**注意事项：**
- 使用 `!` 非空断言操作符确保类型安全
- 先赋值给中间变量再访问属性，避免编译器报错

---

## 二、UTS 调用 Java 方法问题

### 问题：无法调用 synchronized 方法

**错误信息：**
```
error: 找不到名称"setMessageStatusCallback"
```

**原因：**
UTS 编译器不支持直接调用 Java 的 `synchronized` 方法。环信 SDK 中的 `EMMessage.setMessageStatusCallback()` 是 synchronized 方法。

**解决方案：**

使用 **UTS 原生混编技术**，创建 Kotlin 辅助类：

**1. 创建 Kotlin 辅助类**

文件：`uni_modules/easemob-uts-sdk/utssdk/app-android/MessageHelper.kt`

```kotlin
package uts.sdk.modules.easemobUtsSdk

import com.hyphenate.chat.EMMessage
import com.hyphenate.EMCallBack
import android.util.Log

object MessageHelper {
    private const val TAG = "MessageHelper"

    @JvmStatic
    fun setMessageStatusCallback(message: EMMessage, callback: EMCallBack) {
        Log.d(TAG, "setMessageStatusCallback 被调用")
        message.setMessageStatusCallback(callback)
    }
}
```

**2. 在 UTS 中导入使用**

```typescript
import { MessageHelper } from 'uts.sdk.modules.easemobUtsSdk'

function setMessageCallback(message: EMMessage, callback: EMCallBack): void {
    MessageHelper.setMessageStatusCallback(message, callback)
}
```

**关键点：**
- Kotlin 文件放在 `utssdk/app-android/` 目录下
- 使用 `@JvmStatic` 注解使方法可被静态调用
- 包名格式：`uts.sdk.modules.{moduleName}`

---

## 三、消息发送 API 设计规范

### 聊天类型参数设计

**问题：**
发送消息时需要区分单聊、群组、聊天室，但环信 SDK 默认创建的消息是单聊类型。

**解决方案：**

创建辅助函数统一设置聊天类型：

```typescript
function setEMMessageChatType(message: EMMessage, chatType: string): void {
    if (chatType === 'group') {
        message.setChatType(EMMessageChatType.GroupChat)
    } else if (chatType === 'chatroom') {
        message.setChatType(EMMessageChatType.ChatRoom)
    } else {
        message.setChatType(EMMessageChatType.Chat)
    }
}
```

**API 调用示例：**

```typescript
// 发送文本消息
sendTextMessage(content, to, 'group', callback)

// 发送图片消息
sendImageMessage(filePath, false, to, 'single', callback)
```

---

## 四、UTS 类型兼容性注意事项

### 1. 数组访问问题

```typescript
// ❌ 可能报错
const path = res.tempFilePaths[0]

// ✅ 安全写法
const paths = res.tempFilePaths
if (paths != null && paths.length > 0) {
    const path = paths[0]!
}
```

### 2. 回调函数类型

```typescript
// ❌ 显式类型可能报错
fail: (err: any) => { }

// ✅ 省略类型注解
fail: (err) => { }
```

### 3. 联合类型限制

UTS 不支持 TypeScript 的联合类型（`string | number`），需要重构为可选字段的单一类型。

---

## 五、UTS-Kotlin 互操作：动态对象访问

### 问题：无法访问 Kotlin 返回的对象属性

**错误信息：**
```
error: 找不到名称"msgId"。参考: https://doc.dcloud.net.cn/uni-app-x/uts/compiler-known-issues.html#error18
error: Unresolved reference. None of the following candidates is applicable because of a receiver type mismatch:
fun String.get(index: Number): Char
```

**原因：**
1. UTS 的 `any` 类型在编译时可能被推断为 `String` 类型，导致方括号访问被解析为 `String.get(index)`
2. Kotlin data class 的属性不会自动映射为 UTS 可访问的字段
3. UTS 不支持直接访问跨语言传递的动态对象属性

**解决方案：**

在 UTS 层构造 `UTSJSONObject` 对象，而不是依赖 Kotlin 层返回的对象。

**UTS 层（message.uts）：**
```typescript
override onSuccess(): void {
    const callback = messageCallbacks.get(this.callbackId);
    if (callback != null) {
        const onSuccess = callback["onSuccess"];
        const msg = this.message;
        if (onSuccess != null && msg != null) {
            // 构造 UTSJSONObject 对象
            const messageInfo = new UTSJSONObject();
            messageInfo['msgId'] = msg.getMsgId();
            messageInfo['from'] = msg.getFrom();
            messageInfo['to'] = msg.getTo();
            messageInfo['type'] = msg.getType().name;
            const chatType = msg.getChatType();
            if (chatType == EMMessageChatType.GroupChat) {
                messageInfo['chatType'] = 'group';
            } else if (chatType == EMMessageChatType.ChatRoom) {
                messageInfo['chatType'] = 'chatroom';
            } else {
                messageInfo['chatType'] = 'single';
            }
            messageInfo['timestamp'] = msg.getMsgTime();
            (onSuccess as (messageInfo: UTSJSONObject) => void)(messageInfo);
        }
        messageCallbacks.delete(this.callbackId);
    }
}
```

**前端（message.uvue）：**
```typescript
onSuccess: (messageInfo: UTSJSONObject) => {
    const msgId = messageInfo.getString('msgId') ?? 'unknown'
    const msgType = messageInfo.getString('type') ?? 'unknown'
    const chatTypeStr = messageInfo.getString('chatType') ?? 'unknown'
    const timestamp = messageInfo.getNumber('timestamp') ?? 0
    console.log('[MessagePage] 消息ID:', msgId)
    console.log('[MessagePage] 消息类型:', msgType)
}
```

**最佳实践：**
1. **UTS 层构造对象**：在 UTS 层使用 `new UTSJSONObject()` 构造对象，设置属性时使用方括号语法 `obj['key'] = value`
2. **前端使用 UTSJSONObject 类型**：回调参数类型声明为 `UTSJSONObject`，使用 `getString()` / `getNumber()` 等方法访问属性
3. **避免跨语言传递复杂对象**：不要将 Kotlin data class 直接传递给 UTS，而是在 UTS 层转换为 UTSJSONObject
4. **使用 ?? 提供默认值**：使用空值合并运算符 `??` 提供默认值，避免 null 或 undefined 问题

---

## 六、调试技巧

### 查看 Kotlin 混编日志

在 Android Studio Logcat 中过滤：

```
MessageHelper | EMMessage | MessagePage
```

### 验证消息发送流程

| 层级 | 日志标签 | 说明 |
|------|----------|------|
| Kotlin 层 | `MessageHelper` | 验证混编类是否工作 |
| UTS 层 | `EMMessage` | 验证消息回调 |
| 页面层 | `MessagePage` | 验证页面逻辑 |

---

## 七、相关文件位置

| 功能 | 文件路径 |
|------|----------|
| 消息发送页面 | `pages/message/message.uvue` |
| 消息发送实现 | `uni_modules/easemob-uts-sdk/utssdk/app-android/apis/modules/message.uts` |
| Kotlin 辅助类 | `uni_modules/easemob-uts-sdk/utssdk/app-android/MessageHelper.kt` |
| 公共 API 导出 | `uni_modules/easemob-uts-sdk/utssdk/app-android/index.uts` |
| 模块配置 | `manifest.json` |

---

## 八、iOS 轮询架构设计说明

### 为什么 iOS 采用轮询而非直接回调？

**根本原因：UTS/Swift 混编无法安全传递 `@escaping` 闭包**

在 iOS 平台，UTS 插件调用 Swift `@objc` 方法时存在根本性限制：

1. **闭包传递崩溃**：UTS 无法将 `@escaping` 闭包（如 `onSuccess`/`onError`/`onProgress`）安全传递给 Swift 方法。尝试传递会导致运行时崩溃，错误通常发生在参数求值阶段，早于函数体执行。

2. **UTSJSONObject 含闭包崩溃**：在 UTS 层构造 `{ onSuccess: () => {} }` 这样的对象字面量，iOS 运行时在解析该对象时就会崩溃。

3. **跨语言类型不匹配**：Swift 的闭包类型与 UTS 的函数类型在内存布局和调用约定上不兼容，无法直接桥接。

**解决方案：轮询架构（Poll-based Architecture）**

```
┌─────────────┐     JSON参数      ┌──────────────────┐
│  UTS Layer  │ ───────────────> │  Swift Bridge    │
│  (sender)   │                  │  (EMMessageBridge)│
└─────────────┘                  └──────────────────┘
       ↑                                  │
       │         轮询查询结果              │ 环信SDK异步回调
       │    getResultStatus(callbackId)   │
       └──────────────────────────────────┘
```

**工作流程：**

1. **调用阶段**：UTS 生成唯一 `callbackId`，将参数序列化为 JSON 字符串传入 Swift
2. **执行阶段**：Swift 层调用环信 SDK，在异步回调中将结果存入线程安全的 `msgResultMap[callbackId]`
3. **轮询阶段**：UTS 使用 `setInterval` 每 50ms 查询 `getResultStatus(callbackId)`
4. **回调阶段**：检测到 `success`/`error` 后，UTS 从 Swift 读取详细结果，触发 `onSuccess`/`onError`

**架构优势：**

| 方面 | 说明 |
|------|------|
| 零闭包传递 | 完全规避 UTS→Swift 闭包传递的崩溃风险 |
| 类型安全 | 所有跨语言交互使用基本类型（String, Int） |
| 可扩展 | 同一模式可复制到所有带回调的 SDK 方法 |
| 可维护 | Swift 层纯异步，UTS 层纯轮询，职责清晰 |

**相关文件：**
- `app-ios/message/EMMessageBridge.swift` - Swift 桥接层，结果存储
- `app-ios/message/sender.uts` - UTS 轮询实现

---

## 九、iOS 附件上传进度回调问题

### 问题1：onProgress 回调不触发或崩溃

**现象：**
- 发送图片/视频时 onProgress 回调不触发
- 或触发一次后报错：`onProgress回调函数已释放，不能再次执行`
- 参考文档：https://doc.dcloud.net.cn/uni-app-x/plugin/uts-plugin.html#keepalive

**原因：**
1. UTS 插件 iOS 运行时默认在导出函数返回后释放回调参数，只允许异步调用一次
2. `onSuccess`/`onError` 只调用一次所以能工作，`onProgress` 需多次调用故报错
3. `app-ios/index.uts` 中未将 `onProgress` 显式赋值到 callback 对象

**解决方案：**

**1. 添加 @UTSJS.keepAlive 装饰器**

在 `app-ios/index.uts` 中：

```typescript
@UTSJS.keepAlive
export function sendImageMessage(
  filePath: string,
  sendOriginalImage: boolean,
  to: string,
  chatType: string,
  onSuccess: ((messageInfo: UTSJSONObject) => void) | null,
  onError: ((code: number, message: string) => void) | null,
  onProgress: ((progress: number, status: string) => void) | null = null,
  ext: UTSJSONObject | null = null
): void {
  const callback = new UTSJSONObject();
  callback['onSuccess'] = onSuccess;
  callback['onError'] = onError;
  callback['onProgress'] = onProgress;  // 必须显式赋值
  sendImageMessageImpl(filePath, sendOriginalImage, to, chatType, callback, ext);
}
```

**2. Swift Bridge 进度存储**

在 `EMMessageBridge.swift` 中使用线程安全的 `msgProgressMap`：

```swift
private let msgProgressLock = NSLock()
private var msgProgressMap: [String: Int] = [:]

// 发送时注入 progress block
EMClient.shared().chatManager?.send(message, progress: { progress in
    setMsgProgress(Int(progress), for: callbackId)
}) { sentMessage, error in
    // 注意：不在此处 removeMsgProgress，由 UTS 层 clearResult 统一清理
    // 避免竞态：progress 被提前删除导致 UTS 轮询读不到最终进度
}
```

**3. UTS 层轮询与去重**

在 `sender.uts` 中：

```typescript
function pollMessageResultWithProgress(
  callbackId: string,
  onSuccess: ((messageInfo: UTSJSONObject) => void) | null,
  onError: ((code: number, message: string) => void) | null,
  onProgress: ((progress: number, status: string) => void) | null
): void {
  let elapsed = 0;
  let timer = 0;
  let lastProgress = -1;
  
  timer = setInterval((): void => {
    elapsed += MSG_POLL_INTERVAL;
    const status = EMMessageBridge.getResultStatus(callbackId);
    
    if (status == 'success') {
      clearInterval(timer);
      if (onProgress != null) { onProgress!(100, 'complete'); }  // 完成时上报100%
      const info = buildSuccessMessageInfo(callbackId);
      if (onSuccess != null) { onSuccess!(info); }
      EMMessageBridge.clearResult(callbackId);  // 统一清理
    } else if (onProgress != null) {
      const pRaw = EMMessageBridge.getResultProgress(callbackId);
      if (pRaw >= 0 && pRaw != lastProgress) {  // 去重
        lastProgress = pRaw;
        onProgress!(pRaw, 'uploading');
      }
    }
  }, MSG_POLL_INTERVAL);
}
```

---

### 问题2：进度值始终为 0 或只有 0→100

**现象：**
- 相册选择的小图片只有 `progress=0` 然后直接 `progress=100`
- 拍照的大图片能看到 `0→40→61→81→100` 的完整进度

**原因：**
这是**正常的 SDK 行为**，不是 bug：
- 进度回调只追踪**网络上传**阶段
- 相册照片通常已被系统压缩，文件很小，网络上传在 1-2 个轮询周期内完成
- 拍照的原始高分辨率照片文件较大，上传需要数秒，SDK 有足够时间报告中间进度

**预期行为：**
| 场景 | 文件大小 | 进度表现 |
|------|----------|----------|
| 拍照 | 大（几 MB） | 0→40→61→81→100（渐进） |
| 相册 | 小（几百 KB） | 0→100（瞬间完成） |

**建议：**
- 接受真实进度行为，大文件自然有中间值，小文件秒传直接 100%
- 如需模拟进度效果，可在 UI 层做动画过渡，但需明确区分真实进度和模拟进度

---

## 九、参考链接

- [UTS 编译器已知问题](https://doc.dcloud.net.cn/uni-app-x/uts/compiler-known-issues.html)
- [uni.chooseImage 文档](https://doc.dcloud.net.cn/uni-app-x/api/media/image/chooseImage.html)
- [UTS 原生混编文档](https://doc.dcloud.net.cn/uni-app-x/uts/native-code.html)
- [UTS 插件 keepAlive 文档](https://doc.dcloud.net.cn/uni-app-x/plugin/uts-plugin.html#keepalive)
