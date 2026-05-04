import UIKit
import HyphenateChat

@objc
@objcMembers
public class UNIEMClient: NSObject {

    public static let shared = UNIEMClient()

    // MARK: - EMChatManagerDelegate blocks

    /// UTS 侧设置的消息接收回调 block
    public var onMessagesReceivedBlock: ((String) -> Void)?
    /// UTS 侧设置的 CMD 消息接收回调 block
    public var onCmdMessagesReceivedBlock: ((String) -> Void)?
    /// UTS 侧设置的会话列表更新回调 block
    public var onConversationListDidUpdateBlock: ((String) -> Void)?
    /// UTS 侧设置的已读回执回调 block
    public var onMessagesDidReadBlock: ((String) -> Void)?
    /// UTS 侧设置的群消息已读回执回调 block
    public var onGroupMessageDidReadBlock: ((String) -> Void)?
    /// UTS 侧设置的群已读消息数量变化回调 block
    public var onGroupMessageAckHasChangedBlock: (() -> Void)?
    /// UTS 侧设置的会话已读回调 block
    public var onConversationReadBlock: ((String, String) -> Void)?
    /// UTS 侧设置的消息已送达回调 block
    public var onMessagesDidDeliverBlock: ((String) -> Void)?
    /// UTS 侧设置的消息撤回回调 block
    public var onMessagesInfoDidRecallBlock: ((String) -> Void)?
    /// UTS 侧设置的消息状态变化回调 block
    public var onMessageStatusDidChangeBlock: ((String, String) -> Void)?
    /// UTS 侧设置的消息附件状态变化回调 block
    public var onMessageAttachmentStatusDidChangeBlock: ((String, String) -> Void)?
    /// UTS 侧设置的消息内容变化回调 block
    public var onMessageContentChangedBlock: ((String, String, NSNumber) -> Void)?
    /// UTS 侧设置的 Reaction 变化回调 block
    public var onMessageReactionDidChangeBlock: ((String) -> Void)?

    // MARK: - EMClientDelegate blocks

    /// UTS 侧设置的连接状态变化回调 block
    public var onConnectionStateDidChangeBlock: ((NSNumber) -> Void)?
    /// UTS 侧设置的自动登录完成回调 block
    public var onAutoLoginDidCompleteBlock: ((String?) -> Void)?
    /// UTS 侧设置的其他设备登录回调 block
    public var onUserAccountDidLoginFromOtherDeviceBlock: ((String?) -> Void)?
    /// UTS 侧设置的账号被服务器删除回调 block
    public var onUserAccountDidRemoveFromServerBlock: (() -> Void)?
    /// UTS 侧设置的账号被禁用回调 block
    public var onUserDidForbidByServerBlock: (() -> Void)?
    /// UTS 侧设置的被强制登出回调 block
    public var onUserAccountDidForcedToLogoutBlock: ((String?) -> Void)?
    /// UTS 侧设置的 token 即将过期回调 block
    public var onTokenWillExpireBlock: ((NSNumber) -> Void)?
    /// UTS 侧设置的 token 已过期回调 block
    public var onTokenDidExpireBlock: ((NSNumber) -> Void)?

    private override init() {
        super.init()
        EMClient.shared().chatManager?.add(self, delegateQueue: nil)
        EMClient.shared().add(self, delegateQueue: nil)
    }

