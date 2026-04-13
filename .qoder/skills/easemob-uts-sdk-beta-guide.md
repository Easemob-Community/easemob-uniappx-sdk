# easemob-uts-sdk-beta 开发规范

## Description
专为 `easemob-uts-sdk-beta` 模块（重新设计版）提供的完整开发规范。涵盖架构设计、类型系统、Android 回调机制、枚举导出、测试页写法等所有经过真实编译验证的规则。每一条都来自本模块的实际踩坑教训。

## Trigger
当修改 `uni_modules/easemob-uts-sdk-beta/` 目录下任何文件，或新增 SDK API 时，自动触发此规范检查。

---

## 一、模块架构总览

### 文件职责

| 文件 | 职责 |
|------|------|
| `utssdk/interface.uts` | 所有公共类型、枚举、接口定义（唯一类型来源） |
| `utssdk/index.uts` | 跨平台主入口，条件编译分发，**统一导出类型** |
| `utssdk/app-android/index.uts` | Android 平台实现，只导出函数，**不重复 re-export 类型** |
| `utssdk/app-ios/index.uts` | iOS 平台实现，只导出函数，**不重复 re-export 类型** |
| `utssdk/app-ios/em_bridge.swift` | iOS 原生桥接层，实现 `EMClientDelegate` 及独立回调函数 |
| `android/.../index.kt` | HBuilderX 自动转译产物，**禁止手动维护** |

### 关键约束

- **类型只在 `utssdk/index.uts` 统一导出一次**，平台文件（`app-android/index.uts`、`app-ios/index.uts`）末尾**绝对不能**再 re-export 同名类型，否则 Kotlin 转译时枚举/类名会产生 `__1` 后缀，导致页面层 `Unresolved reference` 错误。
- **`android/` 目录是编译产物**，每次 HBuilderX「重新生成本地资源」会覆盖，所有修改必须从 UTS 源码层（`utssdk/`）根治。
- **iOS 的 `em_bridge.swift` 是原生桥接文件**，由 UTS 编译器自动识别，无需 `declare module` 或 `declare class`。

---

## 二、类型定义规范

### 2.1 配置对象：必须用 `type`，禁用 `interface`

```typescript
// ✅ 正确：用 type
export type EMInitConfig = {
  appKey: string;
  autoLogin?: boolean;
}

// ❌ 错误：interface 在 Kotlin 层没有构造器，页面侧用字面量初始化会报错
export interface EMInitConfig {
  appKey: string;
}
```

**原因**：UTS `interface` 转译为 Kotlin `interface`，无构造器。页面侧 `const config = { appKey: '...' }` 字面量会被转为 `UTSJSONObject`，传给期望 `interface` 类型的函数就会报 `Argument type mismatch`。

### 2.2 函数参数：对象字面量必须用 `UTSJSONObject`

所有接受配置对象的导出函数，**参数类型必须是 `UTSJSONObject`**，在函数内部读取字段：

```typescript
// ✅ 正确
export function initSDK(config: UTSJSONObject): Promise<void> {
  const appKey = config['appKey'] as string;
  const autoLogin = (config['autoLogin'] as boolean) ?? false;
  // ...
}

// ❌ 错误：config 为 EMInitConfig 类型，页面传字面量时类型不匹配
export function initSDK(config: EMInitConfig): Promise<void> { ... }
```

**原因**：UTS 页面侧的对象字面量 `{ appKey: 'xxx' }` 在转译后是 `UTSJSONObject`，不是任何具名类/接口实例。

### 2.3 枚举：只在 `interface.uts` 定义，只在 `index.uts` 导出一次

```typescript
// interface.uts ✅
export enum EMConnectionState {
  CONNECTED = 0,
  CONNECTING = 1,
  DISCONNECTED = 2,
}

// index.uts ✅ 统一导出
export { EMConnectionState, EMConnectionEvent, ... } from './interface.uts';

// app-android/index.uts ✅ 末尾只有注释，不重复导出
// 类型已在 utssdk/index.uts 统一导出，此处不重复 re-export，避免转译时产生 __1 别名冲突
```

### 2.4 事件数据类：必须用 `class`，不能用 `interface` 或 `type`

