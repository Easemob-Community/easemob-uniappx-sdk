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
uni_modules/easemob-im/
├── package.json                # 插件配置信息
├── utssdk/
│   ├── interface.uts           # 接口定义（核心）
│   ├── unierror.uts            # 错误码定义
│   ├── index.uts               # 跨平台入口（条件编译）
│   ├── app-android/
│   │   ├── index.uts           # Android 实现
│   │   ├── config.json         # Android 依赖配置
│   │   └── AndroidManifest.xml # Android 权限配置
│   └── app-ios/
│       ├── index.uts           # iOS 实现
│       └── config.json         # iOS 依赖配置
```

### 文件职责

| 文件 | 职责 | 修改频率 |
|-----|------|---------|
| interface.uts | 定义对外 API 接口、类型 | 低（架构变更时） |
| unierror.uts | 定义错误码 | 低 |
| index.uts | 跨平台入口，条件编译分发 | 低 |
| app-android/index.uts | Android 桥接实现 | 中 |
| app-ios/index.uts | iOS 桥接实现 | 中 |

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
