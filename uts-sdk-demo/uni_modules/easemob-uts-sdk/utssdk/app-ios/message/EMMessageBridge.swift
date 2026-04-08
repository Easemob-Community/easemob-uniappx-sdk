import Foundation
import HyphenateChat

// MARK: - 线程安全的结果存储（复用 Auth 侧的设计模式）

private let msgResultLock = NSLock()
/// key: callbackId, value: { "status": "success"/"error", "msgId": ..., "from": ..., "to": ..., "type": ..., "chatType": ..., "timestamp": ..., "code": ..., "message": ... }
private var msgResultMap: [String: [String: Any]] = [:]

// MARK: - 进度存储（附件消息上传进度）
private let msgProgressLock = NSLock()
/// key: callbackId, value: 0-100
private var msgProgressMap: [String: Int] = [:]

private func setMsgProgress(_ progress: Int, for callbackId: String) {
    msgProgressLock.lock()
    msgProgressMap[callbackId] = progress
    msgProgressLock.unlock()
}

private func getMsgProgress(for callbackId: String) -> Int {
    msgProgressLock.lock()
    defer { msgProgressLock.unlock() }
    return msgProgressMap[callbackId] ?? -1
}

private func removeMsgProgress(for callbackId: String) {
    msgProgressLock.lock()
    msgProgressMap.removeValue(forKey: callbackId)
    msgProgressLock.unlock()
}

private func setMsgResult(_ dict: [String: Any], for callbackId: String) {
    msgResultLock.lock()
    msgResultMap[callbackId] = dict
    msgResultLock.unlock()
}

private func getMsgResult(for callbackId: String) -> [String: Any]? {
    msgResultLock.lock()
    defer { msgResultLock.unlock() }
    return msgResultMap[callbackId]
}

private func removeMsgResult(for callbackId: String) {
    msgResultLock.lock()
    msgResultMap.removeValue(forKey: callbackId)
    msgResultLock.unlock()
}

// MARK: - 消息发送桥接类

/// 封装消息发送操作，供 UTS 侧直接调用，通过轮询 getResultStatus 获取结果
@objc
@objcMembers
public class EMMessageBridge: NSObject {

    // MARK: - 发送文本消息

    /// 发送文本消息
    /// - Parameter paramsJson: JSON 字符串，包含 content / to / chatType / extJson / callbackId 五个字段
    @objc public static func sendTextMessage(paramsJson: String) {
        guard
            let data = paramsJson.data(using: .utf8),
            let params = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
            let content    = params["content"]    as? String,
            let to         = params["to"]         as? String,
            let chatType   = params["chatType"]   as? String,
            let callbackId = params["callbackId"] as? String
        else {
            NSLog("[EMMessageBridge] sendTextMessage: invalid paramsJson")
            return
        }
        let extJson = params["extJson"] as? String ?? ""
        NSLog("[EMMessageBridge] sendTextMessage start, to: %@, callbackId: %@", to, callbackId)

        // 构建消息体
        let body = EMTextMessageBody(text: content)

        // 构建消息
        let fromUser = EMClient.shared().currentUsername ?? ""
        let message = EMChatMessage(
            conversationID: to,
            from: fromUser,
            to: to,
            body: body,
            ext: nil
        )

        // 设置会话类型
        switch chatType {
        case "group":
            message.chatType = .groupChat
        case "chatroom":
            message.chatType = .chatRoom
        default:
            message.chatType = .chat
        }

        // 设置扩展字段
        if !extJson.isEmpty, extJson != "null" {
            if let data = extJson.data(using: .utf8),
               let dict = try? JSONSerialization.jsonObject(with: data) as? [String: Any] {
                message.ext = dict
            }
        }

        // 发送消息
        EMClient.shared().chatManager?.send(message, progress: nil) { sentMessage, error in
            if let error = error {
                NSLog("[EMMessageBridge] sendTextMessage error: %d %@", error.code.rawValue, error.errorDescription ?? "")
                setMsgResult([
                    "status": "error",
                    "code": Int(error.code.rawValue),
                    "message": error.errorDescription ?? "Send message failed"
                ], for: callbackId)
            } else if let msg = sentMessage {
                NSLog("[EMMessageBridge] sendTextMessage success, msgId: %@", msg.messageId)
                var chatTypeStr = "single"
                switch msg.chatType {
                case .groupChat:
                    chatTypeStr = "group"
                case .chatRoom:
                    chatTypeStr = "chatroom"
                default:
                    chatTypeStr = "single"
                }

                // 序列化 ext
                var extJsonResult = "{}"
                if let ext = msg.ext,
                   let data = try? JSONSerialization.data(withJSONObject: ext),
                   let str = String(data: data, encoding: .utf8) {
                    extJsonResult = str
                }

                setMsgResult([
                    "status": "success",
                    "msgId": msg.messageId,
                    "from": msg.from ?? "",
                    "to": msg.to ?? "",
                    "type": "txt",
                    "chatType": chatTypeStr,
                    "timestamp": Int(msg.timestamp),
                    "extJson": extJsonResult
                ], for: callbackId)
            }
        }
    }