```typescript
// ✅ 正确：class 支持 new 构造
export class EMConnectionStateChangedEvent {
  state: EMConnectionState;
  event: EMConnectionEvent;
  ext: string | null;
  constructor(state: EMConnectionState, event: EMConnectionEvent, ext: string | null = null) {
    this.state = state;
    this.event = event;
    this.ext = ext;
  }
}

// ❌ 错误：interface/type 无法用 new 实例化
export interface EMConnectionStateChangedEvent { ... }
```

---

## 三、Android 回调机制（EMCallBackImpl 模式）

### 3.1 核心问题：Promise resolve/reject 类型不匹配

UTS 的 `new Promise((resolve, reject) => {...})` 转译后，`resolve` 类型是 `(Unit) -> Unit`（`Function1`），而 Kotlin 函数默认期望 `() -> Unit`（`Function0`）。直接把 `resolve`/`reject` 传递给任何 Kotlin 类构造器都会触发类型错误。

### 3.2 标准解法：EMCallBackImpl + makeCallBack 工厂函数

```typescript
// 通用 EMCallBack 实现：通过成员属性持有闭包，彻底避免 Promise 类型转译问题
class EMCallBackImpl extends EMCallBack {
  @UTSJS.keepAlive
  onSuccessHandler: (() => void) | null = null;
  @UTSJS.keepAlive
  onErrorHandler: ((code: number, message: string) => void) | null = null;

  override onSuccess(): void {
    this.onSuccessHandler?.();
  }

  // 注意：errorCode 必须是 Int 类型（不能是 number），否则 Kotlin override 签名不匹配
  override onError(code: Int, message: string): void {
    this.onErrorHandler?.(code, message);
  }
}

function makeCallBack(
  onSuccess: () => void,
  onError: (code: number, message: string) => void
): EMCallBackImpl {
  const cb = new EMCallBackImpl();
  cb.onSuccessHandler = onSuccess;
  cb.onErrorHandler = onError;
  return cb;
}

// 使用方式（login 示例）
login(config: UTSJSONObject): Promise<void> {
  return new Promise((resolve, reject) => {
    try {
      const callback = makeCallBack(
        (): void => {
          console.log('[Android] Login success');
          resolve();  // ✅ 不传 Unit，让 UTS 编译器自动处理
        },
        (code: number, message: string): void => {
          reject(new Error(`Login failed: ${code} - ${message}`));
        }
      );
      EMClient.getInstance().login(userId, password, callback);
    } catch (error) {
      reject(error);
    }
  });
}
```

**为什么有效**：`onSuccessHandler`/`onErrorHandler` 是普通成员属性，持有的箭头函数 `(): void` 转译为 `() -> Unit`（`Function0`），与属性声明类型完全一致。`resolve()` 在箭头函数内部调用，无需关心其 Kotlin 类型。

### 3.3 关键细节

- `override onError(code: Int, message: string)` — 参数必须用 `Int`（Kotlin 基础类型），不能用 `number`
- `@UTSJS.keepAlive` — 成员闭包属性必须加此装饰器，防止 GC 回收
- `resolve()` 调用时不传参数（不传 `Unit`）

---

## 四、Android 导入规范

### 4.1 直接 import，不使用 utsAndroidClassOf

```typescript
// ✅ 正确
import EMClient from 'com.hyphenate.chat.EMClient';
import EMOptions from 'com.hyphenate.chat.EMOptions';
import EMCallBack from 'com.hyphenate.EMCallBack';
import EMConnectionListener from 'com.hyphenate.EMConnectionListener';

// ❌ 错误：utsAndroidClassOf 是运行时反射，不适合此场景
const EMClient = utsAndroidClassOf('com.hyphenate.chat.EMClient');
```

### 4.2 UTSAndroid 不需要显式导入

```typescript
// ✅ 正确：UTSAndroid 由编译器自动注入
EMClient.getInstance().init(UTSAndroid.getAppContext()!, options);

// ❌ 错误：显式 import 会报 Unresolved reference
import UTSAndroid from 'io.dcloud.uts.UTSAndroid';
```

---

## 五、连接监听器实现规范

