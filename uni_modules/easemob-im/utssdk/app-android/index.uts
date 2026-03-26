/**
 * 环信IM SDK - Android平台实现
 * 基于环信Android原生SDK进行UTS桥接
 */

import {
  EMOptions,
  EMLoginParam,
  EMUserInfo,
  EMMessage,
  EMSendMessageParam,
  EMMessageBody,
  EMTextMessageBody,
  EMImageMessageBody,
  EMVoiceMessageBody,
  EMFileMessageBody,
  EMCustomMessageBody,
  EMConversation,
  EMChatType,
  EMMessageType,
  EMMessageStatus,
  EMConnectionState,
  EMError,
  EMErrorCode,
  EMSuccessCallback,
  EMFailCallback,
  EMProgressCallback,
  EMMessageListener,
  EMConnectionListener,
  EMTokenWillExpireListener,
  EMTokenDidExpireListener
} from '../interface.uts'

import { getErrorMessage } from '../unierror.uts'

// ==================== 原生SDK类导入 ====================
// 注意：这些类型是环信Android SDK中的类，需要通过UTS编译器识别

type EMClient = any
type EMOptionsNative = any
type EMMessageNative = any
type EMTextMessageBodyNative = any
type EMImageMessageBodyNative = any
type EMVoiceMessageBodyNative = any
type EMFileMessageBodyNative = any
type EMCmdMessageBodyNative = any
type EMConversationNative = any
type EMCallBack = any
type EMValueCallBack<T> = any
type MessageListener = any
type ConnectionListener = any

// ==================== 全局变量 ====================

/** SDK客户端实例 */
let emClient: EMClient | null = null

/** 是否已初始化 */
let isInitialized = false

/** 是否已登录 */
let isLoggedIn = false

/** 当前用户信息 */
let currentUser: EMUserInfo | null = null

/** 监听器集合 */
const messageListeners = new Array<EMMessageListener>()
const connectionListeners = new Array<EMConnectionListener>()
const tokenWillExpireListeners = new Array<EMTokenWillExpireListener>()
const tokenDidExpireListeners = new Array<EMTokenDidExpireListener>()

/** 消息缓存 Map<msgId, EMMessage> */
const messageCache = new Map<string, EMMessage>()

/** 原生监听器引用 */
let nativeMessageListener: MessageListener | null = null
let nativeConnectionListener: ConnectionListener | null = null

// ==================== 初始化 ====================

/**
 * 初始化SDK
 */
export function init(options: EMOptions): void {
  if (isInitialized) {
    console.warn('[EaseMobIM] SDK already initialized')
    return
  }
  
  try {
    // 获取应用上下文
    const context = UTSAndroid.getAppContext()
    if (context == null) {
      throw new Error('Failed to get application context')
    }
    
    // 创建原生EMOptions
    const nativeOptions = new com.hyphenate.chat.EMOptions()
    nativeOptions.setAppKey(options.appKey)
    
    // 配置自动登录
    if (options.autoLogin !== undefined) {
      nativeOptions.setAutoLogin(options.autoLogin)
    }
    
    // 配置调试模式
    if (options.debugMode === true) {
      com.hyphenate.chat.EMClient.getInstance().setDebugMode(true)
    }
    
    // 配置私有部署
    if (options.usePrivateServer === true) {
      nativeOptions.enableDNSConfig(false)
      if (options.restServer != null) {
        nativeOptions.setRestServer(options.restServer)
      }
      if (options.imServer != null) {
        nativeOptions.setIMServer(options.imServer)
      }
      if (options.imPort != null) {
        nativeOptions.setImPort(options.imPort)
      }
    }
    
    // 初始化SDK
    com.hyphenate.chat.EMClient.getInstance().init(context, nativeOptions)
    emClient = com.hyphenate.chat.EMClient.getInstance()
    isInitialized = true
    
    // 注册原生监听器
    registerNativeListeners()
    
    console.log('[EaseMobIM] SDK initialized successfully')
  } catch (e) {
    console.error('[EaseMobIM] SDK init failed:', e)
    throw new Error(`SDK initialization failed: ${e}`)
  }
}

