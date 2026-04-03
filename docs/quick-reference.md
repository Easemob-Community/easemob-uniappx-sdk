# 环信IM SDK UniAppX 快速参考

## 快速开始代码

```uts
<script setup lang="uts">
import easemobIM from '@/uni_modules/easemob-im'
import { ref, onUnmounted } from 'vue'

// ========== 初始化 ==========
easemobIM.im.init({
  appKey: 'your-app-key',
  autoLogin: false,
  debugMode: true
})

// ========== 登录 ==========
easemobIM.im.login(
  { username: 'user1', password: '123456' },
  () => console.log('登录成功'),
  (code, msg) => console.error('登录失败', code, msg)
)

// ========== 监听消息 ==========
const messageListener = (message: EMMessage) => {
  console.log('收到:', message.body.content)
}
easemobIM.im.addMessageListener(messageListener)

// 清理监听
onUnmounted(() => {
  easemobIM.im.removeMessageListener(messageListener)
})

// ========== 发送消息 ==========
const sendText = () => {
  const param = easemobIM.im.createTextMessage('user2', 'Hello!')
  easemobIM.im.sendMessage(param, 
    (msg) => console.log('发送成功'),
    (code, msg) => console.error('发送失败', code, msg)
  )
}
</script>
```

## API 速查表

### 初始化

| API | 说明 |
|-----|------|
| `im.init(options)` | 初始化SDK |
| `im.isInitialized()` | 是否已初始化 |
| `im.getVersion()` | 获取SDK版本 |
| `im.setLogLevel(level)` | 设置日志级别 |

### 用户

| API | 说明 |
|-----|------|
| `im.register(user, pass, success?, fail?)` | 注册 |
| `im.login(params, success?, fail?)` | 登录 |
| `im.logout(unbind?, success?, fail?)` | 登出 |
| `im.isLoggedIn()` | 是否已登录 |
| `im.getCurrentUser()` | 获取当前用户 |
| `im.getToken()` | 获取Token |
| `im.renewToken(token)` | 刷新Token |

### 消息

| API | 说明 |
|-----|------|
| `im.sendMessage(param, success?, fail?, progress?)` | 发送消息 |
| `im.createTextMessage(to, content, chatType?)` | 创建文本消息 |
| `im.createImageMessage(to, path, chatType?)` | 创建图片消息 |
| `im.createVoiceMessage(to, path, duration, chatType?)` | 创建语音消息 |
| `im.createFileMessage(to, path, name, chatType?)` | 创建文件消息 |
| `im.resendMessage(msgId, success?, fail?)` | 重发消息 |
| `im.recallMessage(msgId, success?, fail?)` | 撤回消息 |
| `im.deleteMessage(msgId, success?, fail?)` | 删除消息 |
| `im.getMessageById(msgId)` | 根据ID获取消息 |
| `im.loadMessages(convId, type, limit?, fromId?)` | 加载历史消息 |

### 会话

| API | 说明 |
|-----|------|
| `im.getAllConversations()` | 获取所有会话 |
| `im.getConversation(id, type, create?)` | 获取指定会话 |
| `im.deleteConversation(id, type, deleteMsgs?, success?, fail?)` | 删除会话 |
| `im.getUnreadMessageCount()` | 获取未读总数 |
| `im.markAllMessagesAsRead(convId?)` | 标记已读 |
| `im.markMessageAsRead(msgId)` | 标记单条已读 |

### 监听

| API | 说明 |
|-----|------|
| `im.addMessageListener(listener)` | 添加消息监听 |
| `im.removeMessageListener(listener)` | 移除消息监听 |
| `im.addConnectionListener(listener)` | 添加连接监听 |
| `im.removeConnectionListener(listener)` | 移除连接监听 |
| `im.addTokenWillExpireListener(listener)` | Token将过期监听 |
| `im.removeTokenWillExpireListener(listener)` | 移除监听 |
| `im.addTokenDidExpireListener(listener)` | Token已过期监听 |
| `im.removeTokenDidExpireListener(listener)` | 移除监听 |

## 类型定义

### EMMessage

```uts
interface EMMessage {
  msgId: string           // 消息ID
  type: 'txt' | 'image' | 'voice' | 'file' | 'cmd'
  chatType: 'chat' | 'groupChat' | 'chatRoom'
  from: string            // 发送方
  to: string              // 接收方
  timestamp: number       // 时间戳
  isSender: boolean       // 是否发送方
  status: 'pending' | 'sending' | 'success' | 'fail'
  isRead: boolean         // 是否已读
  body: EMMessageBody     // 消息体
}
```

### EMConversation

```uts
interface EMConversation {
  conversationId: string
  type: 'chat' | 'groupChat' | 'chatRoom'
  unreadCount: number
  lastMessage?: EMMessage
}
```

## 错误码速查

| 码 | 含义 |
|---|------|
| 1 | 通用错误 |
| 2 | 网络错误 |
| 100 | 无效凭证 |
| 101 | 用户不存在 |
| 102 | 用户已存在 |
| 104 | 账号在其他设备登录 |
| 200 | 消息发送失败 |
| 401 | Token已过期 |
| 402 | Token即将过期 |

## 文件路径

```
uni_modules/easemob-uts-sdk/
├── utssdk/
│   ├── interface.uts          # API定义
│   ├── index.uts              # 入口（导出所有API）
│   ├── app-android/
│   │   ├── index.uts          # Android入口
│   │   ├── MessageHelper.kt   # Kotlin辅助类
│   │   ├── connection/        # 连接模块
│   │   ├── message/           # 消息模块
│   │   ├── auth/              # 认证模块
│   │   └── core/              # 核心模块
│   └── app-ios/
│       └── index.uts          # iOS实现
```

## 依赖版本

| 平台 | SDK版本 |
|-----|---------|
| Android | 4.11.0 |
| iOS | 4.11.0 |
