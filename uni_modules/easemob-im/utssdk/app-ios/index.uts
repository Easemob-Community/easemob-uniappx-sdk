/**
 * 环信IM SDK - iOS简化版
 */

// 全局变量
let isInit = false
let isLogin = false

/**
 * 初始化SDK
 */
export function init(appKey: string): boolean {
  try {
    let options = EMOptions(appkey: appKey)
    EMClient.sharedClient.initializeSDK(with: options)
    isInit = true
    
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
export function login(username: string, password: string, onSuccess: any, onFail: any): void {
  if (!isInit) {
    if (onFail) onFail(-1, 'SDK not init')
    return
  }
  
  EMClient.sharedClient.login(withUsername: username, password: password) { user, error in
    if error != nil {
      if onFail != nil {
        onFail!(error!.code, error!.errorDescription ?? 'login failed')
      }
    } else {
      isLogin = true
      if onSuccess != nil {
        onSuccess!()
      }
    }
  }
}

/**
 * 登出
 */
export function logout(onSuccess: any): void {
  if (!isLogin) {
    if onSuccess != nil {
      onSuccess!()
    }
    return
  }
  
  EMClient.sharedClient.logout(true) { error in
    isLogin = false
    if onSuccess != nil {
      onSuccess!()
    }
  }
}

/**
 * 发送文本消息
 */
export function sendText(to: string, content: string, onSuccess: any, onFail: any): void {
  if (!isLogin) {
    if onFail != nil {
      onFail!(-1, 'not login')
    }
    return
  }
  
  let body = EMTextMessageBody(text: content)
  let msg = EMChatMessage(conversationID: to, body: body, ext: nil)
  
  EMClient.sharedClient.chatManager.send(msg, progress: nil) { message, error in
    if error != nil {
      if onFail != nil {
        onFail!(error!.code, error!.errorDescription ?? 'send failed')
      }
    } else {
      if onSuccess != nil {
        onSuccess!()
      }
    }
  }
}

/**
 * 添加消息监听
 */
export function onMessage(listener: any): void {
  if (!isInit) return
  
  // 使用通知中心简单实现
  // 实际项目中应该使用 EMChatManagerDelegate
}

/**
 * 是否已登录
 */
export function isLoggedIn(): boolean {
  return isLogin
}

/**
 * 获取SDK版本
 */
export function getVersion(): string {
  return EMClient.sharedClient.version
}