/**
 * 检查SDK是否已初始化
 */
export function isSDKInitialized(): boolean {
  return isInitialized
}

/**
 * 注册原生监听器
 */
function registerNativeListeners(): void {
  if (emClient == null) return
  
  // 注册消息监听器
  nativeMessageListener = new com.hyphenate.EMMessageListener({
    onMessageReceived: (messages: any[]) => {
      for (const nativeMsg of messages) {
        const msg = convertNativeMessage(nativeMsg)
        messageCache.set(msg.msgId, msg)
        // 触发前端监听器
        for (const listener of messageListeners) {
          listener(msg)
        }
      }
    },
    onCmdMessageReceived: (messages: any[]) => {
      // 处理命令消息
      for (const nativeMsg of messages) {
        const msg = convertNativeMessage(nativeMsg)
        for (const listener of messageListeners) {
          listener(msg)
        }
      }
    },
    onMessageRead: (messages: any[]) => {
      // 消息已读状态变更
    },
    onMessageDelivered: (messages: any[]) => {
      // 消息送达状态变更
    },
    onMessageRecalled: (messages: any[]) => {
      // 消息被撤回
    },
    onMessageChanged: (message: any, change: any) => {
      // 消息内容变更
    }
  })
  
  emClient.chatManager().addMessageListener(nativeMessageListener)
  
  // 注册连接状态监听器
  nativeConnectionListener = new com.hyphenate.EMConnectionListener({
    onConnected: () => {
      for (const listener of connectionListeners) {
        listener('connected' as EMConnectionState)
      }
    },
    onDisconnected: (errorCode: number) => {
      let state: EMConnectionState = 'disconnected'
      const error: EMError = {
        code: errorCode,
        message: getErrorMessage(errorCode as EMErrorCode)
      }
      
      // 处理特定错误码
      if (errorCode == 206) {
        // 用户被踢
        isLoggedIn = false
      }
      
      for (const listener of connectionListeners) {
        listener(state, error)
      }
    },
    onTokenExpired: () => {
      for (const listener of tokenDidExpireListeners) {
        listener()
      }
    },
    onTokenWillExpire: () => {
      for (const listener of tokenWillExpireListeners) {
        listener()
      }
    }
  })
  
  emClient.addConnectionListener(nativeConnectionListener)
}

// ==================== 用户管理 ====================

/**
 * 用户注册
 */
export function register(
  username: string,
  password: string,
  onSuccess?: EMSuccessCallback,
  onFail?: EMFailCallback
): void {
  if (!checkInitialized(onFail)) return
  
  try {
    // 注册是同步方法，在子线程执行
    uni.$offThread(() => {
      try {
        emClient!!.createAccount(username, password)
        uni.$onThread(() => {
          console.log('[EaseMobIM] Register success:', username)
          onSuccess?.()
        })
      } catch (e) {
        uni.$onThread(() => {
          console.error('[EaseMobIM] Register failed:', e)
          const errorCode = extractErrorCode(e)
          onFail?.(errorCode, getErrorMessage(errorCode as EMErrorCode))
        })
      }
    })
  } catch (e) {
    console.error('[EaseMobIM] Register exception:', e)
    onFail?.(EMErrorCode.GENERAL_ERROR, String(e))
  }
}

/**
 * 用户登录
 */
export function login(
  params: EMLoginParam,
  onSuccess?: EMSuccessCallback,
  onFail?: EMFailCallback
): void {
  if (!checkInitialized(onFail)) return
  
  const username = params.username
  const password = params.password
  const token = params.token
  
  if (token != null && token.length > 0) {
    // Token登录
    loginWithToken(username, token, onSuccess, onFail)
  } else if (password != null && password.length > 0) {
    // 密码登录
    loginWithPassword(username, password, onSuccess, onFail)
  } else {
    onFail?.(EMErrorCode.INVALID_PARAMS, 'Password or token is required')
  }
}

/**
 * 使用密码登录
 */
