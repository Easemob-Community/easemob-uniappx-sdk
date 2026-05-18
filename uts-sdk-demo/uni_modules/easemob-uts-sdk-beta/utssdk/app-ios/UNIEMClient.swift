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

    // MARK: - EMGroupManagerDelegate blocks

    /// 收到群组邀请
    public var onGroupInvitationDidReceiveBlock: ((String) -> Void)?
    /// 群组邀请被接受
    public var onGroupInvitationDidAcceptBlock: ((String) -> Void)?
    /// 群组邀请被拒绝
    public var onGroupInvitationDidDeclineBlock: ((String) -> Void)?
    /// 自动加入群组
    public var onDidJoinGroupBlock: ((String) -> Void)?
    /// 离开群组
    public var onDidLeaveGroupBlock: ((String) -> Void)?
    /// 收到入群申请
    public var onJoinGroupRequestDidReceiveBlock: ((String) -> Void)?
    /// 入群申请被拒绝
    public var onJoinGroupRequestDidDeclineBlock: ((String) -> Void)?
    /// 入群申请被同意
    public var onJoinGroupRequestDidApproveBlock: ((String) -> Void)?
    /// 群组列表更新
    public var onGroupListDidUpdateBlock: ((String) -> Void)?
    /// 群成员加入禁言列表
    public var onGroupMuteListAddedBlock: ((String) -> Void)?
    /// 群成员移出禁言列表
    public var onGroupMuteListRemovedBlock: ((String) -> Void)?
    /// 群成员加入白名单
    public var onGroupWhiteListAddedBlock: ((String) -> Void)?
    /// 群成员移出白名单
    public var onGroupWhiteListRemovedBlock: ((String) -> Void)?
    /// 全员禁言状态变化
    public var onGroupAllMemberMuteChangedBlock: ((String) -> Void)?
    /// 管理员添加
    public var onGroupAdminAddedBlock: ((String) -> Void)?
    /// 管理员移除
    public var onGroupAdminRemovedBlock: ((String) -> Void)?
    /// 群主变更
    public var onGroupOwnerDidUpdateBlock: ((String) -> Void)?
    /// 用户加入群组
    public var onUserDidJoinGroupBlock: ((String) -> Void)?
    /// 用户离开群组
    public var onUserDidLeaveGroupBlock: ((String) -> Void)?
    /// 群公告更新
    public var onGroupAnnouncementDidUpdateBlock: ((String) -> Void)?
    /// 群共享文件添加
    public var onGroupFileListAddedBlock: ((String) -> Void)?
    /// 群共享文件移除
    public var onGroupFileListRemovedBlock: ((String) -> Void)?
    /// 群禁用状态变化
    public var onGroupStateChangedBlock: ((String) -> Void)?
    /// 群详情更新
    public var onGroupSpecificationDidUpdateBlock: ((String) -> Void)?
    /// 群成员自定义属性变更
    public var onGroupMemberAttributesChangedBlock: ((String) -> Void)?

    // MARK: - EMChatroomManagerDelegate blocks

    /// 用户加入聊天室
    public var onChatroomUserJoinedBlock: ((String) -> Void)?
    /// 用户离开聊天室
    public var onChatroomUserLeftBlock: ((String) -> Void)?
    /// 被踢出聊天室
    public var onChatroomDidDismissBlock: ((String) -> Void)?
    /// 聊天室详情更新
    public var onChatroomSpecificationDidUpdateBlock: ((String) -> Void)?
    /// 聊天室成员加入禁言列表
    public var onChatroomMuteListAddedBlock: ((String) -> Void)?
    /// 聊天室成员移出禁言列表
    public var onChatroomMuteListRemovedBlock: ((String) -> Void)?
    /// 聊天室成员加入白名单
    public var onChatroomWhiteListAddedBlock: ((String) -> Void)?
    /// 聊天室成员移出白名单
    public var onChatroomWhiteListRemovedBlock: ((String) -> Void)?
    /// 聊天室全员禁言状态变化
    public var onChatroomAllMemberMuteChangedBlock: ((String) -> Void)?
    /// 聊天室管理员添加
    public var onChatroomAdminAddedBlock: ((String) -> Void)?
    /// 聊天室管理员移除
    public var onChatroomAdminRemovedBlock: ((String) -> Void)?
    /// 聊天室所有者更新
    public var onChatroomOwnerDidUpdateBlock: ((String) -> Void)?
    /// 聊天室公告更新
    public var onChatroomAnnouncementDidUpdateBlock: ((String) -> Void)?
    /// 聊天室自定义属性更新
    public var onChatroomAttributesUpdatedBlock: ((String) -> Void)?
    /// 聊天室自定义属性移除
    public var onChatroomAttributesRemovedBlock: ((String) -> Void)?

    private override init() {
        super.init()
        EMClient.shared().chatManager?.add(self, delegateQueue: nil)
        EMClient.shared().add(self, delegateQueue: nil)
        EMClient.shared().groupManager?.add(self, delegateQueue: nil)
        EMClient.shared().roomManager?.add(self, delegateQueue: nil)
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

    func groupToDict(_ group: EMGroup) -> [String: Any] {
        let desc = (group.value(forKey: "description") as? String) ?? ""
        var dict: [String: Any] = [
            "groupId": group.groupId ?? "",
            "groupName": group.groupName ?? "",
            "groupAvatar": group.groupAvatar ?? "",
            "description": desc,
            "announcement": group.announcement ?? "",
            "occupantsCount": group.occupantsCount,
            "isPublic": group.isPublic,
            "isMuteAllMembers": group.isMuteAllMembers,
            "isDisabled": group.isDisabled,
            "isBlocked": group.isBlocked,
            "isPushNotificationEnabled": group.isPushNotificationEnabled,
            "permissionType": group.permissionType.rawValue,
            "owner": group.owner ?? ""
        ]
        if let settings = group.settings {
            dict["maxUsers"] = settings.maxUsers
            dict["style"] = settings.style.rawValue
            if let ext = settings.ext {
                dict["ext"] = ext
            }
        }
        if let admins = group.adminList {
            dict["adminList"] = admins
        }
        if let members = group.memberList {
            dict["memberList"] = members
        }
        if let users = group.users {
            dict["users"] = users
        }
        if let blacklist = group.blacklist {
            dict["blacklist"] = blacklist
        }
        if let muteList = group.muteList {
            dict["muteList"] = muteList
        }
        if let whiteList = group.whiteList {
            dict["whiteList"] = whiteList
        }
        if let files = group.sharedFileList {
            dict["sharedFileList"] = files.map { sharedFileToDict($0) }
        }
        return dict
    }

    func groupToJson(_ group: EMGroup) -> String? {
        let dict = groupToDict(group)
        guard let data = try? JSONSerialization.data(withJSONObject: dict, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func groupListToJson(_ groups: [EMGroup]) -> String? {
        let dicts = groups.map { groupToDict($0) }
        guard let data = try? JSONSerialization.data(withJSONObject: dicts, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func sharedFileToDict(_ file: EMGroupSharedFile) -> [String: Any] {
        return [
            "fileId": file.fileId ?? "",
            "fileName": file.fileName ?? "",
            "fileOwner": file.fileOwner ?? "",
            "fileSize": file.fileSize,
            "createdAt": file.createdAt
        ]
    }

    func sharedFileToJson(_ file: EMGroupSharedFile) -> String? {
        let dict = sharedFileToDict(file)
        guard let data = try? JSONSerialization.data(withJSONObject: dict, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func chatroomToDict(_ room: EMChatroom) -> [String: Any] {
        let desc = (room.value(forKey: "description") as? String) ?? ""
        var dict: [String: Any] = [
            "chatroomId": room.chatroomId ?? "",
            "subject": room.subject ?? "",
            "description": desc,
            "owner": room.owner ?? "",
            "announcement": room.announcement ?? "",
            "permissionType": room.permissionType.rawValue,
            "maxOccupantsCount": room.maxOccupantsCount,
            "occupantsCount": room.occupantsCount,
            "isMuteAllMembers": room.isMuteAllMembers,
            "isInWhitelist": room.isInWhitelist,
            "createTimestamp": room.createTimestamp,
            "muteExpireTimestamp": room.muteExpireTimestamp
        ]
        if let admins = room.adminList {
            dict["adminList"] = admins
        }
        if let members = room.memberList {
            dict["memberList"] = members
        }
        if let blacklist = room.blacklist {
            dict["blacklist"] = blacklist
        }
        if let whitelist = room.whitelist {
            dict["whitelist"] = whitelist
        }
        if let muteMembers = room.muteMembers {
            var muteDict: [String: Any] = [:]
            for (k, v) in muteMembers {
                muteDict[k] = v
            }
            dict["muteMembers"] = muteDict
        }
        return dict
    }

    func chatroomToJson(_ room: EMChatroom) -> String? {
        let dict = chatroomToDict(room)
        guard let data = try? JSONSerialization.data(withJSONObject: dict, options: []) else { return nil }
        return String(data: data, encoding: .utf8)
    }

    func dictToJsonString(_ dict: [String: Any]) -> String? {
        guard let data = try? JSONSerialization.data(withJSONObject: dict, options: []) else { return nil }
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

// MARK: - EMGroupManagerDelegate

extension UNIEMClient: EMGroupManagerDelegate {

    /// 收到群组邀请
    @objc(groupInvitationDidReceive:groupName:inviter:message:)
    public func groupInvitationDidReceive(_ aGroupId: String, groupName aGroupName: String, inviter aInviter: String, message aMessage: String?) {
        let dict: [String: Any] = [
            "groupId": aGroupId,
            "groupName": aGroupName,
            "inviter": aInviter,
            "message": aMessage ?? ""
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupInvitationDidReceiveBlock?(json)
        }
    }

    /// 群组邀请被接受
    @objc(groupInvitationDidAccept:invitee:)
    public func groupInvitationDidAccept(_ aGroup: EMGroup, invitee aInvitee: String) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "invitee": aInvitee
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupInvitationDidAcceptBlock?(json)
        }
    }

    /// 群组邀请被拒绝
    @objc(groupInvitationDidDecline:invitee:reason:)
    public func groupInvitationDidDecline(_ aGroup: EMGroup, invitee aInvitee: String, reason aReason: String?) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "invitee": aInvitee,
            "reason": aReason ?? ""
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupInvitationDidDeclineBlock?(json)
        }
    }

    /// 自动加入群组
    @objc(didJoinGroup:inviter:message:)
    public func didJoinGroup(_ aGroup: EMGroup, inviter aInviter: String, message aMessage: String?) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "inviter": aInviter,
            "message": aMessage ?? ""
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onDidJoinGroupBlock?(json)
        }
    }

    /// 离开群组
    @objc(didLeaveGroup:reason:)
    public func didLeaveGroup(_ aGroup: EMGroup, reason aReason: EMGroupLeaveReason) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "reason": aReason.rawValue
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onDidLeaveGroupBlock?(json)
        }
    }

    /// 收到入群申请
    @objc(joinGroupRequestDidReceive:user:reason:)
    public func joinGroupRequestDidReceive(_ aGroup: EMGroup, user aUsername: String, reason aReason: String?) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "applicant": aUsername,
            "reason": aReason ?? ""
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onJoinGroupRequestDidReceiveBlock?(json)
        }
    }

    /// 入群申请被拒绝
    @objc(joinGroupRequestDidDecline:reason:decliner:applicant:)
    public func joinGroupRequestDidDecline(_ aGroupId: String, reason aReason: String?, decliner aDecliner: String?, applicant aApplicant: String) {
        let dict: [String: Any] = [
            "groupId": aGroupId,
            "reason": aReason ?? "",
            "decliner": aDecliner ?? "",
            "applicant": aApplicant
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onJoinGroupRequestDidDeclineBlock?(json)
        }
    }

    /// 入群申请被同意
    @objc(joinGroupRequestDidApprove:)
    public func joinGroupRequestDidApprove(_ aGroup: EMGroup) {
        guard let json = groupToJson(aGroup) else { return }
        DispatchQueue.main.async {
            self.onJoinGroupRequestDidApproveBlock?(json)
        }
    }

    /// 群组列表更新
    @objc(groupListDidUpdate:)
    public func groupListDidUpdate(_ aGroupList: [EMGroup]) {
        guard let json = groupListToJson(aGroupList) else { return }
        DispatchQueue.main.async {
            self.onGroupListDidUpdateBlock?(json)
        }
    }

    /// 群成员加入禁言列表
    @objc(groupMuteListDidUpdate:addedMutedMembers:muteExpire:)
    public func groupMuteListDidUpdate(_ aGroup: EMGroup, addedMutedMembers aMutedMembers: [String], muteExpire aMuteExpire: Int) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "mutedMembers": aMutedMembers,
            "muteExpire": aMuteExpire
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupMuteListAddedBlock?(json)
        }
    }

    /// 群成员移出禁言列表
    @objc(groupMuteListDidUpdate:removedMutedMembers:)
    public func groupMuteListDidUpdate(_ aGroup: EMGroup, removedMutedMembers aMutedMembers: [String]) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "mutedMembers": aMutedMembers
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupMuteListRemovedBlock?(json)
        }
    }

    /// 群成员加入白名单
    @objc(groupWhiteListDidUpdate:addedWhiteListMembers:)
    public func groupWhiteListDidUpdate(_ aGroup: EMGroup, addedWhiteListMembers aMembers: [String]) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "members": aMembers
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupWhiteListAddedBlock?(json)
        }
    }

    /// 群成员移出白名单
    @objc(groupWhiteListDidUpdate:removedWhiteListMembers:)
    public func groupWhiteListDidUpdate(_ aGroup: EMGroup, removedWhiteListMembers aMembers: [String]) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "members": aMembers
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupWhiteListRemovedBlock?(json)
        }
    }

    /// 全员禁言状态变化
    @objc(groupAllMemberMuteChanged:isAllMemberMuted:)
    public func groupAllMemberMuteChanged(_ aGroup: EMGroup, isAllMemberMuted aMuted: Bool) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "isAllMuted": aMuted
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupAllMemberMuteChangedBlock?(json)
        }
    }

    /// 添加管理员
    @objc(groupAdminListDidUpdate:addedAdmin:)
    public func groupAdminListDidUpdate(_ aGroup: EMGroup, addedAdmin aAdmin: String) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "admin": aAdmin
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupAdminAddedBlock?(json)
        }
    }

    /// 移除管理员
    @objc(groupAdminListDidUpdate:removedAdmin:)
    public func groupAdminListDidUpdate(_ aGroup: EMGroup, removedAdmin aAdmin: String) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "admin": aAdmin
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupAdminRemovedBlock?(json)
        }
    }

    /// 群主变更
    @objc(groupOwnerDidUpdate:newOwner:oldOwner:)
    public func groupOwnerDidUpdate(_ aGroup: EMGroup, newOwner aNewOwner: String, oldOwner aOldOwner: String) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "newOwner": aNewOwner,
            "oldOwner": aOldOwner
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupOwnerDidUpdateBlock?(json)
        }
    }

    /// 用户加入群组
    @objc(userDidJoinGroup:users:)
    public func userDidJoinGroup(_ group: EMGroup, users userIds: [String]) {
        let dict: [String: Any] = [
            "group": groupToDict(group),
            "userIds": userIds
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onUserDidJoinGroupBlock?(json)
        }
    }

    /// 用户离开群组
    @objc(userDidLeaveGroup:users:)
    public func userDidLeaveGroup(_ group: EMGroup, users userIds: [String]) {
        let dict: [String: Any] = [
            "group": groupToDict(group),
            "userIds": userIds
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onUserDidLeaveGroupBlock?(json)
        }
    }

    /// 群公告更新
    @objc(groupAnnouncementDidUpdate:announcement:)
    public func groupAnnouncementDidUpdate(_ aGroup: EMGroup, announcement aAnnouncement: String?) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "announcement": aAnnouncement ?? ""
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupAnnouncementDidUpdateBlock?(json)
        }
    }

    /// 群共享文件添加
    @objc(groupFileListDidUpdate:addedSharedFile:)
    public func groupFileListDidUpdate(_ aGroup: EMGroup, addedSharedFile aSharedFile: EMGroupSharedFile) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "sharedFile": sharedFileToDict(aSharedFile)
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupFileListAddedBlock?(json)
        }
    }

    /// 群共享文件移除
    @objc(groupFileListDidUpdate:removedSharedFile:)
    public func groupFileListDidUpdate(_ aGroup: EMGroup, removedSharedFile aFileId: String) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "fileId": aFileId
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupFileListRemovedBlock?(json)
        }
    }

    /// 群禁用状态变化
    @objc(groupStateChanged:isDisabled:)
    public func groupStateChanged(_ aGroup: EMGroup, isDisabled aDisabled: Bool) {
        let dict: [String: Any] = [
            "group": groupToDict(aGroup),
            "isDisabled": aDisabled
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupStateChangedBlock?(json)
        }
    }

    /// 群详情更新
    @objc(groupSpecificationDidUpdate:)
    public func groupSpecificationDidUpdate(_ aGroup: EMGroup) {
        guard let json = groupToJson(aGroup) else { return }
        DispatchQueue.main.async {
            self.onGroupSpecificationDidUpdateBlock?(json)
        }
    }

    /// 群成员自定义属性变更
    @objc(onAttributesChangedOfGroupMember:userId:attributes:operatorId:)
    public func onAttributesChangedOfGroupMember(_ groupId: String, userId: String, attributes: [String: String]?, operatorId: String) {
        let dict: [String: Any] = [
            "groupId": groupId,
            "userId": userId,
            "attributes": attributes ?? [:],
            "operatorId": operatorId
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onGroupMemberAttributesChangedBlock?(json)
        }
    }
}

