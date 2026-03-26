/**
 * 环信IM SDK - 跨平台主入口
 * 自动根据平台导出对应的实现
 */

import { EaseMobIMPlugin } from './interface.uts'
import { EMErrorCode } from './interface.uts'

// ==================== 平台判断 ====================


import {
  init as androidInit,
  isSDKInitialized as androidIsInitialized,
  register as androidRegister,
  login as androidLogin,
  logout as androidLogout,
  getCurrentUser as androidGetCurrentUser,
  isUserLoggedIn as androidIsLoggedIn,
  getUserToken as androidGetToken,
  sendMessage as androidSendMessage,
  createTextMessage as androidCreateTextMessage,
  createImageMessage as androidCreateImageMessage,
  createVoiceMessage as androidCreateVoiceMessage,
  createFileMessage as androidCreateFileMessage,
  createCustomMessage as androidCreateCustomMessage,
  resendMessage as androidResendMessage,
  recallMessage as androidRecallMessage,
  deleteMessage as androidDeleteMessage,
  getMessageById as androidGetMessageById,
  loadMessages as androidLoadMessages,
  getAllConversations as androidGetAllConversations,
  getConversation as androidGetConversation,
  deleteConversation as androidDeleteConversation,
  getUnreadMessageCount as androidGetUnreadCount,
  markAllMessagesAsRead as androidMarkAllRead,
  markMessageAsRead as androidMarkMessageRead,
  addMessageListener as androidAddMessageListener,
  removeMessageListener as androidRemoveMessageListener,
  addConnectionListener as androidAddConnectionListener,
  removeConnectionListener as androidRemoveConnectionListener,
  addTokenWillExpireListener as androidAddTokenWillExpireListener,
  removeTokenWillExpireListener as androidRemoveTokenWillExpireListener,
  addTokenDidExpireListener as androidAddTokenDidExpireListener,
  removeTokenDidExpireListener as androidRemoveTokenDidExpireListener,
  renewToken as androidRenewToken,
  getVersion as androidGetVersion,
  setLogLevel as androidSetLogLevel
} from './app-android/index.uts'











































// ==================== 统一导出 ====================

/**
 * SDK实例
 */
export const im = {
  // 初始化
  init: (options: any) => {

    return androidInit(options)







  },
  
  isInitialized: (): boolean => {

    return androidIsInitialized()







  },
  
  // 用户管理
  register: (username: string, password: string, onSuccess?: any, onFail?: any) => {

    return androidRegister(username, password, onSuccess, onFail)




  },
  
  login: (params: any, onSuccess?: any, onFail?: any) => {

    return androidLogin(params, onSuccess, onFail)




  },
  
  logout: (unbindDeviceToken?: boolean, onSuccess?: any, onFail?: any) => {

    return androidLogout(unbindDeviceToken, onSuccess, onFail)




  },
  
  getCurrentUser: () => {

    return androidGetCurrentUser()




    return null
  },
  
  isLoggedIn: (): boolean => {

    return androidIsLoggedIn()




    return false
  },
  
  getToken: () => {

    return androidGetToken()




    return null
  },
  
  // 消息管理
  sendMessage: (param: any, onSuccess?: any, onFail?: any, onProgress?: any) => {

    return androidSendMessage(param, onSuccess, onFail, onProgress)




  },
  
  createTextMessage: (to: string, content: string, chatType?: any) => {

    return androidCreateTextMessage(to, content, chatType)




    return null
  },
  
  createImageMessage: (to: string, localPath: string, chatType?: any) => {

    return androidCreateImageMessage(to, localPath, chatType)




    return null
  },
  
  createVoiceMessage: (to: string, localPath: string, duration: number, chatType?: any) => {

    return androidCreateVoiceMessage(to, localPath, duration, chatType)




    return null
  },
  
  createFileMessage: (to: string, localPath: string, displayName: string, chatType?: any) => {

    return androidCreateFileMessage(to, localPath, displayName, chatType)




    return null
  },
  
  createCustomMessage: (to: string, event: string, params?: any, chatType?: any) => {

    return androidCreateCustomMessage(to, event, params, chatType)




    return null
  },
  
  resendMessage: (msgId: string, onSuccess?: any, onFail?: any) => {

    return androidResendMessage(msgId, onSuccess, onFail)




  },
  
  recallMessage: (msgId: string, onSuccess?: any, onFail?: any) => {

    return androidRecallMessage(msgId, onSuccess, onFail)




  },
  
  deleteMessage: (msgId: string, onSuccess?: any, onFail?: any) => {

    return androidDeleteMessage(msgId, onSuccess, onFail)




  },
  
  getMessageById: (msgId: string) => {

    return androidGetMessageById(msgId)




    return null
  },
  
  loadMessages: (conversationId: string, chatType: any, limit?: number, fromId?: string) => {

    return androidLoadMessages(conversationId, chatType, limit, fromId)




    return []
  },
  
  // 会话管理
  getAllConversations: () => {

    return androidGetAllConversations()




    return []
  },
  
  getConversation: (conversationId: string, chatType: any, createIfNotExists?: boolean) => {

    return androidGetConversation(conversationId, chatType, createIfNotExists)




    return null
  },
  
  deleteConversation: (conversationId: string, chatType: any, deleteMessages?: boolean, onSuccess?: any, onFail?: any) => {

    return androidDeleteConversation(conversationId, chatType, deleteMessages, onSuccess, onFail)




  },
  
  getUnreadMessageCount: (): number => {

    return androidGetUnreadCount()




    return 0
  },
  
  markAllMessagesAsRead: (conversationId?: string) => {

    return androidMarkAllRead(conversationId)




  },
  
  markMessageAsRead: (msgId: string) => {

    return androidMarkMessageRead(msgId)




  },
  
  // 事件监听
  addMessageListener: (listener: any) => {

    return androidAddMessageListener(listener)




  },
  
  removeMessageListener: (listener: any) => {

    return androidRemoveMessageListener(listener)




  },
  
  addConnectionListener: (listener: any) => {

    return androidAddConnectionListener(listener)




  },
  
  removeConnectionListener: (listener: any) => {

    return androidRemoveConnectionListener(listener)




  },
  
  addTokenWillExpireListener: (listener: any) => {

    return androidAddTokenWillExpireListener(listener)




  },
  
  removeTokenWillExpireListener: (listener: any) => {

    return androidRemoveTokenWillExpireListener(listener)




  },
  
  addTokenDidExpireListener: (listener: any) => {

    return androidAddTokenDidExpireListener(listener)




  },
  
  removeTokenDidExpireListener: (listener: any) => {

    return androidRemoveTokenDidExpireListener(listener)




  },
  
  // 其他
  renewToken: (token: string) => {

    return androidRenewToken(token)




  },
  
  getVersion: (): string => {

    return androidGetVersion()




    return ''
  },
  
  setLogLevel: (level: number) => {

    return androidSetLogLevel(level)




  }
}

/**
 * 消息类型枚举
 */
export const MessageType = {
  TEXT: 'txt' as const,
  IMAGE: 'image' as const,
  VOICE: 'voice' as const,
  VIDEO: 'video' as const,
  LOCATION: 'location' as const,
  FILE: 'file' as const,
  CMD: 'cmd' as const
}

/**
 * 聊天类型枚举
 */
export const ChatType = {
  CHAT: 'chat' as const,
  GROUP_CHAT: 'groupChat' as const,
  CHAT_ROOM: 'chatRoom' as const
}

/**
 * 插件默认导出
 */
export default {
  im,
  ErrorCode: EMErrorCode,
  MessageType,
  ChatType
} as EaseMobIMPlugin
