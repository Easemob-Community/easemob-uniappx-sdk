# 环信IM SDK for UniAppX

基于 UniAppX + UTS 开发的环信即时通讯 SDK 封装，支持 Android 和 iOS 双端。

## 功能特性

- ✅ SDK 初始化与配置
- ✅ 用户注册、登录、登出
- ✅ 单聊消息发送与接收
- ✅ 文本、图片、语音、文件消息
- ✅ 消息撤回与删除
- ✅ 会话列表管理
- ✅ 未读消息计数
- ✅ 连接状态监听
- ✅ Token 过期处理

## 技术架构

```
UniAppX 应用层
    ↓
UTS 插件层 (easemob-im)
    ├── interface.uts (API接口定义)
    ├── app-android/index.uts (Android桥接)
    └── app-ios/index.uts (iOS桥接)
    ↓
原生 SDK 层
    ├── 环信 Android SDK
    └── 环信 iOS SDK
```

## 快速开始

### 1. 安装

将 `uni_modules/easemob-im` 复制到你的 UniAppX 项目的 `uni_modules` 目录下。

### 2. 初始化 SDK

```uts
<script setup lang="uts">
import easemobIM from '@/uni_modules/easemob-im'

// 初始化
const initSDK = () => {
  easemobIM.im.init({
    appKey: 'your-app-key',      // 环信控制台获取的 AppKey
    autoLogin: false,             // 是否自动登录
    debugMode: true               // 是否开启调试模式
  })
}
</script>
```

### 3. 用户登录

```uts
// 注册
easemobIM.im.register(
  'username',
  'password',
  () => console.log('注册成功'),
  (code, message) => console.error('注册失败:', code, message)
)

// 登录
easemobIM.im.login(
  { username: 'user1', password: '123456' },
  () => console.log('登录成功'),
  (code, message) => console.error('登录失败:', code, message)
)

// 登出
easemobIM.im.logout(
  true,  // 是否解绑设备Token
  () => console.log('登出成功'),
  (code, message) => console.error('登出失败:', code, message)
)
```

### 4. 消息收发

```uts
// 添加消息接收监听
easemobIM.im.addMessageListener((message: EMMessage) => {
  console.log('收到消息:', message.body.content, '来自:', message.from)
})

// 发送文本消息
const sendMessage = () => {
  const param = easemobIM.im.createTextMessage('user2', 'Hello, World!')
  
  easemobIM.im.sendMessage(
    param,
    (message) => console.log('发送成功:', message.msgId),
    (code, message) => console.error('发送失败:', code, message),
    (progress) => console.log('发送进度:', progress)
  )
}
```

### 5. 会话管理

```uts
// 获取所有会话
const conversations = easemobIM.im.getAllConversations()

// 获取未读消息总数
const unreadCount = easemobIM.im.getUnreadMessageCount()

// 标记所有消息已读
easemobIM.im.markAllMessagesAsRead()
```

## 示例项目

查看 `pages/index/index.uvue` 获取完整的示例代码。

## API 文档

详见 `docs/pre-research-report.md` 中的 API 定义章节。

## 开发环境要求

- HBuilderX 4.0+
- Android 5.0+ (API 21+)
- iOS 12.0+

## 原生SDK版本

| 平台 | SDK版本 |
|-----|---------|
| Android | 4.11.0 |
| iOS | 4.11.0 |

## 目录结构

```
uni_modules/easemob-im/
├── package.json                # 插件配置
├── utssdk/
│   ├── interface.uts           # API接口定义
│   ├── unierror.uts            # 错误码定义
│   ├── index.uts               # 跨平台入口
│   ├── app-android/
│   │   ├── index.uts           # Android实现
│   │   ├── config.json         # Android配置
│   │   └── AndroidManifest.xml # Android权限
│   └── app-ios/
│       ├── index.uts           # iOS实现
│       └── config.json         # iOS配置
└── components/                 # UI组件（可选）
```

## 注意事项

1. **隐私合规**：在用户同意隐私政策前不要初始化 SDK
2. **推送配置**：如需离线推送，需额外配置推送服务
3. **自定义基座**：涉及原生 SDK 功能需要打自定义基座测试
4. **调试模式**：开发时建议开启 `debugMode`，生产环境关闭

## 错误码

| 错误码 | 说明 |
|-------|------|
| 1 | 通用错误 |
| 2 | 网络错误 |
| 100 | 无效凭证 |
| 101 | 用户不存在 |
| 102 | 用户已存在 |
| 104 | 用户在其他设备登录 |
| 200 | 消息发送失败 |
| 401 | Token已过期 |
| 402 | Token即将过期 |

## 贡献

欢迎提交 Issue 和 Pull Request。

## 许可证

MIT License

## 相关链接

- [环信官网](https://www.easemob.com/)
- [环信IM文档](https://doc.easemob.com/)
- [UniAppX官方文档](https://doc.dcloud.net.cn/uni-app-x/)
