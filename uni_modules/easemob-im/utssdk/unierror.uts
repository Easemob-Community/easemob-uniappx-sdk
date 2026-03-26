/**
 * 环信IM SDK - 错误码定义文件
 * 实现 interface.uts 中定义的错误类型
 */

import { EMErrorCode } from './interface.uts'

/**
 * 获取错误码对应的错误描述
 * @param code 错误码
 * @returns 错误描述
 */
export function getErrorMessage(code: EMErrorCode): string {
  switch (code) {
    // 通用错误
    case EMErrorCode.GENERAL_ERROR:
      return '通用错误'
    case EMErrorCode.NETWORK_ERROR:
      return '网络错误'
    case EMErrorCode.DATABASE_ERROR:
      return '数据库错误'
    
    // 登录相关
    case EMErrorCode.INVALID_CREDENTIALS:
      return '无效的凭证'
    case EMErrorCode.USER_NOT_FOUND:
      return '用户不存在'
    case EMErrorCode.USER_ALREADY_EXISTS:
      return '用户已存在'
    case EMErrorCode.USER_AUTHENTICATION_FAILED:
      return '用户认证失败'
    case EMErrorCode.USER_LOGIN_ANOTHER_DEVICE:
      return '用户在其他设备登录'
    case EMErrorCode.USER_REMOVED:
      return '用户已被移除'
    case EMErrorCode.USER_KICKED_BY_CHANGE_PASSWORD:
      return '密码修改导致被踢出'
    case EMErrorCode.USER_KICKED_BY_OTHER_DEVICE:
      return '被其他设备踢出'
    
    // 消息相关
    case EMErrorCode.MESSAGE_SEND_FAILED:
      return '消息发送失败'
    case EMErrorCode.MESSAGE_DELETE_FAILED:
      return '消息删除失败'
    case EMErrorCode.MESSAGE_UPDATE_FAILED:
      return '消息更新失败'
    
    // 连接相关
    case EMErrorCode.CONNECTION_FAILED:
      return '连接失败'
    case EMErrorCode.SERVER_NOT_REACHABLE:
      return '服务器不可达'
    case EMErrorCode.SERVER_TIMEOUT:
      return '服务器超时'
    case EMErrorCode.SERVER_BUSY:
      return '服务器繁忙'
    
    // Token相关
    case EMErrorCode.TOKEN_EXPIRED:
      return 'Token已过期'
    case EMErrorCode.TOKEN_WILL_EXPIRE:
      return 'Token即将过期'
    
    // 参数错误
    case EMErrorCode.INVALID_PARAMS:
      return '无效的参数'
    case EMErrorCode.INVALID_USERNAME:
      return '无效的用户名'
    case EMErrorCode.INVALID_PASSWORD:
      return '无效的密码'
    
    default:
      return '未知错误'
  }
}

/**
 * 创建错误对象
 * @param code 错误码
 * @param customMessage 自定义错误信息（可选）
 * @returns 错误对象
 */
export function createError(code: EMErrorCode, customMessage?: string): Error {
  const message = customMessage || getErrorMessage(code)
  return new Error(`[EMError:${code}] ${message}`)
}
