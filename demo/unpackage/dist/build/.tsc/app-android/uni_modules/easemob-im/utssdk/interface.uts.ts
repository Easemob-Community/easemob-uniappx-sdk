/**
 * 环信IM SDK - 接口定义
 */

// 登录回调类型
export type EMLoginSuccess = () => void
export type EMLoginFail = (code: number, message: string) => void
export type EMSendSuccess = () => void
export type EMSendFail = (code: number, message: string) => void
export type EMMessageCallback = (message: EMMessage) => void

/**
 * 消息体
 */
export interface EMMessageBody {
  type: string
  content?: string
}

/**
 * 消息
 */
export interface EMMessage {
  messageId: string
  from: string
  to: string
  content: string
  timestamp: number
}

/**
 * 登录参数
 */
export interface EMLoginParam {
  username: string
  password: string
}
