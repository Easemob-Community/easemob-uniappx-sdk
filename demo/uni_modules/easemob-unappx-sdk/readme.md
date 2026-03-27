# 环信即时通讯 SDK (UniApp X)

基于环信即时通讯 SDK 4.20.0 版本的 UniApp X UTS 插件，支持 Android 和 iOS 平台。

## 特性

- ✅ 基于环信 SDK 4.20.0 版本
- ✅ 支持 Android 平台
- ✅ 支持 iOS 平台
- ✅ UTS (Unified TypeScript) 编写
- ✅ TypeScript 类型支持

## 安装

将本插件放入项目的 `uni_modules` 目录下：

```
uni_modules/
  └── easemob-unappx-sdk/
```

## 快速开始

### 1. 初始化 SDK

```typescript
import { initChat } from '@/uni_modules/easemob-unappx-sdk';

initChat({
    appKey: 'your-app-key#your-app-name',
    autoLogin: false,
    debugMode: true,
    success: (res) => {
        console.log('初始化成功', res);
    },
    fail: (err) => {
        console.error('初始化失败', err);
    }
});
```

### 2. 用户注册

```typescript
import { registerUser } from '@/uni_modules/easemob-unappx-sdk';

registerUser({
    userId: 'user123',
    password: 'password123',
    success: (res) => {
        console.log('注册成功', res);
    },
    fail: (err) => {
        console.error('注册失败', err);
    }
});
```

### 3. 用户登录

**使用密码登录：**

```typescript
import { loginWithPassword } from '@/uni_modules/easemob-unappx-sdk';

loginWithPassword({
    userId: 'user123',
    password: 'password123',
    success: (res) => {
        console.log('登录成功', res);
    },
    fail: (err) => {
        console.error('登录失败', err);
    }
});
```

**使用 Token 登录（推荐）：**

```typescript
import { loginWithToken } from '@/uni_modules/easemob-unappx-sdk';

loginWithToken({
    userId: 'user123',
    token: 'your-token',
    success: (res) => {
        console.log('登录成功', res);
    },
    fail: (err) => {
        console.error('登录失败', err);
    }
});
```

### 4. 发送消息

```typescript
import { sendTextMessage } from '@/uni_modules/easemob-unappx-sdk';

sendTextMessage({
    content: '你好，环信！',
    to: 'user456',
    chatType: 0, // 0: 单聊, 1: 群聊, 2: 聊天室
    success: (res) => {
        console.log('发送成功', res);
    },
    fail: (err) => {
        console.error('发送失败', err);
    }
});
```

### 5. 登出

```typescript
import { logout } from '@/uni_modules/easemob-unappx-sdk';

logout({
    unbindToken: true,
    success: (res) => {
        console.log('登出成功', res);
    },
    fail: (err) => {
        console.error('登出失败', err);
    }
});
```

## API 列表

### 初始化

- `initChat(options: EasemobInitOptions)` - 初始化 SDK

### 认证

- `registerUser(options: EasemobRegisterOptions)` - 用户注册
- `loginWithPassword(options: EasemobLoginOptions)` - 密码登录
- `loginWithToken(options: EasemobLoginOptions)` - Token 登录
- `logout(options?)` - 用户登出

### 消息

- `sendTextMessage(options: EasemobSendMessageOptions)` - 发送文本消息

### 状态查询

- `getCurrentUser()` - 获取当前登录用户
- `isLoggedIn()` - 判断是否已登录
- `isConnected()` - 判断是否连接到服务器
- `loadAllConversations()` - 加载本地会话（Android）
- `loadAllGroups()` - 加载本地群组（Android）

## 依赖版本

- Android: `io.hyphenate:hyphenate-chat:4.20.0`
- iOS: `HyphenateChat 4.20.0`

## 平台支持

| 平台 | 支持状态 |
|------|----------|
| Android | ✅ 支持 |
| iOS | ✅ 支持 |
| HarmonyOS | ⏳ 待实现 |

## 注意事项

1. 本插件基于 UniApp X (uni-app-x) 开发，需要使用 HBuilderX 4.0+ 版本
2. Android 平台最低支持 API 21 (Android 5.0)
3. iOS 平台最低支持 iOS 12.0
4. 用户注册功能仅用于开发测试，生产环境建议在服务端完成用户注册

## 相关链接

- [环信官方文档](https://doc.easemob.com/)
- [环信控制台](https://console.easemob.com/)
- [UniApp X 文档](https://uniapp.dcloud.net.cn/uni-app-x/)

## 许可证

MIT