```typescript
class EMConnectionListenerImpl extends EMConnectionListener {
  @UTSJS.keepAlive
  onConnectionStateChanged: EMConnectionStateChangedListener | null = null;

  override onConnected(): void {
    const eventData = new EMConnectionStateChangedEvent(
      EMConnectionState.CONNECTED,
      EMConnectionEvent.LOGIN_SUCCESS
    );
    this.onConnectionStateChanged?.(eventData);
  }

  // 必须是 Int 类型
  override onDisconnected(errorCode: Int): void {
    let event = EMConnectionEvent.NETWORK_ERROR;
    if (errorCode == 206) {
      event = EMConnectionEvent.KICKED_BY_OTHER_DEVICE;
    } else if (errorCode == 202 || errorCode == 204) {
      event = EMConnectionEvent.LOGIN_FAILED;
    }
    const eventData = new EMConnectionStateChangedEvent(
      EMConnectionState.DISCONNECTED,
      event,
      errorCode.toString()
    );
    this.onConnectionStateChanged?.(eventData);
  }

  override onTokenWillExpire(): void {}
  override onTokenExpired(): void {}
}
```

---

## 六、单例模式规范

```typescript
class EMClientImpl implements IEMClient {
  private static _instance: EMClientImpl | null = null;
  private _isInitialized = false;

  private constructor() {}

  static getInstance(): EMClientImpl {
    if (this._instance == null) {
      this._instance = new EMClientImpl();
    }
    return this._instance!;
  }
  // ...
}
```

---

## 七、导出函数规范

### 7.1 `app-android/index.uts` 导出结构

```typescript
// 1. 导入环信 SDK 类
import EMClient from 'com.hyphenate.chat.EMClient';
// ...

// 2. 从 interface.uts 导入类型（不重复导出！）
import { IEMClient, EMConnectionState, ... } from '../interface.uts';

// 3. 内部类实现（class，不 export）
class EMConnectionListenerImpl extends EMConnectionListener { ... }
class EMCallBackImpl extends EMCallBack { ... }
function makeCallBack(...): EMCallBackImpl { ... }
class EMClientImpl implements IEMClient { ... }

// 4. 导出函数（必须用 export function，不用 export class）
export function initSDK(config: UTSJSONObject): Promise<void> {
  return EMClientImpl.getInstance().initSDK(config);
}
export function login(config: UTSJSONObject): Promise<void> { ... }
export function logout(): Promise<void> { ... }
// 桩函数
export function onError(_listener: EMErrorListener | null): void {}
// ...

// 5. 末尾注释（不重复导出类型）
// 类型已在 utssdk/index.uts 统一导出，此处不重复 re-export，避免转译时产生 __1 别名冲突
```

### 7.2 `utssdk/index.uts` 导出结构

```typescript
// 1. 类型统一从 interface.uts 导出
export {
  EMInitConfig, EMLoginConfig,
  EMConnectionState, EMConnectionEvent, EMConnectionStateChangedEvent,
  EMError, EMConnectionStateChangedListener, IEMClient,
  // ...
} from './interface.uts';

// 2. 平台函数条件编译导出
// #ifdef APP-ANDROID
export {
  initSDK, login, logout, destroy,
  onConnectionStateChanged, onError, ...
} from './app-android/index.uts';
// #endif

// #ifdef APP-IOS
export { ... } from './app-ios/index.uts';
// #endif
```

---

## 八、页面使用规范（sdk-demo.uvue）

### 8.1 导入规范

```typescript
// #ifdef APP-ANDROID
import {
  initSDK,
  login,
  logout,
  onConnectionStateChanged,
  // ❌ 不要导入枚举 EMConnectionState，在页面侧用数值比较
} from '@/uni_modules/easemob-uts-sdk-beta'
// #endif
```

### 8.2 禁止在页面导入枚举

枚举从 `easemob-uts-sdk-beta` 导入后，`sdk-demo.kt` 里会引用 `EMConnectionState`，但 `index.kt` 里枚举名可能带 `__1` 后缀，导致 `Unresolved reference`。

**替代方案：用数值直接比较**

```typescript
onConnectionStateChanged((event) => {
  let statusText = '未知'
  const stateVal = event.state as number
  if (stateVal == 0) {        // CONNECTED
    statusText = '已连接'
  } else if (stateVal == 1) { // CONNECTING
    statusText = '连接中'
  } else if (stateVal == 2) { // DISCONNECTED
    statusText = '已断开'
  }
  connectionStatus.value = statusText
})
```