function loginWithPassword(
  username: string,
  password: string,
  onSuccess?: EMSuccessCallback,
  onFail?: EMFailCallback
): void {
  emClient!!.login(username, password, new com.hyphenate.EMCallBack({
    onSuccess: () => {
      isLoggedIn = true
      currentUser = {
        userId: username,
        nickname: username
      }
      // 加载本地会话和群组
      emClient!!.chatManager().loadAllConversations()
      emClient!!.groupManager().loadAllGroups()
      console.log('[EaseMobIM] Login success:', username)
      onSuccess?.()
    },
    onError: (code: number, error: string) => {
      console.error('[EaseMobIM] Login failed:', code, error)
      onFail?.(code, error)
    },
    onProgress: (progress: number, status: string) => {
      // 登录进度
    }
  }))
}

/**
 * 使用Token登录
 */
function loginWithToken(
  username: string,
  token: string,
  onSuccess?: EMSuccessCallback,
  onFail?: EMFailCallback
): void {
  emClient!!.loginWithToken(username, token, new com.hyphenate.EMCallBack({
    onSuccess: () => {
      isLoggedIn = true
      currentUser = {
        userId: username,
        nickname: username
      }
      emClient!!.chatManager().loadAllConversations()
      emClient!!.groupManager().loadAllGroups()
      console.log('[EaseMobIM] Login with token success:', username)
      onSuccess?.()
    },
    onError: (code: number, error: string) => {
      console.error('[EaseMobIM] Login with token failed:', code, error)
      onFail?.(code, error)
    },
    onProgress: (progress: number, status: string) => {
      // 登录进度
    }
  }))
}

/**
 * 用户登出
 */
export function logout(
  unbindDeviceToken: boolean = true,
  onSuccess?: EMSuccessCallback,
  onFail?: EMFailCallback
): void {
  if (!checkInitialized(onFail)) return
  if (!isLoggedIn) {
    onSuccess?.()
    return
  }
  
  emClient!!.logout(unbindDeviceToken, new com.hyphenate.EMCallBack({
    onSuccess: () => {
      isLoggedIn = false
      currentUser = null
      console.log('[EaseMobIM] Logout success')
      onSuccess?.()
    },
    onError: (code: number, error: string) => {
      console.error('[EaseMobIM] Logout failed:', code, error)
      onFail?.(code, error)
    },
    onProgress: (progress: number, status: string) => {
      // 登出进度
    }
  }))
}

/**
 * 获取当前登录用户
 */
export function getCurrentUser(): EMUserInfo | null {
  return currentUser
}

/**
 * 检查是否已登录
 */
export function isUserLoggedIn(): boolean {
  return isLoggedIn
}

/**
 * 获取当前Token
 */
export function getUserToken(): string | null {
  if (!isLoggedIn || emClient == null) return null
  return emClient.getAccessToken()
}

// ==================== 消息管理 ====================

/**
 * 发送消息
 */
export function sendMessage(
  param: EMSendMessageParam,
  onSuccess?: (message: EMMessage) => void,
  onFail?: EMFailCallback,
  onProgress?: EMProgressCallback
): void {
  if (!checkLogin(onFail)) return
  
  try {
    const nativeMsg = createNativeMessage(param)
    if (nativeMsg == null) {
      onFail?.(EMErrorCode.INVALID_PARAMS, 'Failed to create message')
      return
    }
    
    // 设置消息回调
    nativeMsg.setMessageStatusCallback(new com.hyphenate.EMCallBack({
      onSuccess: () => {
        const msg = convertNativeMessage(nativeMsg)
        messageCache.set(msg.msgId, msg)
        onSuccess?.(msg)
      },
      onError: (code: number, error: string) => {
        onFail?.(code, error)
      },
      onProgress: (progress: number, status: string) => {
        onProgress?.(progress)
      }
    }))
    
    // 发送消息
    emClient!!.chatManager().sendMessage(nativeMsg)
  } catch (e) {
    console.error('[EaseMobIM] Send message failed:', e)
    onFail?.(EMErrorCode.MESSAGE_SEND_FAILED, String(e))
  }
}

/**
 * 创建文本消息发送参数
 */