    // MARK: - 发送图片消息

    /// 发送图片消息
    /// - Parameter paramsJson: JSON 字符串，包含 filePath / sendOriginalImage / to / chatType / extJson / callbackId
    @objc public static func sendImageMessage(paramsJson: String) {
        guard
            let data = paramsJson.data(using: .utf8),
            let params = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
            let filePath       = params["filePath"]   as? String,
            let to             = params["to"]         as? String,
            let chatType       = params["chatType"]   as? String,
            let callbackId     = params["callbackId"] as? String
        else {
            NSLog("[EMMessageBridge] sendImageMessage: invalid paramsJson")
            return
        }
        let sendOriginal = params["sendOriginalImage"] as? Bool ?? false
        let extJson = params["extJson"] as? String ?? ""
        NSLog("[EMMessageBridge] sendImageMessage start, to: %@, callbackId: %@", to, callbackId)

        // 将 file:// 路径转换为本地绝对路径
        let localPath: String
        if filePath.hasPrefix("file://") {
            localPath = String(filePath.dropFirst(7))
        } else {
            localPath = filePath
        }

        let body = EMImageMessageBody(localPath: localPath, displayName: URL(fileURLWithPath: localPath).lastPathComponent)
        // sendOriginalImage=true 时设 compressionRatio=1.0（不压缩），否则用默认压缩率 0.6
        body.compressionRatio = sendOriginal ? 1.0 : 0.6

        let fromUser = EMClient.shared().currentUsername ?? ""
        let message = EMChatMessage(
            conversationID: to,
            from: fromUser,
            to: to,
            body: body,
            ext: nil
        )
        switch chatType {
        case "group":    message.chatType = .groupChat
        case "chatroom": message.chatType = .chatRoom
        default:         message.chatType = .chat
        }
        if !extJson.isEmpty, extJson != "null" {
            if let d = extJson.data(using: .utf8),
               let dict = try? JSONSerialization.jsonObject(with: d) as? [String: Any] {
                message.ext = dict
            }
        }

        EMClient.shared().chatManager?.send(message, progress: { progress in
            NSLog("[EMMessageBridge] sendImageMessage progress: %d, callbackId: %@", progress, callbackId)
            setMsgProgress(Int(progress), for: callbackId)
        }) { sentMessage, error in
            // 注意：不在此处 removeMsgProgress，由 UTS 侧 clearResult 统一清理
            // 避免竞态：progress 被提前删除导致 UTS 轮询读不到最终进度
            if let error = error {
                NSLog("[EMMessageBridge] sendImageMessage error: %d %@", error.code.rawValue, error.errorDescription ?? "")
                setMsgResult(["status": "error", "code": Int(error.code.rawValue), "message": error.errorDescription ?? "Send image failed"], for: callbackId)
            } else if let msg = sentMessage {
                NSLog("[EMMessageBridge] sendImageMessage success, msgId: %@", msg.messageId)
                var chatTypeStr = "single"
                switch msg.chatType {
                case .groupChat: chatTypeStr = "group"
                case .chatRoom:  chatTypeStr = "chatroom"
                default:         chatTypeStr = "single"
                }
                var extJsonResult = "{}"
                if let ext = msg.ext,
                   let d = try? JSONSerialization.data(withJSONObject: ext),
                   let s = String(data: d, encoding: .utf8) { extJsonResult = s }
                setMsgResult([
                    "status": "success", "msgId": msg.messageId,
                    "from": msg.from ?? "", "to": msg.to ?? "",
                    "type": "img", "chatType": chatTypeStr,
                    "timestamp": Int(msg.timestamp), "extJson": extJsonResult
                ], for: callbackId)
            }
        }
    }