### 8.3 禁止对 `type` 类型加类型标注

```typescript
// ✅ 正确：不标注类型，让 UTS 自动推断
const config = { appKey: appKey.value, autoLogin: false }
await initSDK(config)

// ❌ 错误：type 标注会导致 Interface does not have constructors 错误
const config : EMInitConfig = { appKey: appKey.value, autoLogin: false }
```

### 8.4 禁止使用计算属性键

```typescript
// ❌ 错误：UTS 不支持计算属性键，类型推断失败
const statusMap = {
  [EMConnectionState.CONNECTED]: '已连接',
}

// ✅ 正确：用 if/else if 链
if (stateVal == 0) { statusText = '已连接' }
```

### 8.5 uvue 页面纵向滚动规范

uni-app x 的 uvue 页面**不支持 CSS `overflow-y` / `overflow: scroll`**（仅支持 `visible`/`hidden`），必须通过 `scroll-view` 实现纵向滚动。

#### ✅ 正确结构

```vue
<template>
  <scroll-view class="container" scroll-y="true">
    <view class="content">
      <!-- 页面内容 -->
    </view>
  </scroll-view>
</template>

<style>
  .container {
    flex: 1;        /* 必须：让 scroll-view 占满全屏 */
    background-color: #f5f5f5;
  }
  .content {
    padding: 15px;  /* 内容边距放在内层 view */
  }
</style>
```

#### ❌ 常见错误

1. **根容器用 `view`，内部再嵌 `scroll-view`**
   - `scroll-view` 嵌套在 `view` 里时高度由内容撑开，没有可滚动空间。

2. **使用 `position: fixed` 浮窗**
   - 真机上 `fixed` 定位元素会拦截触摸事件，导致外层 `scroll-view` 无法滚动。

3. **依赖 CSS `overflow-y: scroll`**
   - uvue 编译器会忽略或报错，真机完全无效。

#### 日志区域写法

日志应放在页面底部，作为普通 `section`，内部再用固定高度的 `scroll-view` 展示：

```vue
<view class="section">
  <text class="section-title">日志</text>
  <scroll-view class="log-scroll" scroll-y="true" :scroll-top="logScrollTop">
    <text class="log-text">{{ logs }}</text>
  </scroll-view>
</view>
```

```css
.log-scroll {
  height: 150px;
  background-color: #f9f9f9;
  border-radius: 5px;
  padding: 10px;
}
```

#### 修改后必须清缓存

uvue 页面结构修改后，**必须删除编译缓存**再运行，否则旧布局缓存会导致修改不生效：

```bash
# 删除 HBuilderX 编译缓存
rm -rf unpackage/dist
rm -rf unpackage/cache
```

---

## 九、iOS 实现规范

iOS 平台已实现完整功能（初始化、登录、登出、连接监听、状态查询），采用 **UTS + Swift 桥接** 的混合架构。

### 9.1 文件结构

```
utssdk/app-ios/
├── config.json          # CocoaPods 依赖配置
├── index.uts            # UTS 层：封装类、导出函数
└── em_bridge.swift      # Swift 桥接层：EMClientDelegate + 独立回调函数
```

### 9.2 Swift 桥接层（em_bridge.swift）

#### 核心设计

- 使用 `fileprivate class EMDelegateNative: NSObject, EMClientDelegate` 实现原生代理
- 使用模块级单例 `private var _emDelegate: EMDelegateNative?` 持有代理实例
- 暴露**独立函数**给 UTS 调用（无需 `declare`）