export function createTextMessage(
  to: string,
  content: string,
  chatType: EMChatType = 'chat'
): EMSendMessageParam {
  return {
    to: to,
    chatType: chatType,
    body: {
      type: 'txt',
      content: content
    } as EMTextMessageBody
  }
}

/**
 * 创建图片消息发送参数
 */
export function createImageMessage(
  to: string,
  localPath: string,
  chatType: EMChatType = 'chat'
): EMSendMessageParam {
  return {
    to: to,
    chatType: chatType,
    body: {
      type: 'image',
      localPath: localPath
    } as EMImageMessageBody
  }
}

/**
 * 创建语音消息发送参数
 */
export function createVoiceMessage(
  to: string,
  localPath: string,
  duration: number,
  chatType: EMChatType = 'chat'
): EMSendMessageParam {
  return {
    to: to,
    chatType: chatType,
    body: {
      type: 'voice',
      localPath: localPath,
      duration: duration
    } as EMVoiceMessageBody
  }
}

/**
 * 创建文件消息发送参数
 */
export function createFileMessage(
  to: string,
  localPath: string,
  displayName: string,
  chatType: EMChatType = 'chat'
): EMSendMessageParam {
  return {
    to: to,
    chatType: chatType,
    body: {
      type: 'file',
      localPath: localPath,
      displayName: displayName
    } as EMFileMessageBody
  }
}

/**
 * 创建自定义消息发送参数
 */
export function createCustomMessage(
  to: string,
  event: string,
  params: UTSJSONObject | undefined = undefined,
  chatType: EMChatType = 'chat'
): EMSendMessageParam {
  return {
    to: to,
    chatType: chatType,
    body: {
      type: 'custom',
      event: event,
      params: params
    } as EMCustomMessageBody
  }
}

/**
 * 创建原生消息对象
 */
function createNativeMessage(param: EMSendMessageParam): any | null {
  const chatType = param.chatType == 'groupChat' 
    ? com.hyphenate.chat.EMMessage.ChatType.GroupChat 
    : (param.chatType == 'chatRoom' 
      ? com.hyphenate.chat.EMMessage.ChatType.ChatRoom 
      : com.hyphenate.chat.EMMessage.ChatType.Chat)
  
  let nativeMsg: any = null
  const body = param.body
  
  switch (body.type) {
    case 'txt':
      const textBody = body as EMTextMessageBody
      nativeMsg = com.hyphenate.chat.EMMessage.createTxtSendMessage(textBody.content, param.to)
      break
    case 'image':
      const imageBody = body as EMImageMessageBody
      nativeMsg = com.hyphenate.chat.EMMessage.createImageSendMessage(
        imageBody.localPath || '',
        false, // 是否发送原图
        param.to
      )
      break
    case 'voice':
      const voiceBody = body as EMVoiceMessageBody
      nativeMsg = com.hyphenate.chat.EMMessage.createVoiceSendMessage(
        voiceBody.localPath || '',
        voiceBody.duration,
        param.to
      )
      break
    case 'file':
      const fileBody = body as EMFileMessageBody
      nativeMsg = com.hyphenate.chat.EMMessage.createFileSendMessage(
        fileBody.localPath || '',
        param.to
      )
      break
    case 'custom':
      const customBody = body as EMCustomMessageBody
      nativeMsg = com.hyphenate.chat.EMMessage.createSendMessage(
        com.hyphenate.chat.EMMessage.Type.CMD
      )
      const cmdBody = new com.hyphenate.chat.EMCmdMessageBody(customBody.event)
      if (customBody.params != null) {
        // 设置扩展参数
        const keys = Object.keys(customBody.params)
        for (const key of keys) {
          cmdBody.setParams(key, String(customBody.params[key]))
        }
      }
      nativeMsg.addBody(cmdBody)
      nativeMsg.setTo(param.to)
      break
  }
  
  if (nativeMsg != null) {
    nativeMsg.setChatType(chatType)
  }
  
  return nativeMsg
}

/**
 * 将原生消息转换为UTS消息
 */