// MARK: - EMChatroomManagerDelegate

extension UNIEMClient: EMChatroomManagerDelegate {

    /// 用户加入聊天室
    @objc(userDidJoinChatroom:user:ext:)
    public func userDidJoinChatroom(_ aChatroom: EMChatroom, user aUsername: String, ext: String?) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "user": aUsername,
            "ext": ext ?? ""
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomUserJoinedBlock?(json)
        }
    }

    /// 用户离开聊天室
    @objc(userDidLeaveChatroom:user:)
    public func userDidLeaveChatroom(_ aChatroom: EMChatroom, user aUsername: String) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "user": aUsername
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomUserLeftBlock?(json)
        }
    }

    /// 被踢出聊天室
    @objc(didDismissFromChatroom:reason:)
    public func didDismissFromChatroom(_ aChatroom: EMChatroom, reason aReason: EMChatroomBeKickedReason) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "reason": aReason.rawValue
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomDidDismissBlock?(json)
        }
    }

    /// 聊天室详情更新
    @objc(chatroomSpecificationDidUpdate:)
    public func chatroomSpecificationDidUpdate(_ aChatroom: EMChatroom) {
        guard let json = chatroomToJson(aChatroom) else { return }
        DispatchQueue.main.async {
            self.onChatroomSpecificationDidUpdateBlock?(json)
        }
    }

    /// 聊天室成员加入禁言列表
    @objc(chatroomMuteListDidUpdate:addedMutedMembers:)
    public func chatroomMuteListDidUpdate(_ aChatroom: EMChatroom, addedMutedMembers aMutes: [String: NSNumber]) {
        var muteDict: [String: Any] = [:]
        for (k, v) in aMutes {
            muteDict[k] = v
        }
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "mutedMembers": muteDict
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomMuteListAddedBlock?(json)
        }
    }

    /// 聊天室成员移出禁言列表
    @objc(chatroomMuteListDidUpdate:removedMutedMembers:)
    public func chatroomMuteListDidUpdate(_ aChatroom: EMChatroom, removedMutedMembers aMutes: [String]) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "mutedMembers": aMutes
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomMuteListRemovedBlock?(json)
        }
    }

    /// 聊天室成员加入白名单
    @objc(chatroomWhiteListDidUpdate:addedWhiteListMembers:)
    public func chatroomWhiteListDidUpdate(_ aChatroom: EMChatroom, addedWhiteListMembers aMembers: [String]) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "members": aMembers
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomWhiteListAddedBlock?(json)
        }
    }

    /// 聊天室成员移出白名单
    @objc(chatroomWhiteListDidUpdate:removedWhiteListMembers:)
    public func chatroomWhiteListDidUpdate(_ aChatroom: EMChatroom, removedWhiteListMembers aMembers: [String]) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "members": aMembers
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomWhiteListRemovedBlock?(json)
        }
    }

    /// 聊天室全员禁言状态变化
    @objc(chatroomAllMemberMuteChanged:isAllMemberMuted:)
    public func chatroomAllMemberMuteChanged(_ aChatroom: EMChatroom, isAllMemberMuted aMuted: Bool) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "isAllMemberMuted": aMuted
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomAllMemberMuteChangedBlock?(json)
        }
    }

    /// 聊天室管理员添加
    @objc(chatroomAdminListDidUpdate:addedAdmin:)
    public func chatroomAdminListDidUpdate(_ aChatroom: EMChatroom, addedAdmin aAdmin: String) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "admin": aAdmin
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomAdminAddedBlock?(json)
        }
    }

    /// 聊天室管理员移除
    @objc(chatroomAdminListDidUpdate:removedAdmin:)
    public func chatroomAdminListDidUpdate(_ aChatroom: EMChatroom, removedAdmin aAdmin: String) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "admin": aAdmin
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomAdminRemovedBlock?(json)
        }
    }

    /// 聊天室所有者更新
    @objc(chatroomOwnerDidUpdate:newOwner:oldOwner:)
    public func chatroomOwnerDidUpdate(_ aChatroom: EMChatroom, newOwner aNewOwner: String, oldOwner aOldOwner: String) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "newOwner": aNewOwner,
            "oldOwner": aOldOwner
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomOwnerDidUpdateBlock?(json)
        }
    }

    /// 聊天室公告更新
    @objc(chatroomAnnouncementDidUpdate:announcement:)
    public func chatroomAnnouncementDidUpdate(_ aChatroom: EMChatroom, announcement aAnnouncement: String?) {
        let dict: [String: Any] = [
            "chatroom": chatroomToDict(aChatroom),
            "announcement": aAnnouncement ?? ""
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomAnnouncementDidUpdateBlock?(json)
        }
    }

    /// 聊天室自定义属性更新
    @objc(chatroomAttributesDidUpdated:attributeMap:from:)
    public func chatroomAttributesDidUpdated(_ roomId: String, attributeMap: [String: String], from fromId: String) {
        let dict: [String: Any] = [
            "roomId": roomId,
            "attributeMap": attributeMap,
            "fromId": fromId
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomAttributesUpdatedBlock?(json)
        }
    }

    /// 聊天室自定义属性移除
    @objc(chatroomAttributesDidRemoved:attributes:from:)
    public func chatroomAttributesDidRemoved(_ roomId: String, attributes: [String], from fromId: String) {
        let dict: [String: Any] = [
            "roomId": roomId,
            "attributes": attributes,
            "fromId": fromId
        ]
        guard let json = dictToJsonString(dict) else { return }
        DispatchQueue.main.async {
            self.onChatroomAttributesRemovedBlock?(json)
        }
    }
}