```swift
import HyphenateChat

fileprivate class EMDelegateNative: NSObject, EMClientDelegate {
    var onConnectedCb: (() -> Void)?
    var onDisconnectedCb: ((NSNumber) -> Void)?
    var onLogoutCb: ((NSNumber) -> Void)?
    var onTokenWillExpireCb: (() -> Void)?
    var onTokenExpiredCb: (() -> Void)?

    public func connectionStateDidChange(_ aConnectionState: EMConnectionState) {
        if aConnectionState == EMConnectionState.connected {
            self.onConnectedCb?()
        } else {
            self.onDisconnectedCb?(0)
        }
    }

    public func userAccountDidForced(toLogout aError: EMError?) {
        if let error = aError {
            let code = error.code.rawValue as NSNumber
            self.onLogoutCb?(code)
        }
    }

    public func tokenWillExpire(_ aErrorCode: EMErrorCode) {
        self.onTokenWillExpireCb?()
    }

    public func tokenDidExpire(_ aErrorCode: EMErrorCode) {
        self.onTokenExpiredCb?()
    }
}

private var _emDelegate: EMDelegateNative?

func emBridgeSetupDelegate() {
    let d = EMDelegateNative()
    _emDelegate = d
    EMClient.shared().add(d, delegateQueue: nil)
}

func emBridgeTeardownDelegate() {
    if let d = _emDelegate {
        EMClient.shared().removeDelegate(d)
        _emDelegate = nil
    }
}

func emBridgeSetOnConnected(callback: (() -> Void)?) {
    _emDelegate?.onConnectedCb = callback
}

func emBridgeSetOnDisconnected(callback: ((NSNumber) -> Void)?) {
    _emDelegate?.onDisconnectedCb = callback
}

func emBridgeSetOnLogout(callback: ((NSNumber) -> Void)?) {
    _emDelegate?.onLogoutCb = callback
}

func emBridgeSetOnTokenWillExpire(callback: (() -> Void)?) {
    _emDelegate?.onTokenWillExpireCb = callback
}

func emBridgeSetOnTokenExpired(callback: (() -> Void)?) {
    _emDelegate?.onTokenExpiredCb = callback
}
```

#### 关键约束

- **回调参数类型必须用 `NSNumber`**，不能用 `Int` 或 `number`。UTS 侧接收为 `number`。
- **Swift 独立函数无需 `declare`**，UTS 编译器会自动识别同目录下的 `.swift` 文件。
- **代理类用 `fileprivate`**，避免与 UTS 侧类型名冲突。

### 9.3 UTS iOS 层（app-ios/index.uts）

#### 导入规范

```typescript
// ✅ 正确：从 HyphenateChat framework 导入
import { EMClient, EMOptions, EMClientDelegate, EMError, EMErrorCode, EMConnectionState } from 'HyphenateChat';

// ✅ 正确：从 interface.uts 导入类型
import { IEMClient, EMErrorListener, EMTokenWillExpireListener, EMTokenExpiredListener } from '../interface.uts';
```

#### Promise 封装模式

iOS SDK 使用 completion handler 回调，UTS 侧用 Promise 封装：

```typescript
login(config: UTSJSONObject): Promise<void> {
  return new Promise((resolve, reject) => {
    try {
      const userId = config['userId'] as string;
      const password = config['password'] as string;
      const useToken = (config['useToken'] as boolean) ?? false;

      const completion = (username: string, error: EMError | null): void => {
        if (error != null) {
          const code = error!.code.rawValue as number;
          const msg = error!.errorDescription ?? 'Login failed';
          reject(new Error(`Login failed: ${code} - ${msg}`));
        } else {
          resolve();
        }
      };

      if (useToken) {
        EMClient.shared().login(withUsername = userId, token = password, completion = completion);
      } else {
        EMClient.shared().login(withUsername = userId, password = password, completion = completion);
      }
    } catch (error) {
      reject(error);
    }
  });
}
```

#### 命名参数调用

iOS Swift API 在 UTS 中调用时，**必须带参数标签**（named parameters）：

```typescript
// ✅ 正确：带参数标签
EMClient.shared().login(withUsername = userId, password = password, completion = completion);
EMClient.shared().initializeSDK(with = options);
EMClient.shared().logout(true, completion = completion);

// ❌ 错误：不带标签或位置参数
EMClient.shared().login(userId, password, completion);
```

#### 事件监听实现

```typescript
@UTSJS.keepAlive
onConnected(listener: (() => void) | null): void {
  emBridgeSetOnConnected(callback = listener);
}

@UTSJS.keepAlive
onDisconnected(listener: ((errorCode: number) => void) | null): void {
  emBridgeSetOnDisconnected(callback = listener);
}
```

#### 状态查询

```typescript
getVersion(): string {
  return EMClient.shared().version;
}

isConnected(): boolean {
  return EMClient.shared().isConnected;
}

isLoggedIn(): boolean {
  return EMClient.shared().isLoggedIn;
}

getCurrentUser(): string | null {
  return EMClient.shared().currentUsername;
}
```