function convertNativeMessage(nativeMsg: any): EMMessage {
  const type = convertNativeMessageType(nativeMsg.getType())
  const chatType = convertNativeChatType(nativeMsg.chatType())
  
  // 解析消息体
  let body: EMTextMessageBody | EMImageMessageBody | EMVoiceMessageBody | EMFileMessageBody | EMCustomMessageBody
  const nativeBody = nativeMsg.getBody()
  
  switch (type) {
    case 'txt':
      const txtBody = nativeBody as com.hyphenate.chat.EMTextMessageBody
      body = {
        type: 'txt',
        content: txtBody.getMessage()
      } as EMTextMessageBody
      break
    case 'image':
      const imgBody = nativeBody as com.hyphenate.chat.EMImageMessageBody
      body = {
        type: 'image',
        localPath: imgBody.getLocalUrl(),
        remoteUrl: imgBody.getRemoteUrl(),
        thumbnailLocalPath: imgBody.thumbnailLocalPath(),
        thumbnailRemoteUrl: imgBody.thumbnailUrl(),
        width: imgBody.getWidth(),
        height: imgBody.getHeight(),
        fileSize: imgBody.getFileSize(),
        displayName: imgBody.getFileName()
      } as EMImageMessageBody
      break
    case 'voice':
      const voiceBody = nativeBody as com.hyphenate.chat.EMVoiceMessageBody
      body = {
        type: 'voice',
        localPath: voiceBody.getLocalUrl(),
        remoteUrl: voiceBody.getRemoteUrl(),
        duration: voiceBody.getLength(),
        fileSize: voiceBody.getFileSize(),
        displayName: voiceBody.getFileName()
      } as EMVoiceMessageBody
      break
    case 'file':
      const fileBody = nativeBody as com.hyphenate.chat.EMNormalFileMessageBody
      body = {
        type: 'file',
        localPath: fileBody.getLocalUrl(),
        remoteUrl: fileBody.getRemoteUrl(),
        fileSize: fileBody.getFileSize(),
        displayName: fileBody.getFileName()
      } as EMFileMessageBody
      break
    default:
      body = {
        type: 'txt',
        content: ''
      } as EMTextMessageBody
  }
  
  return {
    msgId: nativeMsg.getMsgId(),
    type: type,
    chatType: chatType,
    from: nativeMsg.getFrom(),
    to: nativeMsg.getTo(),
    timestamp: nativeMsg.getMsgTime(),
    localTime: nativeMsg.localTime(),
    isSender: nativeMsg.direct() == com.hyphenate.chat.EMMessage.Direct.SEND,
    status: convertNativeStatus(nativeMsg.status()),
    isRead: nativeMsg.isAcked(),
    body: body
  }
}

/**
 * 转换原生消息类型
 */
function convertNativeMessageType(nativeType: any): EMMessageType {
  if (nativeType == com.hyphenate.chat.EMMessage.Type.TXT) return 'txt'
  if (nativeType == com.hyphenate.chat.EMMessage.Type.IMAGE) return 'image'
  if (nativeType == com.hyphenate.chat.EMMessage.Type.VOICE) return 'voice'
  if (nativeType == com.hyphenate.chat.EMMessage.Type.VIDEO) return 'video'
  if (nativeType == com.hyphenate.chat.EMMessage.Type.LOCATION) return 'location'
  if (nativeType == com.hyphenate.chat.EMMessage.Type.FILE) return 'file'
  if (nativeType == com.hyphenate.chat.EMMessage.Type.CMD) return 'cmd'
  return 'txt'
}

/**
 * 转换原生聊天类型
 */
function convertNativeChatType(nativeType: any): EMChatType {
  if (nativeType == com.hyphenate.chat.EMMessage.ChatType.GroupChat) return 'groupChat'
  if (nativeType == com.hyphenate.chat.EMMessage.ChatType.ChatRoom) return 'chatRoom'
  return 'chat'
}

/**
 * 转换原生消息状态
 */
