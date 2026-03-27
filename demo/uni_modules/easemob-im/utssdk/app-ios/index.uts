/**
 * 环信IM SDK - iOS实现
 */

import { EMLoginSuccess, EMLoginFail, EMSendSuccess, EMSendFail, EMMessageCallback, EMMessage } from '../interface.uts'

// 全局状态
let gInited = false
let gLogined = false

/**
 * 初始化SDK
 */
export function init(appKey: string): boolean {
  console.log('[EM] iOS init:', appKey)
  // iOS 初始化实现
  gInited = true
  return true
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
  // iOS 登录实现
  gLogined = true
  onSuccess()
}

/**
 * 登出
 */
@UTSJS.keepAlive
export function logout(onSuccess: () => void): void {
  gLogined = false
  onSuccess()
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
  // iOS 发送消息实现
  onSuccess()
}

/**
 * 设置消息监听
 */
export function onMessageReceived(callback: EMMessageCallback): void {
  // iOS 消息监听实现
}

/**
 * 移除消息监听
 */
export function offMessageReceived(): void {
  // iOS 移除监听实现
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
  return '4.15.1'
}