    // MARK: - 发送视频消息

    /// 发送视频消息
    /// - Parameter paramsJson: JSON 字符串，包含 videoFilePath / imageThumbPath / timeLength / to / chatType / callbackId
    @objc public static func sendVideoMessage(paramsJson: String) {
        guard
            let data = paramsJson.data(using: .utf8),
            let params = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
            let videoFilePath = params["videoFilePath"] as? String,
            let to            = params["to"]            as? String,
            let chatType      = params["chatType"]      as? String,
            let callbackId    = params["callbackId"]    as? String
        else {
            NSLog("[EMMessageBridge] sendVideoMessage: invalid paramsJson")
            return
        }
        let timeLength = params["timeLength"] as? Int ?? 0
        let imageThumbPath = params["imageThumbPath"] as? String ?? ""
        NSLog("[EMMessageBridge] sendVideoMessage start, to: %@, callbackId: %@", to, callbackId)

        let localVideoPath: String = videoFilePath.hasPrefix("file://") ? String(videoFilePath.dropFirst(7)) : videoFilePath
        let localThumbPath: String = imageThumbPath.hasPrefix("file://") ? String(imageThumbPath.dropFirst(7)) : imageThumbPath

        let body = EMVideoMessageBody(
            localPath: localVideoPath,
            displayName: URL(fileURLWithPath: localVideoPath).lastPathComponent
        )
        body.duration = Int32(timeLength)
        if !localThumbPath.isEmpty {
            body.thumbnailLocalPath = localThumbPath
        }

        let fromUser = EMClient.shared().currentUsername ?? ""
        let message = EMChatMessage(
            conversationID: to,
            from: fromUser,
            to: to,
            body: body,
            ext: nil
        )
        switch chatType {
        case "group":    message.chatType = .groupChat
        case "chatroom": message.chatType = .chatRoom
        default:         message.chatType = .chat
        }

        EMClient.shared().chatManager?.send(message, progress: { progress in
            NSLog("[EMMessageBridge] sendVideoMessage progress: %d, callbackId: %@", progress, callbackId)
            setMsgProgress(Int(progress), for: callbackId)
        }) { sentMessage, error in
            // 注意：不在此处 removeMsgProgress，由 UTS 侧 clearResult 统一清理
            if let error = error {
                NSLog("[EMMessageBridge] sendVideoMessage error: %d %@", error.code.rawValue, error.errorDescription ?? "")
                setMsgResult(["status": "error", "code": Int(error.code.rawValue), "message": error.errorDescription ?? "Send video failed"], for: callbackId)
            } else if let msg = sentMessage {
                NSLog("[EMMessageBridge] sendVideoMessage success, msgId: %@", msg.messageId)
                var chatTypeStr = "single"
                switch msg.chatType {
                case .groupChat: chatTypeStr = "group"
                case .chatRoom:  chatTypeStr = "chatroom"
                default:         chatTypeStr = "single"
                }
                setMsgResult([
                    "status": "success", "msgId": msg.messageId,
                    "from": msg.from ?? "", "to": msg.to ?? "",
                    "type": "video", "chatType": chatTypeStr,
                    "timestamp": Int(msg.timestamp), "extJson": "{}"
                ], for: callbackId)
            }
        }
    }

    // MARK: - 发送 CMD 消息

