/**
 * 环信IM SDK - Android实现
 * 适配环信 SDK 4.15.1
 * 根据编译错误修正: EMCallBack 和 EMMessageListener 在SDK 4.15.1中是抽象类
 */

import type { EMLoginSuccess, EMLoginFail, EMSendSuccess, EMSendFail, EMMessageCallback, EMMessage } from '../interface.uts'

// 全局状态
let gInited = false
let gLogined = false

// 存储回调函数
let gLoginSuccess: EMLoginSuccess | null = null
let gLoginFail: EMLoginFail | null = null
let gLogoutSuccess: (() => void) | null = null
let gSendSuccess: EMSendSuccess | null = null
let gSendFail: EMSendFail | null = null
let gMsgCallback: EMMessageCallback | null = null

/**
 * 登录回调实现 - extends 抽象类 EMCallBack
 * 根据编译错误: EMCallBack 是抽象类，需要用 extends + override
 */
class EMLoginCallBack extends com.hyphenate.EMCallBack {
  constructor() {
    super()
  }

  // onSuccess - 抽象方法，需要 override
  override onSuccess(): void {
    gLogined = true
    // 登录成功后加载会话
    com.hyphenate.chat.EMClient.getInstance().chatManager().loadAllConversations()
    // 保存到局部变量避免 Smart cast 问题
    const callback = gLoginSuccess
    if (callback != null) {
      callback()
    }
    gLoginSuccess = null
    gLoginFail = null
  }

  // onError - 抽象方法，需要 override
  // Kotlin 映射签名: fun onError(p0: Int, p1: String!): Unit
  override onError(code: Int, error: string): void {
    const callback = gLoginFail
    if (callback != null) {
      callback(code as number, error)
    }
    gLoginSuccess = null
    gLoginFail = null
  }

  // onProgress - 已经有默认实现，但为了保险也写上
  override onProgress(progress: Int, status: string): void {
    // 暂不处理进度
  }
}

/**
 * 登出回调实现
 */
class EMLogoutCallBack extends com.hyphenate.EMCallBack {
  constructor() {
    super()
  }

  override onSuccess(): void {
    gLogined = false
    const callback = gLogoutSuccess
    if (callback != null) {
      callback()
    }
    gLogoutSuccess = null
  }

  override onError(code: Int, error: string): void {
    // 即使失败也视为登出
    gLogined = false
    const callback = gLogoutSuccess
    if (callback != null) {
      callback()
    }
    gLogoutSuccess = null
  }

  override onProgress(progress: Int, status: string): void {
    // 暂不处理
  }
}

/**
 * 发送消息回调实现
 */
class EMSendCallBack extends com.hyphenate.EMCallBack {
  constructor() {
    super()
  }

  override onSuccess(): void {
    const callback = gSendSuccess
    if (callback != null) {
      callback()
    }
    gSendSuccess = null
    gSendFail = null
  }

  override onError(code: Int, error: string): void {
    const callback = gSendFail
    if (callback != null) {
      callback(code as number, error)
    }
    gSendSuccess = null
    gSendFail = null
  }

  override onProgress(progress: Int, status: string): void {
    // 暂不处理
  }
}

/**
 * 消息监听器实现 - extends 抽象类 EMMessageListener
 * 根据编译错误: onMessageReceived 参数类型是 (Mutable)List<EMMessage!>!
 */
class EMMessageListenerImpl extends com.hyphenate.EMMessageListener {
  constructor() {
    super()
  }

  // 必须实现的抽象方法
  // Kotlin 映射签名: fun onMessageReceived(p0: (Mutable)List<EMMessage!>!): Unit
  override onMessageReceived(messages: MutableList<com.hyphenate.chat.EMMessage>): void {
    const callback = gMsgCallback
    if (callback == null) return

    for (let i = 0; i < messages.size; i++) {
      const msg = messages.get(i)
      if (msg == null) continue

      try {
        const body = msg.getBody()
        let content = ''

        // 判断是否是文本消息
        if (body instanceof com.hyphenate.chat.EMTextMessageBody) {
          content = (body as com.hyphenate.chat.EMTextMessageBody).getMessage()
        }

        const message: EMMessage = {
          messageId: msg.getMsgId(),
          from: msg.getFrom(),
          to: msg.getTo(),
          content: content,
          timestamp: msg.getMsgTime()
        }

        callback(message)
      } catch (e) {
        console.error('[EM] process message error:', e)
      }
    }
  }
}

