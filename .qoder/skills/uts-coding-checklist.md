# UTS SDK 编码检查清单

## Description
在为 easemob-uniappx-sdk 项目编写或修改 UTS 代码前，必须逐项检查以下已知限制和规范。每一条都来自真实的编译/运行时错误教训。

## Trigger
当修改 `utssdk/` 目录下任何 `.uts` 文件时，或新增导出函数/接口时，自动触发此清单检查。

---

## 一、跨平台同步原则（最高优先级）

> **核心原则：修改一个平台时，必须检查另一个平台是否需要相同处理。**

- [ ] 修改 `app-ios/` 下的文件后，检查 `app-android/` 是否有对应文件需要同步修改
- [ ] 修改 `app-android/` 下的文件后，检查 `app-ios/` 是否有对应文件需要同步修改
- [ ] 新增公共 API 时，两个平台的函数签名必须完全一致
- [ ] `utssdk/index.uts` 的条件编译导出必须覆盖双平台

---

## 二、函数命名与导出规范

### 2.1 Impl 后缀规范（iOS + Android 通用）
- [ ] 子模块函数必须以 `Impl` 后缀命名（如 `openFilePickerImpl`）
- [ ] `index.uts` 公共 API 不带 `Impl` 后缀（如 `openFilePicker`）
- [ ] 导入别名与导出函数名不得相同，否则导致递归调用或 `Function invocation 'xxx' expected`

### 2.2 uts-proxy 导出限制
- [ ] 平台 `index.uts` 中只有 `export function xxx()` 形式才被 uts-proxy 识别
- [ ] `export { xxx } from './xxx.uts'` 的 re-export 语法 **不被 uts-proxy 解析**（仅对类型/class/常量可用）
- [ ] `export type` 必须直接声明，不能从其他文件 re-export
- [ ] 不支持 `export { xxx as yyy }` 别名导出语法

### 2.3 iOS 特殊限制
- [ ] iOS 编译器将整个 UTS 插件视为单一 Swift 模块，不同 `.uts` 文件中不得有同名函数
- [ ] 若子模块函数名与 `index.uts` 导出函数同名 → `invalid redeclaration` 编译错误

### 2.4 Android 特殊限制
- [ ] Android 编译器遇到同名函数 → `Function invocation 'openFilePicker(...)' expected`
- [ ] 不能依赖 `import alias` 来隔离同名函数，必须在源头重命名

---

## 三、iOS 回调与闭包限制

### 3.1 UTSJSONObject 闭包崩溃（致命）
- [ ] **禁止** 在调用方构造含闭包的 UTSJSONObject 字面量（如 `{onSuccess: () => {...}} as UTSJSONObject`）
- [ ] 公共 API 必须接收独立函数参数，函数体内用 `new UTSJSONObject()` + 属性赋值重组
- [ ] setInterval 闭包内 **禁止** 访问 UTSJSONObject 中存储的闭包

### 3.2 回调生命周期
- [ ] 异步/多次回调的导出函数必须加 `@UTSJS.keepAlive` 装饰器
- [ ] 不加此装饰器的回调只能触发一次，之后会被释放

### 3.3 Swift 混编限制
- [ ] UTS **禁止** 调用含 `@escaping` 闭包参数的 Swift 方法（运行时崩溃）
- [ ] UTS **无法** 直接继承 Swift 的 `@objc` 协议，必须通过 NSObject 工厂类桥接
- [ ] 跨语言异步通信推荐使用 **轮询模式**（Swift 侧存结果到线程安全 Map，UTS 侧 setInterval 轮询）

---

## 四、类型系统限制

- [ ] UTS 不支持泛型语法
- [ ] 函数返回类型禁用内联对象字面量，必须定义 class 或使用 UTSJSONObject
- [ ] `type` 关键字定义的类型不能与 `const` 常量同名
- [ ] setInterval 的回调引用变量必须用 `let` 声明（非 `const`），且可变函数变量用 `?.invoke()` 调用

---

## 五、导出语法使用规范

### 5.1 直接重新导出（re-export）的适用场景

| 场景 | 处理方式 | 示例 |
|------|----------|------|
| 需要添加额外逻辑 | 正常导入导出，定义包装函数 | `loginSDK` 包装 `loginEMClient` |
| 直接透传（无需包装） | 使用直接重新导出语法 | `export { getCurrentUser } from './module'` |

### 5.2 命名冲突规避