function convertNativeStatus(nativeStatus: any): EMMessageStatus {
  if (nativeStatus == com.hyphenate.chat.EMMessage.Status.CREATE) return 'pending'
  if (nativeStatus == com.hyphenate.chat.EMMessage.Status.INPROGRESS) return 'sending'
  if (nativeStatus == com.hyphenate.chat.EMMessage.Status.SUCCESS) return 'success'
  if (nativeStatus == com.hyphenate.chat.EMMessage.Status.FAIL) return 'fail'
  return 'pending'
}

/**
 * 重发消息
 */
export function resendMessage(
  msgId: string,
  onSuccess?: (message: EMMessage) => void,
  onFail?: EMFailCallback
): void {
  if (!checkLogin(onFail)) return
  
  const msg = messageCache.get(msgId)
  if (msg == null) {
    onFail?.(EMErrorCode.MESSAGE_SEND_FAILED, 'Message not found')
    return
  }
  
  // 重新发送
  const nativeMsg = createNativeMessage({
    to: msg.to,
    chatType: msg.chatType,
    body: msg.body
  })
  
  if (nativeMsg == null) {
    onFail?.(EMErrorCode.MESSAGE_SEND_FAILED, 'Failed to recreate message')
    return
  }
  
  nativeMsg.setMessageStatusCallback(new com.hyphenate.EMCallBack({
    onSuccess: () => {
      const result = convertNativeMessage(nativeMsg)
      onSuccess?.(result)
    },
    onError: (code: number, error: string) => {
      onFail?.(code, error)
    },
    onProgress: (progress: number, status: string) => {}
  }))
  
  emClient!!.chatManager().sendMessage(nativeMsg)
}

/**
 * 撤回消息
 */
export function recallMessage(
  msgId: string,
  onSuccess?: EMSuccessCallback,
  onFail?: EMFailCallback
): void {
  if (!checkLogin(onFail)) return
  
  const conversation = emClient!!.chatManager().getConversation(msgId)
  if (conversation == null) {
    onFail?.(EMErrorCode.MESSAGE_SEND_FAILED, 'Message not found')
    return
  }
  
  const msg = conversation.getMessage(msgId, true)
  if (msg == null) {
    onFail?.(EMErrorCode.MESSAGE_SEND_FAILED, 'Message not found')
    return
  }
  
  emClient!!.chatManager().recallMessage(msg, new com.hyphenate.EMCallBack({
    onSuccess: () => {
      onSuccess?.()
    },
    onError: (code: number, error: string) => {
      onFail?.(code, error)
    },
    onProgress: (progress: number, status: string) => {}
  }))
}

/**
 * 删除消息
 */
export function deleteMessage(
  msgId: string,
  onSuccess?: EMSuccessCallback,
  onFail?: EMFailCallback
): void {
  if (!checkLogin(onFail)) return
  
  // 需要找到消息所在的会话
  const conversations = emClient!!.chatManager().getAllConversations()
  for (const conv of conversations) {
    const msg = conv.getMessage(msgId, false)
    if (msg != null) {
      conv.removeMessage(msgId)
      onSuccess?.()
      return
    }
  }
  
  onFail?.(EMErrorCode.MESSAGE_DELETE_FAILED, 'Message not found')
}

/**
 * 根据ID获取消息
 */
export function getMessageById(msgId: string): EMMessage | null {
  if (!isLoggedIn || emClient == null) return null
  
  // 先查缓存
  const cached = messageCache.get(msgId)
  if (cached != null) return cached
  
  // 从会话中查找
  const conversations = emClient.chatManager().getAllConversations()
  for (const conv of conversations) {
    const msg = conv.getMessage(msgId, true)
    if (msg != null) {
      return convertNativeMessage(msg)
    }
  }
  
  return null
}

/**
 * 加载会话中的消息
 */