    /// 发送透传（CMD）消息
    /// - Parameter paramsJson: JSON 字符串，包含 action / to / chatType / extJson / callbackId
    @objc public static func sendCmdMessage(paramsJson: String) {
        guard
            let data = paramsJson.data(using: .utf8),
            let params = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
            let action     = params["action"]     as? String,
            let to         = params["to"]         as? String,
            let chatType   = params["chatType"]   as? String,
            let callbackId = params["callbackId"] as? String
        else {
            NSLog("[EMMessageBridge] sendCmdMessage: invalid paramsJson")
            return
        }
        let extJson = params["extJson"] as? String ?? ""
        NSLog("[EMMessageBridge] sendCmdMessage start, action: %@, to: %@, callbackId: %@", action, to, callbackId)

        let body = EMCmdMessageBody(action: action)

        let fromUser = EMClient.shared().currentUsername ?? ""
        let message = EMChatMessage(
            conversationID: to,
            from: fromUser,
            to: to,
            body: body,
            ext: nil
        )
        switch chatType {
        case "group":    message.chatType = .groupChat
        case "chatroom": message.chatType = .chatRoom
        default:         message.chatType = .chat
        }
        if !extJson.isEmpty, extJson != "null" {
            if let d = extJson.data(using: .utf8),
               let dict = try? JSONSerialization.jsonObject(with: d) as? [String: Any] {
                message.ext = dict
            }
        }

        EMClient.shared().chatManager?.send(message, progress: nil) { sentMessage, error in
            if let error = error {
                NSLog("[EMMessageBridge] sendCmdMessage error: %d %@", error.code.rawValue, error.errorDescription ?? "")
                setMsgResult(["status": "error", "code": Int(error.code.rawValue), "message": error.errorDescription ?? "Send cmd failed"], for: callbackId)
            } else if let msg = sentMessage {
                NSLog("[EMMessageBridge] sendCmdMessage success, msgId: %@", msg.messageId)
                var chatTypeStr = "single"
                switch msg.chatType {
                case .groupChat: chatTypeStr = "group"
                case .chatRoom:  chatTypeStr = "chatroom"
                default:         chatTypeStr = "single"
                }
                setMsgResult([
                    "status": "success", "msgId": msg.messageId,
                    "from": msg.from ?? "", "to": msg.to ?? "",
                    "type": "cmd", "chatType": chatTypeStr,
                    "timestamp": Int(msg.timestamp), "extJson": "{}"
                ], for: callbackId)
            }
        }
    }

    // MARK: - 发送自定义消息