**错误写法（导致 `Function invocation 'xxx()' expected`）：**
```typescript
// 子模块
export function getCurrentUser(): string { ... }

// index.uts - ❌ 错误
import { getCurrentUser as getCurrentUserImpl } from './module.uts'
export function getCurrentUser(): string {
  return getCurrentUserImpl()  // 编译报错！
}
```

**正确写法：**
```typescript
// 不需要包装逻辑的函数：直接重新导出
export { getCurrentUser, isLoggedIn } from './module.uts'

// 需要包装逻辑的函数：使用 Impl 后缀
import { loginEMClient } from './module.uts'
export function loginSDK(...) { ... }
```

---

## 六、iOS 轮询架构设计规范

### 6.1 为什么必须采用轮询模式

**根本原因：UTS/Swift 混编无法安全传递 `@escaping` 闭包**

1. **闭包传递崩溃**：UTS 无法将 `@escaping` 闭包（如 `onSuccess`/`onError`/`onProgress`）安全传递给 Swift 方法
2. **UTSJSONObject 含闭包崩溃**：构造 `{ onSuccess: () => {} }` 这样的对象字面量，iOS 运行时解析即崩溃
3. **跨语言类型不匹配**：Swift 闭包类型与 UTS 函数类型在内存布局上不兼容

### 6.2 轮询架构工作流程

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

**实现步骤：**
1. UTS 生成唯一 `callbackId`，参数序列化为 JSON 传入 Swift
2. Swift 调用环信 SDK，异步回调中将结果存入线程安全的 `msgResultMap[callbackId]`
3. UTS 使用 `setInterval` 每 50ms 查询 `getResultStatus(callbackId)`
4. 检测到 `success`/`error` 后，UTS 从 Swift 读取详细结果，触发回调

### 6.3 进度回调特殊处理

**Swift 层进度存储：**
```swift
private let msgProgressLock = NSLock()
private var msgProgressMap: [String: Int] = [:]

EMClient.shared().chatManager?.send(message, progress: { progress in
    setMsgProgress(Int(progress), for: callbackId)
}) { sentMessage, error in
    // 注意：不在此处 removeMsgProgress，由 UTS 层 clearResult 统一清理
}
```

**UTS 层轮询与去重：**
```typescript
let lastProgress = -1;
timer = setInterval((): void => {
  const pRaw = EMMessageBridge.getResultProgress(callbackId);
  if (pRaw >= 0 && pRaw != lastProgress) {
    lastProgress = pRaw;
    onProgress!(pRaw, 'uploading');
  }
}, MSG_POLL_INTERVAL);
```

---

## 七、项目目录结构规范

### 7.1 文件职责

| 文件 | 职责 | 修改频率 |
|-----|------|---------|
| `interface.uts` | 定义对外 API 接口、类型 | 低（架构变更时） |
| `index.uts` | 跨平台入口，条件编译分发 | 低 |
| `app-android/index.uts` | Android 平台入口，导出所有公开API | 中 |
| `app-ios/index.uts` | iOS 平台入口，导出所有公开API | 中 |
| `message/sender.uts` | 消息发送实现 | 中 |
| `message/EMMessageBridge.swift` | iOS Swift 桥接层 | 中 |

### 7.2 新增功能开发流程

1. 如需新增接口，先在 `interface.uts` 中定义（如适用）
2. 在 `app-android/index.uts` 实现 Android 端
3. 在 `app-ios/index.uts` 实现 iOS 端
4. 在 `index.uts` 中添加跨平台分发
5. **必须**：双端同步检查，确保 API 签名一致

---

## 八、使用模板

### 新增公共 API 的标准模式：

```typescript
// ===== 子模块 file/picker.uts =====
export function openFilePickerImpl(callback: UTSJSONObject): void {
  // 实际实现...
}

// ===== index.uts (iOS) =====
import { openFilePickerImpl } from './file/picker.uts';

@UTSJS.keepAlive
export function openFilePicker(
  onSuccess: ((result: SomeResult) => void) | null,
  onError: ((code: number, message: string) => void) | null
): void {
  const callback = new UTSJSONObject();
  callback['onSuccess'] = onSuccess;
  callback['onError'] = onError;
  openFilePickerImpl(callback);
}

// ===== index.uts (Android) =====
// 保持完全相同的签名！
import { openFilePickerImpl } from './file/picker.uts';

export function openFilePicker(
  onSuccess: ((result: SomeResult) => void) | null,
  onError: ((code: number, message: string) => void) | null
): void {
  const callback = new UTSJSONObject();
  callback['onSuccess'] = onSuccess;
  callback['onError'] = onError;
  openFilePickerImpl(callback);
}
```
