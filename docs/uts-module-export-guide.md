# UTS 模块导出机制问题解决方案

## 问题现象

在 UTS 插件开发中，从子模块导入函数并在入口文件中重新导出时，编译器报错：

```
error: Function invocation 'getCurrentUser()' expected.
```

## 错误代码示例

```typescript
// apis/modules/login.uts
export function getCurrentUser(): string {
  const user = EMClient.getInstance().getCurrentUser()
  return user != null ? user : ""
}

// app-android/index.uts - ❌ 错误写法
import { getCurrentUser as getCurrentUserImpl } from './apis/modules/login.uts'

export function getCurrentUser(): string {
  return getCurrentUserImpl()  // 编译报错！
}
```

## 根本原因

UTS 编译器在处理同名导入导出时存在命名冲突问题，即使使用别名也无法避免。编译器会把导入的别名和导出的函数名混淆。

## 正确解决方案

使用直接重新导出语法，不在中间层定义包装函数：

```typescript
// app-android/index.uts - ✅ 正确写法
import { loginEMClient } from './apis/modules/login.uts'

// 需要包装逻辑的函数：正常导入导出
export function loginSDK(userId: string, password: string, callback: UTSJSONObject): void {
  // 可以添加额外逻辑
  loginEMClient(userId, password, callback)
}

// 不需要包装逻辑的函数：直接重新导出
export { getCurrentUser, isLoggedInBefore, isConnected, isLoggedIn } from './apis/modules/login.uts'
```

## 完整示例

### 子模块实现 (apis/modules/login.uts)

```typescript
export function getCurrentUser(): string {
  const user = EMClient.getInstance().getCurrentUser()
  return user != null ? user : ""
}

export function isLoggedIn(): boolean {
  return EMClient.getInstance().isLoggedIn()
}

export function isConnected(): boolean {
  return EMClient.getInstance().isConnected()
}

export function isLoggedInBefore(): boolean {
  return EMClient.getInstance().isLoggedInBefore()
}
```

### 平台入口文件 (app-android/index.uts)

```typescript
import {
  loginEMClient,
  loginWithToken as loginWithTokenImpl,
  logoutEMClient
} from './apis/modules/login.uts'

// 直接重新导出状态查询函数（无需包装）
export { getCurrentUser, isLoggedInBefore, isConnected, isLoggedIn } from './apis/modules/login.uts'

// 需要包装逻辑的函数
export function loginSDK(userId: string, password: string, callback: UTSJSONObject): void {
  loginEMClient(userId, password, callback)
}

export function logoutSDK(unbindToken: boolean, callback: UTSJSONObject = {} as UTSJSONObject): void {
  logoutEMClient(unbindToken, callback)
}
```

### 统一入口文件 (utssdk/index.uts)

```typescript
export * from './interface.uts'

// #ifdef APP-ANDROID
export { 
  initSDK, 
  addConnectionListener, 
  removeConnectionListener,
  loginSDK,
  logoutSDK,
  getCurrentUser,      // 从 app-android/index.uts 透传
  isLoggedInBefore,    // 从 app-android/index.uts 透传
  isConnected,         // 从 app-android/index.uts 透传
  isLoggedIn           // 从 app-android/index.uts 透传
} from './app-android/index.uts'
// #endif
```

## 使用场景

| 场景 | 处理方式 | 示例 |
|------|----------|------|
| 需要添加额外逻辑 | 正常导入导出，定义包装函数 | `loginSDK` 包装 `loginEMClient` |
| 直接透传 | 使用直接重新导出语法 | `export { getCurrentUser } from './module'` |

## 注意事项

1. **避免同名冲突**：不要在入口文件中定义与子模块同名的函数，即使使用别名也不行
2. **平台一致性**：确保所有平台入口文件（app-android、app-ios）都正确处理导出
3. **文档注释**：直接重新导出的函数，其文档注释保留在子模块中

## 相关错误码

- `error18`: 找不到名称（函数未正确导出）
- `Function invocation 'xxx()' expected`: 命名冲突导致编译器混淆

## 参考

- [UTS 开发文档](https://doc.dcloud.net.cn/uni-app-x/uts/)
- [UTS 编译器已知问题](https://doc.dcloud.net.cn/uni-app-x/uts/compiler-known-issues.html)
