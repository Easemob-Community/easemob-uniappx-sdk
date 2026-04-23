import UIKit
import HyphenateChat

@objc
@objcMembers
public class UNIEMClient: NSObject {

    public static let shared = UNIEMClient()

    /// UTS 侧设置的消息接收回调 block
    public var onMessagesReceivedBlock: (([EMChatMessage]) -> Void)?
    /// UTS 侧设置的 CMD 消息接收回调 block
    public var onCmdMessagesReceivedBlock: (([EMChatMessage]) -> Void)?

    private override init() {
        super.init()
        EMClient.shared().chatManager?.add(self, delegateQueue: nil)

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
}
extension UNIEMClient:EMChatManagerDelegate{
    
    // MARK: - EMChatManagerDelegate 消息接收回调

    /// 收到普通消息
    public func messagesDidReceive(_ aMessages: [EMChatMessage]) {
        guard !aMessages.isEmpty else { return }
        DispatchQueue.main.async {
            self.onMessagesReceivedBlock?(aMessages)
        }
    }

    /// 收到透传消息
    public func cmdMessagesDidReceive(_ aCmdMessages: [EMChatMessage]) {
        guard !aCmdMessages.isEmpty else { return }
        DispatchQueue.main.async {
            self.onCmdMessagesReceivedBlock?(aCmdMessages)
        }
    }

    // MARK: - 消息转字典（供 UTS 侧调用，或 Swift 内部使用）
    private func messageToDict(_ message: EMChatMessage) -> [String: Any] {
        var dict: [String: Any] = [
            "messageId": message.messageId,
            "from": message.from,
            "to": message.to,
            "conversationId": message.conversationId,
            "chatType": chatTypeToString(message.chatType),
            "direction": message.direction == .send ? "send" : "receive",
            "timestamp": message.timestamp,
            "localTime": message.localTime,
            "isRead": message.isRead,
            "status": messageStatusToString(message.status)
        ]
        
        // 消息体
        dict["body"] = messageBodyToDict(message.body)
        
        return dict
    }
    
    private func chatTypeToString(_ type: EMChatType) -> String {
        switch type {
        case .chat: return "chat"
        case .groupChat: return "groupchat"
        case .chatRoom: return "chatroom"
        @unknown default: return "chat"
        }
    }
    
    private func messageStatusToString(_ status: EMMessageStatus) -> String {
        switch status {
        case .pending: return "pending"
        case .delivering: return "delivering"
        case .succeed: return "succeeded"
        case .failed: return "failed"
        @unknown default: return "pending"
        }
    }
    
    private func messageBodyToDict(_ body: EMMessageBody) -> [String: Any] {
        var dict: [String: Any] = [:]
        
        switch body.type {
        case .text:
            if let textBody = body as? EMTextMessageBody {
                dict = [
                    "type": "text",
                    "text": textBody.text
                ]
            }
        case .image:
            if let imageBody = body as? EMImageMessageBody {
                dict = [
                    "type": "image",
                    "remotePath": imageBody.remotePath ?? "",
                    "localPath": imageBody.localPath ?? "",
                    "thumbnailRemotePath": imageBody.thumbnailRemotePath ?? "",
                    "thumbnailLocalPath": imageBody.thumbnailLocalPath ?? "",
                    "width": imageBody.size.width,
                    "height": imageBody.size.height,
                    "fileSize": imageBody.fileLength
                ]
            }
        case .voice:
            if let voiceBody = body as? EMVoiceMessageBody {
                dict = [
                    "type": "voice",
                    "remotePath": voiceBody.remotePath ?? "",
                    "localPath": voiceBody.localPath ?? "",
                    "duration": voiceBody.duration
                ]
            }
        case .video:
            if let videoBody = body as? EMVideoMessageBody {
                dict = [
                    "type": "video",
                    "remotePath": videoBody.remotePath ?? "",
                    "localPath": videoBody.localPath ?? "",
                    "thumbnailRemotePath": videoBody.thumbnailRemotePath ?? "",
                    "thumbnailLocalPath": videoBody.thumbnailLocalPath ?? "",
                    "duration": videoBody.duration,
                    "fileSize": videoBody.fileLength
                ]
            }
        case .file:
            if let fileBody = body as? EMFileMessageBody {
                dict = [
                    "type": "file",
                    "remotePath": fileBody.remotePath ?? "",
                    "localPath": fileBody.localPath ?? "",
                    "displayName": fileBody.displayName ?? "",
                    "fileSize": fileBody.fileLength
                ]
            }
        case .location:
            if let locationBody = body as? EMLocationMessageBody {
                dict = [
                    "type": "location",
                    "latitude": locationBody.latitude,
                    "longitude": locationBody.longitude,
                    "address": locationBody.address ?? "",
                    "buildingName": locationBody.buildingName ?? ""
                ]
            }
        case .cmd:
            if let cmdBody = body as? EMCmdMessageBody {
                dict = [
                    "type": "cmd",
                    "action": cmdBody.action ?? ""
                ]
            }
        case .custom:
            if let customBody = body as? EMCustomMessageBody {
                dict = [
                    "type": "custom",
                    "event": customBody.event ?? "",
                    "params": customBody.customExt ?? [:]
                ]
            }
        case .combine:
            if let combineBody = body as? EMCombineMessageBody {
                dict = [
                    "type": "combine",
                    "title": combineBody.title ?? "",
                    "summary": combineBody.summary ?? ""
                ]
            }
        @unknown default:
            dict = ["type": "unknown"]
        }
        
        return dict
    }
}