    /// 发送自定义消息
    /// - Parameter paramsJson: JSON 字符串，包含 event / paramsJson(业务参数) / to / chatType / extJson / callbackId
    @objc public static func sendCustomMessage(paramsJson: String) {
        guard
            let data = paramsJson.data(using: .utf8),
            let params = try? JSONSerialization.jsonObject(with: data) as? [String: Any],
            let event      = params["event"]      as? String,
            let to         = params["to"]         as? String,
            let chatType   = params["chatType"]   as? String,
            let callbackId = params["callbackId"] as? String
        else {
            NSLog("[EMMessageBridge] sendCustomMessage: invalid paramsJson")
            return
        }
        let customParamsJson = params["customParams"] as? String ?? "{}"
        let extJson = params["extJson"] as? String ?? ""
        NSLog("[EMMessageBridge] sendCustomMessage start, event: %@, to: %@, callbackId: %@", event, to, callbackId)

        // 解析业务参数
        var customParams: [String: String] = [:]
        if let d = customParamsJson.data(using: .utf8),
           let dict = try? JSONSerialization.jsonObject(with: d) as? [String: Any] {
            for (k, v) in dict {
                customParams[k] = "\(v)"
            }
        }

        let body = EMCustomMessageBody(event: event, customExt: customParams)

        let fromUser = EMClient.shared().currentUsername ?? ""
        let message = EMChatMessage(
            conversationID: to,
            from: fromUser,
            to: to,
            body: body,
            ext: nil
        )
        switch chatType {
        case "group":    message.chatType = .groupChat
        case "chatroom": message.chatType = .chatRoom
        default:         message.chatType = .chat
        }
        if !extJson.isEmpty, extJson != "null" {
            if let d = extJson.data(using: .utf8),
               let dict = try? JSONSerialization.jsonObject(with: d) as? [String: Any] {
                message.ext = dict
            }
        }

        EMClient.shared().chatManager?.send(message, progress: nil) { sentMessage, error in
            if let error = error {
                NSLog("[EMMessageBridge] sendCustomMessage error: %d %@", error.code.rawValue, error.errorDescription ?? "")
                setMsgResult(["status": "error", "code": Int(error.code.rawValue), "message": error.errorDescription ?? "Send custom failed"], for: callbackId)
            } else if let msg = sentMessage {
                NSLog("[EMMessageBridge] sendCustomMessage success, msgId: %@", msg.messageId)
                var chatTypeStr = "single"
                switch msg.chatType {
                case .groupChat: chatTypeStr = "group"
                case .chatRoom:  chatTypeStr = "chatroom"
                default:         chatTypeStr = "single"
                }
                setMsgResult([
                    "status": "success", "msgId": msg.messageId,
                    "from": msg.from ?? "", "to": msg.to ?? "",
                    "type": "custom", "chatType": chatTypeStr,
                    "timestamp": Int(msg.timestamp), "extJson": "{}"
                ], for: callbackId)
            }
        }
    }

    // MARK: - UTS 侧轮询接口

    /// 查询发送状态："pending" / "success" / "error"
    @objc public static func getResultStatus(callbackId: String) -> String {
        guard let result = getMsgResult(for: callbackId) else { return "pending" }
        return result["status"] as? String ?? "pending"
    }

    /// 获取成功后的消息 ID
    @objc public static func getResultMsgId(callbackId: String) -> String {
        return getMsgResult(for: callbackId)?["msgId"] as? String ?? ""
    }

    /// 获取成功后的 from
    @objc public static func getResultFrom(callbackId: String) -> String {
        return getMsgResult(for: callbackId)?["from"] as? String ?? ""
    }

    /// 获取成功后的 to
    @objc public static func getResultTo(callbackId: String) -> String {
        return getMsgResult(for: callbackId)?["to"] as? String ?? ""
    }

    /// 获取成功后的消息类型（"txt" / "img" 等）
    @objc public static func getResultType(callbackId: String) -> String {
        return getMsgResult(for: callbackId)?["type"] as? String ?? ""
    }

    /// 获取成功后的 chatType（"single" / "group" / "chatroom"）
    @objc public static func getResultChatType(callbackId: String) -> String {
        return getMsgResult(for: callbackId)?["chatType"] as? String ?? ""
    }

    /// 获取成功后的时间戳
    @objc public static func getResultTimestamp(callbackId: String) -> Int {
        return getMsgResult(for: callbackId)?["timestamp"] as? Int ?? 0
    }

    /// 获取成功后的扩展字段 JSON 字符串
    @objc public static func getResultExtJson(callbackId: String) -> String {
        return getMsgResult(for: callbackId)?["extJson"] as? String ?? "{}"
    }

    /// 获取错误码（status == "error" 时有效）
    @objc public static func getResultCode(callbackId: String) -> Int {
        return getMsgResult(for: callbackId)?["code"] as? Int ?? 0
    }

    /// 获取错误描述（status == "error" 时有效）
    @objc public static func getResultMessage(callbackId: String) -> String {
        return getMsgResult(for: callbackId)?["message"] as? String ?? ""
    }

    /// 获取上传进度（附件消息），返回 0-100，-1 表示无进度数据
    @objc public static func getResultProgress(callbackId: String) -> Int {
        return getMsgProgress(for: callbackId)
    }

    /// 清除结果（轮询完成后调用）
    @objc public static func clearResult(callbackId: String) {
        removeMsgResult(for: callbackId)
        removeMsgProgress(for: callbackId)
    }
}