    public func createCustomMessageBody(event: String, paramsJson: String?) -> EMCustomMessageBody? {
        guard let json = paramsJson else {
            return EMCustomMessageBody(event: event, customExt: nil)
        }
        guard let data = json.data(using: .utf8),
              let dict = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: String] else {
            return EMCustomMessageBody(event: event, customExt: nil)
        }
        return EMCustomMessageBody(event: event, customExt: dict)
    }

    public func setMessageExt(_ message: EMChatMessage, extJson: String?) {
        guard let json = extJson else { return }
        guard let data = json.data(using: .utf8),
              let dict = try? JSONSerialization.jsonObject(with: data, options: []) as? [AnyHashable: Any] else {
            return
        }
        message.ext = dict
    }

    public func fetchConversationsFromServer(cursor: String?, pageSize: NSNumber, completion: @escaping (EMCursorResult<EMConversation>?, EMError?) -> Void) {
        EMClient.shared().chatManager?.getConversationsFromServer(withCursor: cursor, pageSize: UInt8(pageSize.intValue), completion: completion)
    }

    public func fetchMessagesFromServerBy(_ conversationId: String, conversationType: NSNumber, cursor: String?, pageSize: NSNumber, completion: @escaping (EMCursorResult<EMChatMessage>?, EMError?) -> Void) {
        let type = EMConversationType(rawValue: conversationType.intValue) ?? .chat
        EMClient.shared().chatManager?.fetchMessagesFromServer(by: conversationId, conversationType: type, cursor: cursor, pageSize: UInt(pageSize.intValue), option: nil, completion: completion)    }

    public func messagesToJsonString(_ messages: [EMChatMessage]) -> String? {
        return messagesToJson(messages)
    }

    public func messageToJsonString(_ message: EMChatMessage) -> String? {
        return messageToJson(message)
    }

    public func searchLocalMessagesByKeywords(convId: String, keywords: String?, timestamp: NSNumber, maxCount: NSNumber, from: [String]?, direction: NSNumber, scope: NSNumber, completion: @escaping (String?, EMError?) -> Void) {
        guard let conversation = EMClient.shared().chatManager?.getConversationWithConvId(convId) else {
            completion("[]", nil)
            return
        }
        let dir = EMMessageSearchDirection(rawValue: direction.intValue) ?? .up
        let sc = EMMessageSearchScope(rawValue: scope.intValue) ?? .content
        let ts = timestamp.int64Value
        let cnt = maxCount.int32Value
        conversation.loadMessages(withKeyword: keywords, timestamp: ts, count: cnt, fromUsers: from, searchDirection: dir, scope: sc) { messages, error in
            let json = self.messagesToJsonString(messages ?? [])
            completion(json, error)
        }
    }

    public func loadLocalMessages(convId: String, startMsgId: String?, pageSize: NSNumber, completion: @escaping (String?, EMError?) -> Void) {
        guard let conversation = EMClient.shared().chatManager?.getConversationWithConvId(convId) else {
            completion("[]", nil)
            return
        }
        let dir = EMMessageSearchDirection.up
        let cnt = pageSize.int32Value
        let msgId = startMsgId
        conversation.loadMessagesStart(fromId: msgId, count: cnt, searchDirection: dir) { messages, error in
            let json = self.messagesToJsonString(messages ?? [])
            completion(json, error)
        }
    }
}
// MARK: - Helpers