export function loadMessages(
  conversationId: string,
  chatType: EMChatType,
  limit: number = 20,
  fromId: string | undefined = undefined
): EMMessage[] {
  if (!isLoggedIn || emClient == null) return []
  
  const type = chatType == 'groupChat' 
    ? com.hyphenate.chat.EMConversation.EMConversationType.GroupChat 
    : (chatType == 'chatRoom' 
      ? com.hyphenate.chat.EMConversation.EMConversationType.ChatRoom 
      : com.hyphenate.chat.EMConversation.EMConversationType.Chat)
  
  const conversation = emClient.chatManager().getConversation(conversationId, type, true)
  if (conversation == null) return []
  
  let messages: any[]
  if (fromId != null && fromId.length > 0) {
    messages = conversation.loadMoreMsgFromDB(fromId, limit)
  } else {
    messages = conversation.getAllMessages()
    if (messages.length > limit) {
      messages = messages.slice(messages.length - limit)
    }
  }
  
  const result: EMMessage[] = []
  for (const nativeMsg of messages) {
    result.push(convertNativeMessage(nativeMsg))
  }
  
  return result
}

// ==================== 会话管理 ====================

/**
 * 获取所有会话
 */
export function getAllConversations(): EMConversation[] {
  if (!isLoggedIn || emClient == null) return []
  
  const nativeConvs = emClient.chatManager().getAllConversations()
  const result: EMConversation[] = []
  
  for (const nativeConv of nativeConvs) {
    result.push(convertNativeConversation(nativeConv))
  }
  
  return result
}

/**
 * 获取指定会话
 */
export function getConversation(
  conversationId: string,
  chatType: EMChatType,
  createIfNotExists: boolean = false
): EMConversation | null {
  if (!isLoggedIn || emClient == null) return null
  
  const type = chatType == 'groupChat' 
    ? com.hyphenate.chat.EMConversation.EMConversationType.GroupChat 
    : (chatType == 'chatRoom' 
      ? com.hyphenate.chat.EMConversation.EMConversationType.ChatRoom 
      : com.hyphenate.chat.EMConversation.EMConversationType.Chat)
  
  const nativeConv = emClient.chatManager().getConversation(conversationId, type, createIfNotExists)
  if (nativeConv == null) return null
  
  return convertNativeConversation(nativeConv)
}

/**
 * 删除会话
 */
export function deleteConversation(
  conversationId: string,
  chatType: EMChatType,
  deleteMessages: boolean = true,
  onSuccess?: EMSuccessCallback,
  onFail?: EMFailCallback
): void {
  if (!checkLogin(onFail)) return
  
  try {
    emClient!!.chatManager().deleteConversation(conversationId, deleteMessages)
    onSuccess?.()
  } catch (e) {
    onFail?.(EMErrorCode.GENERAL_ERROR, String(e))
  }
}

/**
 * 获取未读消息总数
 */
export function getUnreadMessageCount(): number {
  if (!isLoggedIn || emClient == null) return 0
  
  const conversations = emClient.chatManager().getAllConversations()
  let count = 0
  for (const conv of conversations) {
    count += conv.getUnreadMsgCount()
  }
  return count
}

/**
 * 标记所有消息为已读
 */
export function markAllMessagesAsRead(conversationId: string | undefined = undefined): void {
  if (!isLoggedIn || emClient == null) return
  
  if (conversationId != null && conversationId.length > 0) {
    // 标记指定会话
    const conversations = emClient.chatManager().getAllConversations()
    for (const conv of conversations) {
      if (conv.conversationId() == conversationId) {
        conv.markAllMessagesAsRead()
        break
      }
    }
  } else {
    // 标记所有会话
    const conversations = emClient.chatManager().getAllConversations()
    for (const conv of conversations) {
      conv.markAllMessagesAsRead()
    }
  }
}

/**
 * 标记指定消息为已读
 */
export function markMessageAsRead(msgId: string): void {
  if (!isLoggedIn || emClient == null) return
  
  const conversations = emClient.chatManager().getAllConversations()
  for (const conv of conversations) {
    const msg = conv.getMessage(msgId, false)
    if (msg != null) {
      conv.markMessageAsRead(msgId)
      break
    }
  }
}

/**
 * 转换原生会话为UTS会话
 */
