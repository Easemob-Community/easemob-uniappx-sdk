/**
 * 环信IM SDK - iOS平台实现
 * 基于环信iOS原生SDK进行UTS桥接
 */

import {
  EMOptions,
  EMLoginParam,
  EMUserInfo,
  EMMessage,
  EMSendMessageParam,
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

type EMClient = any
type EMOptionsNative = any
type EMMessageNative = any
type EMTextMessageBodyNative = any
type EMImageMessageBodyNative = any
type EMVoiceMessageBodyNative = any
type EMFileMessageBodyNative = any
type EMConversationNative = any

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
    // 创建原生EMOptions
    const nativeOptions = EMOptions(appkey: options.appKey)
    
    // 配置自动登录
    if (options.autoLogin !== undefined) {
      nativeOptions.isAutoLogin = options.autoLogin
    }
    
    // 配置调试模式
    if (options.debugMode === true) {
      EMClient.sharedClient.enableConsoleLog(true)
    }
    
    // 配置私有部署
    if (options.usePrivateServer === true) {
      nativeOptions.enableDnsConfig = false
      if (options.restServer != null) {
        nativeOptions.restServer = options.restServer
      }
      if (options.imServer != null) {
        nativeOptions.imServer = options.imServer
      }
      if (options.imPort != null) {
        nativeOptions.imPort = options.imPort
      }
    }
    
    // 初始化SDK
    EMClient.sharedClient.initializeSDK(with: nativeOptions)
    emClient = EMClient.sharedClient
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
  emClient!.chatManager.add(self, delegateQueue: DispatchQueue.main)
  
  // 注册连接状态监听器
  emClient!.add(self, delegateQueue: DispatchQueue.main)
}

// 实现消息监听器协议
class EaseMobMessageDelegate: EMChatManagerDelegate {
  func messagesDidReceive(_ aMessages: [EMChatMessage]) {
    for nativeMsg in aMessages {
      let msg = convertNativeMessage(nativeMsg)
      messageCache.set(msg.msgId, msg)
      // 触发前端监听器
      for listener in messageListeners {
        listener(msg)
      }
    }
  }
  
  func cmdMessagesDidReceive(_ aMessages: [EMChatMessage]) {
    for nativeMsg in aMessages {
      let msg = convertNativeMessage(nativeMsg)
      for listener in messageListeners {
        listener(msg)
      }
    }
  }
  
  func messagesDidRead(_ aMessages: [EMChatMessage]) {
    // 消息已读状态变更
  }
  
  func messagesDidDeliver(_ aMessages: [EMChatMessage]) {
    // 消息送达状态变更
  }
  
  func messagesDidRecall(_ aMessages: [EMChatMessage]) {
    // 消息被撤回
  }
  
  func messageStatusDidChange(_ aMessage: EMChatMessage, error: EMError?) {
    // 消息状态变更
  }
}

// 实现连接监听器协议
class EaseMobConnectionDelegate: EMClientDelegate {
  func connectionStateDidChange(_ aConnectionState: EMConnectionState) {
    let state: EMConnectionState
    switch aConnectionState {
      case .connected:
        state = 'connected'
      case .connecting:
        state = 'connecting'
      default:
        state = 'disconnected'
    }
    
    for listener in connectionListeners {
      listener(state)
    }
  }
  
  func userAccountDidLoginFromOtherDevice() {
    // 用户在其他设备登录
    isLoggedIn = false
    let error: EMError = {
      code: EMErrorCode.USER_LOGIN_ANOTHER_DEVICE,
      message: getErrorMessage(EMErrorCode.USER_LOGIN_ANOTHER_DEVICE)
    }
    for listener in connectionListeners {
      listener('disconnected', error)
    }
  }
  
  func userAccountDidRemoveFromServer() {
    // 用户被移除
    isLoggedIn = false
    let error: EMError = {
      code: EMErrorCode.USER_REMOVED,
      message: getErrorMessage(EMErrorCode.USER_REMOVED)
    }
    for listener in connectionListeners {
      listener('disconnected', error)
    }
  }
  
  func userDidForbidByServer() {
    // 用户被禁止
  }
  
  func userAccountDidForcedToLogout(_ aError: EMError?) {
    // 被强制登出
  }
  
  func tokenDidExpire() {
    for listener in tokenDidExpireListeners {
      listener()
    }
  }
  
  func tokenWillExpire() {
    for listener in tokenWillExpireListeners {
      listener()
    }
  }
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
  