// 全局监听器实例
let gMsgListener: EMMessageListenerImpl | null = null

/**
 * 初始化SDK
 */
export function init(appKey: string): boolean {
  try {
    const context = UTSAndroid.getAppContext()
    if (context == null) {
      console.error('[EM] getAppContext is null')
      return false
    }

    const options = new com.hyphenate.chat.EMOptions()
    options.setAppKey(appKey)

    com.hyphenate.chat.EMClient.getInstance().init(context, options)
    gInited = true

    console.log('[EM] init success')
    return true
  } catch (e) {
    console.error('[EM] init failed:', e)
    return false
  }
}

/**
 * 登录
 */
@UTSJS.keepAlive
export function login(
  username: string,
  password: string,
  onSuccess: EMLoginSuccess,
  onFail: EMLoginFail
): void {
  if (gInited == false) {
    onFail(-1, 'SDK not initialized')
    return
  }

  try {
    gLoginSuccess = onSuccess
    gLoginFail = onFail
    com.hyphenate.chat.EMClient.getInstance().login(username, password, new EMLoginCallBack())
  } catch (e) {
    gLoginSuccess = null
    gLoginFail = null
    const errorMsg = e != null ? (e as Error).message : 'unknown error'
    onFail(-1, errorMsg)
  }
}

/**
 * 登出
 */
@UTSJS.keepAlive
export function logout(onSuccess: () => void): void {
  if (gLogined == false) {
    onSuccess()
    return
  }

  try {
    gLogoutSuccess = onSuccess
    com.hyphenate.chat.EMClient.getInstance().logout(true, new EMLogoutCallBack())
  } catch (e) {
    gLogoutSuccess = null
    gLogined = false
    onSuccess()
  }
}

/**
 * 发送文本消息
 */
@UTSJS.keepAlive
export function sendTextMessage(
  to: string,
  content: string,
  onSuccess: EMSendSuccess,
  onFail: EMSendFail
): void {
  if (gLogined == false) {
    onFail(-1, 'Not logged in')
    return
  }

  try {
    const msg = com.hyphenate.chat.EMMessage.createTxtSendMessage(content, to)
    if (msg == null) {
      onFail(-1, 'Create message failed')
      return
    }

    gSendSuccess = onSuccess
    gSendFail = onFail
    msg.setMessageStatusCallback(new EMSendCallBack())
    com.hyphenate.chat.EMClient.getInstance().chatManager().sendMessage(msg)
  } catch (e) {
    gSendSuccess = null
    gSendFail = null
    const errorMsg = e != null ? (e as Error).message : 'unknown error'
    onFail(-1, errorMsg)
  }
}

/**
 * 设置消息监听
 */
export function onMessageReceived(callback: EMMessageCallback): void {
  if (gInited == false) return

  // 保存回调
  gMsgCallback = callback

  // 移除旧监听器
  if (gMsgListener != null) {
    com.hyphenate.chat.EMClient.getInstance().chatManager().removeMessageListener(gMsgListener)
  }

  // 创建新监听器
  gMsgListener = new EMMessageListenerImpl()
  com.hyphenate.chat.EMClient.getInstance().chatManager().addMessageListener(gMsgListener)
}

/**
 * 移除消息监听
 */
export function offMessageReceived(): void {
  if (gMsgListener != null) {
    com.hyphenate.chat.EMClient.getInstance().chatManager().removeMessageListener(gMsgListener)
    gMsgListener = null
    gMsgCallback = null
  }
}

/**
 * 是否已登录
 */
export function isLoggedIn(): boolean {
  return gLogined
}

/**
 * 是否已初始化
 */
export function isInitialized(): boolean {
  return gInited
}

/**
 * 获取SDK版本
 */
export function getVersion(): string {
  return com.hyphenate.chat.EMClient.VERSION
}