function convertNativeConversation(nativeConv: any): EMConversation {
  const type = nativeConv.getType() == com.hyphenate.chat.EMConversation.EMConversationType.GroupChat 
    ? 'groupChat' 
    : (nativeConv.getType() == com.hyphenate.chat.EMConversation.EMConversationType.ChatRoom 
      ? 'chatRoom' 
      : 'chat')
  
  const lastMsg = nativeConv.getLastMessage()
  
  return {
    conversationId: nativeConv.conversationId(),
    type: type,
    unreadCount: nativeConv.getUnreadMsgCount(),
    lastMessage: lastMsg != null ? convertNativeMessage(lastMsg) : undefined,
    ext: nativeConv.getExtField()
  }
}

// ==================== 事件监听 ====================

/**
 * 添加消息接收监听器
 */
export function addMessageListener(listener: EMMessageListener): void {
  if (!messageListeners.includes(listener)) {
    messageListeners.push(listener)
  }
}

/**
 * 移除消息接收监听器
 */
export function removeMessageListener(listener: EMMessageListener): void {
  const index = messageListeners.indexOf(listener)
  if (index > -1) {
    messageListeners.splice(index, 1)
  }
}

/**
 * 添加连接状态监听器
 */
export function addConnectionListener(listener: EMConnectionListener): void {
  if (!connectionListeners.includes(listener)) {
    connectionListeners.push(listener)
  }
}

/**
 * 移除连接状态监听器
 */
export function removeConnectionListener(listener: EMConnectionListener): void {
  const index = connectionListeners.indexOf(listener)
  if (index > -1) {
    connectionListeners.splice(index, 1)
  }
}

/**
 * 添加Token即将过期监听器
 */
export function addTokenWillExpireListener(listener: EMTokenWillExpireListener): void {
  if (!tokenWillExpireListeners.includes(listener)) {
    tokenWillExpireListeners.push(listener)
  }
}

/**
 * 移除Token即将过期监听器
 */
export function removeTokenWillExpireListener(listener: EMTokenWillExpireListener): void {
  const index = tokenWillExpireListeners.indexOf(listener)
  if (index > -1) {
    tokenWillExpireListeners.splice(index, 1)
  }
}

/**
 * 添加Token已过期监听器
 */
export function addTokenDidExpireListener(listener: EMTokenDidExpireListener): void {
  if (!tokenDidExpireListeners.includes(listener)) {
    tokenDidExpireListeners.push(listener)
  }
}

/**
 * 移除Token已过期监听器
 */
export function removeTokenDidExpireListener(listener: EMTokenDidExpireListener): void {
  const index = tokenDidExpireListeners.indexOf(listener)
  if (index > -1) {
    tokenDidExpireListeners.splice(index, 1)
  }
}

// ==================== 其他 ====================

/**
 * 刷新Token
 */
export function renewToken(token: string): void {
  if (!isLoggedIn || emClient == null) return
  emClient.renewToken(token)
}

/**
 * 获取SDK版本号
 */
export function getVersion(): string {
  return com.hyphenate.chat.EMClient.VERSION
}

/**
 * 设置日志级别
 */
export function setLogLevel(level: number): void {
  if (emClient == null) return
  // 环信SDK使用boolean控制调试模式
  emClient.setDebugMode(level >= 4)
}

// ==================== 工具函数 ====================

/**
 * 检查SDK是否已初始化
 */
function checkInitialized(onFail?: EMFailCallback): boolean {
  if (!isInitialized) {
    onFail?.(EMErrorCode.GENERAL_ERROR, 'SDK not initialized')
    return false
  }
  return true
}

/**
 * 检查是否已登录
 */
function checkLogin(onFail?: EMFailCallback): boolean {
  if (!isInitialized) {
    onFail?.(EMErrorCode.GENERAL_ERROR, 'SDK not initialized')
    return false
  }
  if (!isLoggedIn) {
    onFail?.(EMErrorCode.USER_AUTHENTICATION_FAILED, 'User not logged in')
    return false
  }
  return true
}

/**
 * 从异常中提取错误码
 */
function extractErrorCode(error: any): number {
  if (error instanceof com.hyphenate.exceptions.HyphenateException) {
    return error.getErrorCode()
  }
  return EMErrorCode.GENERAL_ERROR
}