  // iOS注册是异步的
  EMClient.sharedClient.register(withUsername: username, password: password) { [weak self] username, error in
    if let error = error {
      console.error('[EaseMobIM] Register failed:', error.code, error.errorDescription)
      onFail?.(error.code, error.errorDescription ?? 'Unknown error')
    } else {
      console.log('[EaseMobIM] Register success:', username)
      onSuccess?.()
    }
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
  
  let username = params.username
  let password = params.password
  let token = params.token
  
  if token != nil && token!.length > 0 {
    // Token登录
    loginWithToken(username: username, token: token!, onSuccess: onSuccess, onFail: onFail)
  } else if password != nil && password!.length > 0 {
    // 密码登录
    loginWithPassword(username: username, password: password!, onSuccess: onSuccess, onFail: onFail)
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
  EMClient.sharedClient.login(withUsername: username, password: password) { [weak self] username, error in
    if let error = error {
      console.error('[EaseMobIM] Login failed:', error.code, error.errorDescription)
      onFail?.(error.code, error.errorDescription ?? 'Login failed')
    } else {
      isLoggedIn = true
      currentUser = {
        userId: username,
        nickname: username
      }
      console.log('[EaseMobIM] Login success:', username)
      onSuccess?.()
    }
  }
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
  EMClient.sharedClient.login(withUsername: username, token: token) { [weak self] username, error in
    if let error = error {
      console.error('[EaseMobIM] Login with token failed:', error.code, error.errorDescription)
      onFail?.(error.code, error.errorDescription ?? 'Login failed')
    } else {
      isLoggedIn = true
      currentUser = {
        userId: username,
        nickname: username
      }
      console.log('[EaseMobIM] Login with token success:', username)
      onSuccess?.()
    }
  }
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
  if !isLoggedIn {
    onSuccess?.()
    return
  }
  
  EMClient.sharedClient.logout(unbindDeviceToken) { [weak self] error in
    if let error = error {
      console.error('[EaseMobIM] Logout failed:', error.code, error.errorDescription)
      onFail?.(error.code, error.errorDescription ?? 'Logout failed')
    } else {
      isLoggedIn = false
      currentUser = nil
      console.log('[EaseMobIM] Logout success')
      onSuccess?.()
    }
  }
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
  if !isLoggedIn || emClient == nil { return nil }
  return EMClient.sharedClient.accessToken
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
  
  do {
    guard let nativeMsg = createNativeMessage(param) else {
      onFail?.(EMErrorCode.INVALID_PARAMS, 'Failed to create message')
      return
    }
    
    // 发送消息
    EMClient.sharedClient.chatManager.send(nativeMsg, progress: { progress in
      onProgress?(Int(progress * 100))
    }, completion: { [weak self] message, error in
      if let error = error {
        onFail?.(error.code, error.errorDescription ?? 'Send failed')
      } else if let message = message {
        let msg = convertNativeMessage(message)
        messageCache.set(msg.msgId, msg)
        onSuccess?(msg)
      }
    })
  } catch {
    console.error('[EaseMobIM] Send message failed:', error)
    onFail?.(EMErrorCode.MESSAGE_SEND_FAILED, String(error))
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
function createNativeMessage(param: EMSendMessageParam): EMChatMessage? {
  let chatType: EMChatType
  switch param.chatType {
    case 'groupChat':
      chatType = EMChatType.groupChat
    case 'chatRoom':
      chatType = EMChatType.chatRoom
    default:
      chatType = EMChatType.chat
  }
  
  let nativeBody: EMMessageBody
  let body = param.body
  
  switch body.type {
    case 'txt':
      let textBody = body as! EMTextMessageBody
      nativeBody = EMTextMessageBody(text: textBody.content)
    case 'image':
      let imageBody = body as! EMImageMessageBody
      let data = NSData(contentsOfFile: imageBody.localPath ?? '')
      nativeBody = EMImageMessageBody(data: data, displayName: (imageBody.localPath as NSString).lastPathComponent)
    case 'voice':
      let voiceBody = body as! EMVoiceMessageBody
      nativeBody = EMVoiceMessageBody(localPath: voiceBody.localPath, displayName: (voiceBody.localPath as NSString).lastPathComponent)
      (nativeBody as! EMVoiceMessageBody).duration = Int32(voiceBody.duration)
    case 'file':
      let fileBody = body as! EMFileMessageBody
      nativeBody = EMFileMessageBody(localPath: fileBody.localPath, displayName: fileBody.displayName)
    case 'custom':
      let customBody = body as! EMCustomMessageBody
      nativeBody = EMCmdMessageBody(action: customBody.event)
      if let params = customBody.params {
        // 设置扩展参数
        (nativeBody as! EMCmdMessageBody).params = params as! [String: String]
      }
    default:
      return nil
  }
  
  let nativeMsg = EMChatMessage(conversationID: param.to, body: nativeBody, ext: nil)
  nativeMsg.chatType = chatType
  
  return nativeMsg
}

/**
 * 将原生消息转换为UTS消息
 */
function convertNativeMessage(nativeMsg: EMChatMessage): EMMessage {
  let type = convertNativeMessageType(nativeMsg.body.type)
  let chatType: EMChatType
  switch nativeMsg.chatType {
    case .groupChat:
      chatType = 'groupChat'
    case .chatRoom:
      chatType = 'chatRoom'
    default:
      chatType = 'chat'
  }
  
  // 解析消息体
  var body: EMTextMessageBody | EMImageMessageBody | EMVoiceMessageBody | EMFileMessageBody | EMCustomMessageBody
  
  switch type {
    case 'txt':
      let txtBody = nativeMsg.body as! EMTextMessageBody
      body = {
        type: 'txt',
        content: txtBody.text
      } as EMTextMessageBody
    case 'image':
      let imgBody = nativeMsg.body as! EMImageMessageBody
      body = {
        type: 'image',
        localPath: imgBody.localPath,
        remoteUrl: imgBody.remotePath,
        thumbnailLocalPath: imgBody.thumbnailLocalPath,
        thumbnailRemoteUrl: imgBody.thumbnailRemotePath,
        width: CGFloat(imgBody.size.width),
        height: CGFloat(imgBody.size.height),
        fileSize: imgBody.fileLength,
        displayName: imgBody.displayName
      } as EMImageMessageBody
    case 'voice':
      let voiceBody = nativeMsg.body as! EMVoiceMessageBody
      body = {
        type: 'voice',
        localPath: voiceBody.localPath,
        remoteUrl: voiceBody.remotePath,
        duration: Int(voiceBody.duration),
        fileSize: voiceBody.fileLength,
        displayName: voiceBody.displayName
      } as EMVoiceMessageBody
    case 'file':
      let fileBody = nativeMsg.body as! EMFileMessageBody
      body = {
        type: 'file',
        localPath: fileBody.localPath,
        remoteUrl: fileBody.remotePath,
        fileSize: fileBody.fileLength,
        displayName: fileBody.displayName
      } as EMFileMessageBody
    default:
      body = {
        type: 'txt',
        content: ''
      } as EMTextMessageBody
  }
  
  return {
    msgId: nativeMsg.messageId,
    type: type,
    chatType: chatType,
    from: nativeMsg.from,
    to: nativeMsg.to,
    timestamp: Int(nativeMsg.timestamp),
    localTime: Int(nativeMsg.localTime),
    isSender: nativeMsg.direction == EMMessageDirection.send,
    status: convertNativeStatus(nativeMsg.status),
    isRead: nativeMsg.isRead,
    body: body
  }
}

/**
 * 转换原生消息类型
 */
function convertNativeMessageType(nativeType: EMMessageBodyType): EMMessageType {
  switch nativeType {
    case .text:
      return 'txt'
    case .image:
      return 'image'
    case .voice:
      return 'voice'
    case .video:
      return 'video'
    case .location:
      return 'location'
    case .file:
      return 'file'
    case .cmd:
      return 'cmd'
    default:
      return 'txt'
  }
}

/**
 * 转换原生消息状态
 */
function convertNativeStatus(nativeStatus: EMMessageStatus): EMMessageStatus {
  switch nativeStatus {
    case .pending:
      return 'pending'
    case .delivering:
      return 'sending'
    case .successed:
      return 'success'
    case .failed:
      return 'fail'
    default:
      return 'pending'
  }
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
  
  guard let msg = messageCache.get(msgId) else {
    onFail?.(EMErrorCode.MESSAGE_SEND_FAILED, 'Message not found')
    return
  }
  
  guard let nativeMsg = createNativeMessage({
    to: msg.to,
    chatType: msg.chatType,
    body: msg.body
  }) else {
    onFail?.(EMErrorCode.MESSAGE_SEND_FAILED, 'Failed to recreate message')
    return
  }
  
  EMClient.sharedClient.chatManager.resendMessage(nativeMsg, progress: nil) { [weak self] message, error in
    if let error = error {
      onFail?.(error.code, error.errorDescription ?? 'Resend failed')
    } else if let message = message {
      let result = convertNativeMessage(message)
      onSuccess?(result)
    }
  }
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
  
  EMClient.sharedClient.chatManager.recallMessage(withMessageId: msgId, completion: { [weak self] recallMessage, error in
    if let error = error {
      onFail?.(error.code, error.errorDescription ?? 'Recall failed')
    } else {
      onSuccess?()
    }
  })
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
  
  // 查找消息所在的会话
  let conversations = EMClient.sharedClient.chatManager.getAllConversations()
  for conv in conversations {
    if let msg = conv.loadMessage(withId: msgId, error: nil) {
      conv.deleteMessage(withId: msgId, error: nil)
      onSuccess?()
      return
    }
  }
  
  onFail?.(EMErrorCode.MESSAGE_DELETE_FAILED, 'Message not found')
}

/**
 * 根据ID获取消息
 */
export function getMessageById(msgId: string): EMMessage | null {
  if !isLoggedIn || emClient == nil { return nil }
  
  // 先查缓存
  if let cached = messageCache.get(msgId) {
    return cached
  }
  
  // 从会话中查找
  let conversations = EMClient.sharedClient.chatManager.getAllConversations()
  for conv in conversations {
    if let msg = conv.loadMessage(withId: msgId, error: nil) {
      return convertNativeMessage(msg)
    }
  }
  
  return nil
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
  if !isLoggedIn || emClient == nil { return [] }
  
  let type: EMConversationType
  switch chatType {
    case 'groupChat':
      type = EMConversationType.group
    case 'chatRoom':
      type = EMConversationType.chatRoom
    default:
      type = EMConversationType.chat
  }
  
  guard let conversation = EMClient.sharedClient.chatManager.getConversationWithConvId(conversationId) else {
    return []
  }
  
  var messages: [EMChatMessage]
  if let fromId = fromId, fromId.length > 0 {
    messages = conversation.loadMessagesStart(fromId: fromId, count: Int32(limit), searchDirection: EMMessageSearchDirection.up)
  } else {
    messages = conversation.loadMessages(withCount: Int32(limit), searchDirection: EMMessageSearchDirection.up)
  }
  
  var result: EMMessage[] = []
  for nativeMsg in messages {
    result.append(convertNativeMessage(nativeMsg))
  }
  
  return result
}

// ==================== 会话管理 ====================

/**
 * 获取所有会话
 */
export function getAllConversations(): EMConversation[] {
  if !isLoggedIn || emClient == nil { return [] }
  
  let nativeConvs = EMClient.sharedClient.chatManager.getAllConversations()
  var result: EMConversation[] = []
  
  for nativeConv in nativeConvs {
    result.append(convertNativeConversation(nativeConv))
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
  if !isLoggedIn || emClient == nil { return nil }
  
  let type: EMConversationType
  switch chatType {
    case 'groupChat':
      type = EMConversationType.group
    case 'chatRoom':
      type = EMConversationType.chatRoom
    default:
      type = EMConversationType.chat
  }
  
  guard let nativeConv = EMClient.sharedClient.chatManager.getConversation(
    conversationId,
    type: type,
    createIfNotExists: createIfNotExists
  ) else {
    return nil
  }
  
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
  
  do {
    try EMClient.sharedClient.chatManager.deleteConversation(conversationId, isDeleteMessages: deleteMessages)
    onSuccess?()
  } catch {
    onFail?.(EMErrorCode.GENERAL_ERROR, String(error))
  }
}

/**
 * 获取未读消息总数
 */
export function getUnreadMessageCount(): number {
  if !isLoggedIn || emClient == nil { return 0 }
  
  let conversations = EMClient.sharedClient.chatManager.getAllConversations()
  var count = 0
  for conv in conversations {
    count += Int(conv.unreadMessagesCount)
  }
  return count
}

/**
 * 标记所有消息为已读
 */
export function markAllMessagesAsRead(conversationId: string | undefined = undefined): void {
  if !isLoggedIn || emClient == nil { return }
  
  if let conversationId = conversationId, conversationId.length > 0 {
    // 标记指定会话
    if let conv = EMClient.sharedClient.chatManager.getConversationWithConvId(conversationId) {
      conv.markAllMessages(asRead: true)
    }
  } else {
    // 标记所有会话
    let conversations = EMClient.sharedClient.chatManager.getAllConversations()
    for conv in conversations {
      conv.markAllMessages(asRead: true)
    }
  }
}

/**
 * 标记指定消息为已读
 */
export function markMessageAsRead(msgId: string): void {
  if !isLoggedIn || emClient == nil { return }
  
  let conversations = EMClient.sharedClient.chatManager.getAllConversations()
  for conv in conversations {
    if let msg = conv.loadMessage(withId: msgId, error: nil) {
      msg.isRead = true
      break
    }
  }
}

/**
 * 转换原生会话为UTS会话
 */
function convertNativeConversation(nativeConv: EMConversation): EMConversation {
  let type: EMChatType
  switch nativeConv.type {
    case .group:
      type = 'groupChat'
    case .chatRoom:
      type = 'chatRoom'
    default:
      type = 'chat'
  }
  
  let lastMsg = nativeConv.latestMessage
  
  return {
    conversationId: nativeConv.conversationId,
    type: type,
    unreadCount: Int(nativeConv.unreadMessagesCount),
    lastMessage: lastMsg != nil ? convertNativeMessage(lastMsg!) : undefined,
    ext: nativeConv.ext as? String
  }
}

// ==================== 事件监听 ====================

/**
 * 添加消息接收监听器
 */
export function addMessageListener(listener: EMMessageListener): void {
  if !messageListeners.contains(listener) {
    messageListeners.append(listener)
  }
}

/**
 * 移除消息接收监听器
 */
export function removeMessageListener(listener: EMMessageListener): void {
  if let index = messageListeners.firstIndex(where: { $0 === listener }) {
    messageListeners.remove(at: index)
  }
}

/**
 * 添加连接状态监听器
 */
export function addConnectionListener(listener: EMConnectionListener): void {
  if !connectionListeners.contains(listener) {
    connectionListeners.append(listener)
  }
}

/**
 * 移除连接状态监听器
 */
export function removeConnectionListener(listener: EMConnectionListener): void {
  if let index = connectionListeners.firstIndex(where: { $0 === listener }) {
    connectionListeners.remove(at: index)
  }
}

/**
 * 添加Token即将过期监听器
 */
export function addTokenWillExpireListener(listener: EMTokenWillExpireListener): void {
  if !tokenWillExpireListeners.contains(listener) {
    tokenWillExpireListeners.append(listener)
  }
}

/**
 * 移除Token即将过期监听器
 */
export function removeTokenWillExpireListener(listener: EMTokenWillExpireListener): void {
  if let index = tokenWillExpireListeners.firstIndex(where: { $0 === listener }) {
    tokenWillExpireListeners.remove(at: index)
  }
}

/**
 * 添加Token已过期监听器
 */
export function addTokenDidExpireListener(listener: EMTokenDidExpireListener): void {
  if !tokenDidExpireListeners.contains(listener) {
    tokenDidExpireListeners.append(listener)
  }
}

/**
 * 移除Token已过期监听器
 */
export function removeTokenDidExpireListener(listener: EMTokenDidExpireListener): void {
  if let index = tokenDidExpireListeners.firstIndex(where: { $0 === listener }) {
    tokenDidExpireListeners.remove(at: index)
  }
}

// ==================== 其他 ====================

/**
 * 刷新Token
 */
export function renewToken(token: string): void {
  if !isLoggedIn || emClient == nil { return }
  EMClient.sharedClient.renewToken(token)
}

/**
 * 获取SDK版本号
 */
export function getVersion(): string {
  return EMClient.sharedClient.version
}

/**
 * 设置日志级别
 */
export function setLogLevel(level: number): void {
  // iOS SDK使用boolean控制调试模式
  EMClient.sharedClient.enableConsoleLog(level >= 4)
}

// ==================== 工具函数 ====================

/**
 * 检查SDK是否已初始化
 */
function checkInitialized(onFail?: EMFailCallback): boolean {
  if !isInitialized {
    onFail?.(EMErrorCode.GENERAL_ERROR, 'SDK not initialized')
    return false
  }
  return true
}

/**
 * 检查是否已登录
 */
function checkLogin(onFail?: EMFailCallback): boolean {
  if !isInitialized {
    onFail?.(EMErrorCode.GENERAL_ERROR, 'SDK not initialized')
    return false
  }
  if !isLoggedIn {
    onFail?.(EMErrorCode.USER_AUTHENTICATION_FAILED, 'User not logged in')
    return false
  }
  return true
}