private extension UNIEMClient {
    func errorToJson(_ error: EMError?) -> String? {
        guard let err = error else { return nil }
        let dict: [String: Any] = [
            "code": err.code.rawValue,
            "description": err.errorDescription ?? ""
        ]
        guard let data = try? JSONSerialization.data(withJSONObject: dict, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func reactionToDict(_ reaction: EMMessageReaction) -> [String: Any] {
        return [
            "reaction": reaction.reaction ?? "",
            "count": reaction.count,
            "isAddedBySelf": reaction.isAddedBySelf,
            "userList": reaction.userList ?? []
        ]
    }

    func chatThreadToDict(_ thread: EMChatThread) -> [String: Any] {
        var dict: [String: Any] = [
            "threadId": thread.threadId ?? "",
            "threadName": thread.threadName ?? "",
            "owner": thread.owner ?? "",
            "messageId": thread.messageId ?? "",
            "parentId": thread.parentId ?? "",
            "membersCount": thread.membersCount,
            "messageCount": thread.messageCount,
            "createAt": thread.createAt
        ]
        if let lastMessage = thread.lastMessage {
            dict["lastMessage"] = messageToDict(lastMessage)
        }
        return dict
    }

    func extToDict(_ ext: NSDictionary?) -> [String: Any]? {
        guard let ext = ext else { return nil }
        var result: [String: Any] = [:]
        for (key, value) in ext {
            if let k = key as? String {
                result[k] = value
            }
        }
        return result
    }

    func messageToDict(_ message: EMChatMessage) -> [String: Any] {
        var bodyDict: [String: Any] = ["type": "txt", "message": NSNull()]

        let body = message.body
        switch body.type {
        case .text:
            if let textBody = body as? EMTextMessageBody {
                bodyDict = [
                    "type": "txt",
                    "message": textBody.text,
                    "targetLanguages": textBody.targetLanguages ?? [],
                    "translations": textBody.translations ?? [:]
                ]
            }
        case .image:
            if let imageBody = body as? EMImageMessageBody {
                bodyDict = [
                    "type": "img",
                    "message": imageBody.remotePath ?? NSNull(),
                    "localPath": imageBody.localPath ?? NSNull(),
                    "displayName": imageBody.displayName ?? NSNull(),
                    "secretKey": imageBody.secretKey ?? NSNull(),
                    "fileLength": imageBody.fileLength,
                    "downloadStatus": imageBody.downloadStatus.rawValue,
                    "size": ["width": imageBody.size.width, "height": imageBody.size.height],
                    "compressionRatio": imageBody.compressionRatio,
                    "thumbnailDisplayName": imageBody.thumbnailDisplayName ?? NSNull(),
                    "thumbnailLocalPath": imageBody.thumbnailLocalPath ?? NSNull(),
                    "thumbnailRemotePath": imageBody.thumbnailRemotePath ?? NSNull(),
                    "thumbnailSecretKey": imageBody.thumbnailSecretKey ?? NSNull(),
                    "thumbnailSize": ["width": imageBody.thumbnailSize.width, "height": imageBody.thumbnailSize.height],
                    "thumbnailFileLength": imageBody.thumbnailFileLength,
                    "thumbnailDownloadStatus": imageBody.thumbnailDownloadStatus.rawValue
                ]
            }
        case .voice:
            if let voiceBody = body as? EMVoiceMessageBody {
                bodyDict = [
                    "type": "voice",
                    "message": voiceBody.remotePath ?? NSNull(),
                    "localPath": voiceBody.localPath ?? NSNull(),
                    "displayName": voiceBody.displayName ?? NSNull(),
                    "secretKey": voiceBody.secretKey ?? NSNull(),
                    "fileLength": voiceBody.fileLength,
                    "downloadStatus": voiceBody.downloadStatus.rawValue,
                    "duration": voiceBody.duration
                ]
            }
        case .video:
            if let videoBody = body as? EMVideoMessageBody {
                bodyDict = [
                    "type": "video",
                    "message": videoBody.remotePath ?? NSNull(),
                    "localPath": videoBody.localPath ?? NSNull(),
                    "displayName": videoBody.displayName ?? NSNull(),
                    "secretKey": videoBody.secretKey ?? NSNull(),
                    "fileLength": videoBody.fileLength,
                    "downloadStatus": videoBody.downloadStatus.rawValue,
                    "duration": videoBody.duration,
                    "thumbnailLocalPath": videoBody.thumbnailLocalPath ?? NSNull(),
                    "thumbnailRemotePath": videoBody.thumbnailRemotePath ?? NSNull(),
                    "thumbnailSecretKey": videoBody.thumbnailSecretKey ?? NSNull(),
                    "thumbnailSize": ["width": videoBody.thumbnailSize.width, "height": videoBody.thumbnailSize.height],
                    "thumbnailDownloadStatus": videoBody.thumbnailDownloadStatus.rawValue
                ]
            }
        case .location:
            if let locBody = body as? EMLocationMessageBody {
                bodyDict = [
                    "type": "location",
                    "message": locBody.address ?? NSNull(),
                    "latitude": locBody.latitude,
                    "longitude": locBody.longitude,
                    "buildingName": locBody.buildingName ?? NSNull()
                ]
            }
        case .file:
            if let fileBody = body as? EMFileMessageBody {
                bodyDict = [
                    "type": "file",
                    "message": fileBody.remotePath ?? NSNull(),
                    "localPath": fileBody.localPath ?? NSNull(),
                    "displayName": fileBody.displayName ?? NSNull(),
                    "secretKey": fileBody.secretKey ?? NSNull(),
                    "fileLength": fileBody.fileLength,
                    "downloadStatus": fileBody.downloadStatus.rawValue
                ]
            }
        case .cmd:
            if let cmdBody = body as? EMCmdMessageBody {
                bodyDict = [
                    "type": "cmd",
                    "message": cmdBody.action ?? NSNull(),
                    "isDeliverOnlineOnly": cmdBody.isDeliverOnlineOnly
                ]
            }
        case .custom:
            if let customBody = body as? EMCustomMessageBody {
                bodyDict = [
                    "type": "custom",
                    "message": customBody.event ?? NSNull(),
                    "customExt": customBody.customExt ?? [:]
                ]
            }
        @unknown default:
            break
        }

        var dict: [String: Any] = [
            "msgId": message.messageId,
            "messageId": message.messageId,
            "conversationId": message.conversationId,
            "direction": message.direction.rawValue,
            "from": message.from,
            "to": message.to,
            "timestamp": message.timestamp,
            "localTime": message.localTime,
            "chatType": message.chatType.rawValue,
            "status": message.status.rawValue,
            "onlineState": message.onlineState,
            "isReadAcked": message.isReadAcked,
            "isChatThreadMessage": message.isChatThreadMessage,
            "isNeedGroupAck": message.isNeedGroupAck,
            "groupAckCount": message.groupAckCount,
            "isDeliverAcked": message.isDeliverAcked,
            "isRead": message.isRead,
            "isListened": message.isListened,
            "body": bodyDict,
            "priority": message.priority.rawValue,
            "broadcast": message.broadcast,
            "deliverOnlineOnly": message.deliverOnlineOnly
        ]

        if let ext = extToDict(message.ext as NSDictionary?) {
            dict["ext"] = ext
        }

        if let reactions = message.reactionList {
            dict["reactionList"] = reactions.map { reactionToDict($0) }
        }

        if let thread = message.chatThread {
            dict["chatThread"] = chatThreadToDict(thread)
        }

        if let receivers = message.receiverList {
            dict["receiverList"] = receivers
        }

        return dict
    }

    func messagesToJson(_ messages: [EMChatMessage]) -> String? {
        let dicts = messages.map { messageToDict($0) }
        guard let data = try? JSONSerialization.data(withJSONObject: dicts, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func messageToJson(_ message: EMChatMessage) -> String? {
        let dict = messageToDict(message)
        guard let data = try? JSONSerialization.data(withJSONObject: dict, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func conversationsToJson(_ conversations: [EMConversation]) -> String? {
        let dicts = conversations.map { conv -> [String: Any] in
            var dict: [String: Any] = [
                "conversationId": conv.conversationId ?? "",
                "type": conv.type.rawValue,
                "unreadMsgCount": conv.unreadMessagesCount,
                "messagesCount": conv.messagesCount,
                "isChatThread": conv.isChatThread,
                "isPinned": conv.isPinned,
                "pinnedTime": conv.pinnedTime
            ]
            if let ext = conv.ext {
                dict["ext"] = ext
            }
            if let lastMsg = conv.latestMessage {
                dict["lastMessage"] = messageToDict(lastMsg)
            } else {
                dict["lastMessage"] = NSNull()
            }
            if let marks = conv.marks {
                dict["marks"] = marks.map { $0.intValue }
            }
            return dict
        }
        guard let data = try? JSONSerialization.data(withJSONObject: dicts, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func groupAcksToJson(_ message: EMChatMessage, _ groupAcks: [EMGroupMessageAck]) -> String? {
        let ackDicts = groupAcks.map { ack -> [String: Any] in
            [
                "messageId": ack.messageId,
                "readAckId": ack.readAckId,
                "from": ack.from,
                "content": ack.content,
                "readCount": ack.readCount,
                "timestamp": ack.timestamp
            ]
        }
        let dict: [String: Any] = [
            "message": [
                "msgId": message.messageId,
                "from": message.from,
                "to": message.to,
                "conversationId": message.conversationId,
                "chatType": message.chatType.rawValue
            ],
            "groupAcks": ackDicts
        ]
        guard let data = try? JSONSerialization.data(withJSONObject: dict, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }
    
    func recallMessagesToJson(_ infos: [EMRecallMessageInfo]) -> String? {
        let dicts = infos.map { info -> [String: Any] in
            [
                "recallBy": info.recallBy,
                "recallMessage": [
                    "msgId": info.recallMessage?.messageId ?? "",
                    "from": info.recallMessage?.from ?? "",
                    "to": info.recallMessage?.to ?? "",
                    "conversationId": info.recallMessage?.conversationId ?? "",
                    "chatType": info.recallMessage?.chatType.rawValue ?? 0
                ]
            ]
        }
        guard let data = try? JSONSerialization.data(withJSONObject: dicts, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func reactionChangesToJson(_ changes: [EMMessageReactionChange]) -> String? {
        let dicts = changes.map { change -> [String: Any] in
            let reactions = change.reactions?.map { reaction -> [String: Any] in
                [
                    "reaction": reaction.reaction ?? "",
                    "count": reaction.count,
                    "isAddedBySelf": reaction.isAddedBySelf,
                    "userList": reaction.userList ?? []
                ]
            } ?? []
            let operations = change.operations?.map { op -> [String: Any] in
                [
                    "userId": op.userId,
                    "reaction": op.reaction,
                    "operate": op.operate.rawValue
                ]
            } ?? []
            return [
                "conversationId": change.conversationId ?? "",
                "messageId": change.messageId ?? "",
                "reactions": reactions,
                "operations": operations
            ]
        }
        guard let data = try? JSONSerialization.data(withJSONObject: dicts, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }
}

// MARK: - EMChatManagerDelegate

extension UNIEMClient: EMChatManagerDelegate {

    /// 会话列表发生变化
    public func conversationListDidUpdate(_ aConversationList: [EMConversation]) {
        guard let json = conversationsToJson(aConversationList) else { return }
        DispatchQueue.main.async {
            self.onConversationListDidUpdateBlock?(json)
        }
    }

    /// 收到普通消息
    public func messagesDidReceive(_ aMessages: [EMChatMessage]) {
        guard let json = messagesToJson(aMessages) else { return }
        DispatchQueue.main.async {
            self.onMessagesReceivedBlock?(json)
        }
    }

    /// 收到透传消息
    public func cmdMessagesDidReceive(_ aCmdMessages: [EMChatMessage]) {
        guard let json = messagesToJson(aCmdMessages) else { return }
        DispatchQueue.main.async {
            self.onCmdMessagesReceivedBlock?(json)
        }
    }

    /// 收到已读回执
    public func messagesDidRead(_ aMessages: [EMChatMessage]) {
        guard let json = messagesToJson(aMessages) else { return }
        DispatchQueue.main.async {
            self.onMessagesDidReadBlock?(json)
        }
    }

    /// 收到群消息已读回执
    public func groupMessageDidRead(_ aMessage: EMChatMessage, groupAcks: [EMGroupMessageAck]) {
        guard let json = groupAcksToJson(aMessage, groupAcks) else { return }
        DispatchQueue.main.async {
            self.onGroupMessageDidReadBlock?(json)
        }
    }

    /// 群已读消息数量发生变化
    public func groupMessageAckHasChanged() {
        DispatchQueue.main.async {
            self.onGroupMessageAckHasChangedBlock?()
        }
    }

    /// 收到会话已读回调
    public func onConversationRead(_ from: String, to: String) {
        DispatchQueue.main.async {
            self.onConversationReadBlock?(from, to)
        }
    }

    /// 消息已送达
    public func messagesDidDeliver(_ aMessages: [EMChatMessage]) {
        guard let json = messagesToJson(aMessages) else { return }
        DispatchQueue.main.async {
            self.onMessagesDidDeliverBlock?(json)
        }
    }

    /// 消息撤回
    public func messagesInfoDidRecall(_ aRecallMessagesInfo: [EMRecallMessageInfo]) {
        guard let json = recallMessagesToJson(aRecallMessagesInfo) else { return }
        DispatchQueue.main.async {
            self.onMessagesInfoDidRecallBlock?(json)
        }
    }

    /// 消息状态发生变化
    public func messageStatusDidChange(_ aMessage: EMChatMessage, error: EMError?) {
        let errorJson = errorToJson(error) ?? "{}"
        guard let msgJson = messageToJson(aMessage) else { return }
        DispatchQueue.main.async {
            self.onMessageStatusDidChangeBlock?(msgJson, errorJson)
        }
    }

    /// 消息附件状态发生变化
    public func messageAttachmentStatusDidChange(_ aMessage: EMChatMessage, error: EMError?) {
        let errorJson = errorToJson(error) ?? "{}"
        guard let msgJson = messageToJson(aMessage) else { return }
        DispatchQueue.main.async {
            self.onMessageAttachmentStatusDidChangeBlock?(msgJson, errorJson)
        }
    }

    /// 消息内容变化
    public func onMessageContentChanged(_ message: EMChatMessage, operatorId: String, operationTime: UInt) {
        guard let msgJson = messageToJson(message) else { return }
        DispatchQueue.main.async {
            self.onMessageContentChangedBlock?(msgJson, operatorId, NSNumber(value: operationTime))
        }
    }

    /// Reaction 数据发生变化
    public func messageReactionDidChange(_ changes: [EMMessageReactionChange]) {
        guard let json = reactionChangesToJson(changes) else { return }
        DispatchQueue.main.async {
            self.onMessageReactionDidChangeBlock?(json)
        }
    }
}

// MARK: - EMClientDelegate

extension UNIEMClient: EMClientDelegate {

    /// 连接状态变化
    public func connectionStateDidChange(_ aConnectionState: EMConnectionState) {
        DispatchQueue.main.async {
            self.onConnectionStateDidChangeBlock?(NSNumber(value: aConnectionState.rawValue))
        }
    }

    /// 自动登录完成
    public func autoLoginDidCompleteWithError(_ aError: EMError?) {
        let errorJson = errorToJson(aError)
        DispatchQueue.main.async {
            self.onAutoLoginDidCompleteBlock?(errorJson)
        }
    }

    /// 当前登录账号在其他设备登录
    public func userAccountDidLogin(fromOtherDevice aDeviceName: String?) {
        DispatchQueue.main.async {
            self.onUserAccountDidLoginFromOtherDeviceBlock?(aDeviceName)
        }
    }

    /// 当前登录账号被从服务器删除
    public func userAccountDidRemoveFromServer() {
        DispatchQueue.main.async {
            self.onUserAccountDidRemoveFromServerBlock?()
        }
    }

    /// 当前用户账号被禁用
    public func userDidForbidByServer() {
        DispatchQueue.main.async {
            self.onUserDidForbidByServerBlock?()
        }
    }

    /// 当前登录账号被强制登出
    public func userAccountDidForced(toLogout aError: EMError?) {
        let errorJson = errorToJson(aError)
        DispatchQueue.main.async {
            self.onUserAccountDidForcedToLogoutBlock?(errorJson)
        }
    }

    /// token 即将过期
    public func tokenWillExpire(_ aErrorCode: EMErrorCode) {
        DispatchQueue.main.async {
            self.onTokenWillExpireBlock?(NSNumber(value: aErrorCode.rawValue))
        }
    }

    /// token 已过期
    public func tokenDidExpire(_ aErrorCode: EMErrorCode) {
        DispatchQueue.main.async {
            self.onTokenDidExpireBlock?(NSNumber(value: aErrorCode.rawValue))
        }
    }
}