### 9.4 已验证可用的 iOS API 列表

以下 API 已在真机/模拟器上验证可用：

- `initSDK(config: UTSJSONObject): Promise<void>`
- `login(config: UTSJSONObject): Promise<void>`
- `logout(): Promise<void>`
- `destroy(): void`
- `onConnected(listener: (() => void) | null): void`
- `onDisconnected(listener: ((errorCode: number) => void) | null): void`
- `onLogout(listener: ((errorCode: number) => void) | null): void`
- `onTokenWillExpire(listener: EMTokenWillExpireListener | null): void`
- `onTokenExpired(listener: EMTokenExpiredListener | null): void`
- `getVersion(): string`
- `isConnected(): boolean`
- `isLoggedIn(): boolean`
- `getCurrentUser(): string | null`

### 9.5 iOS 导出结构

```typescript
// app-ios/index.uts
export function initSDK(config: UTSJSONObject): Promise<void> { ... }
export function login(config: UTSJSONObject): Promise<void> { ... }
export function logout(): Promise<void> { ... }
export function destroy(): void { ... }
export function onConnected(listener: (() => void) | null): void { ... }
export function onDisconnected(listener: ((errorCode: number) => void) | null): void { ... }
export function onLogout(listener: ((errorCode: number) => void) | null): void { ... }
export function onTokenWillExpire(listener: EMTokenWillExpireListener | null): void { ... }
export function onTokenExpired(listener: EMTokenExpiredListener | null): void { ... }
export function getVersion(): string { ... }
export function isConnected(): boolean { ... }
export function isLoggedIn(): boolean { ... }
export function getCurrentUser(): string | null { ... }

// 类型已在 utssdk/index.uts 统一导出，此处不重复 re-export
```

---

## 十、桩函数规范

iOS 平台及暂未实现的 Android 功能，用空函数占位，不影响编译：

```typescript
// 桩函数：参数加下划线前缀，避免 unused 警告
export function onError(_listener: EMErrorListener | null): void {}
```

**注意**：iOS 的 `onError` 目前是桩函数，因为环信 iOS SDK 的 `EMClientDelegate` 没有直接的 `onError` 统一回调。

---

## 十、常见编译错误速查

| 错误信息 | 根因 | 解决方法 |
|---------|------|---------|
| `Unresolved reference 'EMConnectionState'` | 平台文件重复 re-export 导致枚举加了 `__1` 后缀 | 删除 `app-android/index.uts` 末尾的类型 re-export |
| `Interface 'EMInitConfig' does not have constructors` | `interface` 无构造器 | 改为 `type` |
| `Argument type mismatch: UTSJSONObject but EMInitConfig expected` | 函数签名用了具名类型 | 改为 `UTSJSONObject` 参数 |
| `Function0/Function1 type mismatch` | 直接把 `resolve`/`reject` 传给 Kotlin 类构造器 | 使用 `EMCallBackImpl + makeCallBack` 模式 |
| `override fun onDisconnected` 签名不匹配 | UTS 里写了 `number` 类型 | 改为 `Int` |
| `Unresolved reference 'Application'` | 显式导入了 Android 系统类 | 删除，使用 `UTSAndroid.getAppContext()` |
| `index.kt` 修改后被覆盖 | HBuilderX 重新生成本地资源会覆盖 kt | 只改 UTS 源码，不改 kt |

---

## 十二、新增 API 标准流程

1. 在 `interface.uts` 定义类型/接口
2. 在 `app-android/index.uts` 实现（`EMClientImpl` 方法 + 顶层导出函数）
3. 在 `app-ios/index.uts` 实现（`EMClientImpl` 方法 + 顶层导出函数）
4. 如需 iOS 原生桥接，在 `app-ios/em_bridge.swift` 中添加代理方法或独立函数
5. 在 `utssdk/index.uts` 的两个平台条件编译块中添加导出
6. **检查**：`app-android/index.uts` 和 `app-ios/index.uts` 末尾都没有重复 re-export 类型
7. **检查**：导出函数参数如为对象，使用 `UTSJSONObject`
8. **检查**：iOS Swift API 调用时带参数标签（named parameters）
9. 重新生成本地资源验证编译
