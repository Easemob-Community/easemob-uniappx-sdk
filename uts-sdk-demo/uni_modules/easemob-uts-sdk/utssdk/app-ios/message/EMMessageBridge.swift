import Foundation
import HyphenateChat

// MARK: - 线程安全的结果存储（复用 Auth 侧的设计模式）

private let msgResultLock = NSLock()
/// key: callbackId, value: { "status": "success"/"error", "msgId": ..., "from": ..., "to": ..., "type": ..., "chatType": ..., "timestamp": ..., "code": ..., "message": ... }
private var msgResultMap: [String: [String: Any]] = [:]

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

    /// 清除结果（轮询完成后调用）
    @objc public static func clearResult(callbackId: String) {
        removeMsgResult(for: callbackId)
    }
}
