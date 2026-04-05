import Foundation
import HyphenateChat

// MARK: - 结果存储（线程安全）

private let resultLock = NSLock()
private var resultMap: [String: [String: Any]] = [:]

private func setResult(_ dict: [String: Any], for operationId: String) {
    resultLock.lock()
    resultMap[operationId] = dict
    resultLock.unlock()
}

private func getResult(for operationId: String) -> [String: Any]? {
    resultLock.lock()
    defer { resultLock.unlock() }
    return resultMap[operationId]
}

private func removeResult(for operationId: String) {
    resultLock.lock()
    resultMap.removeValue(forKey: operationId)
    resultLock.unlock()
}

// MARK: - 桥接类

/// 封装 EMClient 登录/登出操作，供 UTS 侧直接调用
/// UTS 侧通过轮询 getResultStatus 获取结果，彻底避免跨语言闭包传递
@objc
@objcMembers
public class EMAuthBridge: NSObject {

    // MARK: - 密码登录

    @objc public static func loginWithPassword(userId: String, password: String, callbackId: String) {
        NSLog("[EMAuthBridge] loginWithPassword start, oid: %@", callbackId)
        EMClient.shared().login(withUsername: userId, password: password) { _, error in
            if let error = error {
                NSLog("[EMAuthBridge] loginWithPassword error: %d %@", error.code.rawValue, error.errorDescription ?? "")
                setResult([
                    "status": "error",
                    "code": Int(error.code.rawValue),
                    "message": error.errorDescription ?? "Login failed"
                ], for: callbackId)
            } else {
                NSLog("[EMAuthBridge] loginWithPassword success")
                setResult(["status": "success"], for: callbackId)
            }
        }
    }

    // MARK: - Token 登录

    @objc public static func loginWithToken(userId: String, token: String, callbackId: String) {
        NSLog("[EMAuthBridge] loginWithToken start, oid: %@", callbackId)
        EMClient.shared().login(withUsername: userId, token: token) { _, error in
            if let error = error {
                NSLog("[EMAuthBridge] loginWithToken error: %d %@", error.code.rawValue, error.errorDescription ?? "")
                setResult([
                    "status": "error",
                    "code": Int(error.code.rawValue),
                    "message": error.errorDescription ?? "Login with token failed"
                ], for: callbackId)
            } else {
                NSLog("[EMAuthBridge] loginWithToken success")
                setResult(["status": "success"], for: callbackId)
            }
        }
    }

    // MARK: - 登出

    @objc public static func logout(unbindToken: Bool, callbackId: String) {
        NSLog("[EMAuthBridge] logout start, oid: %@", callbackId)
        EMClient.shared().logout(unbindToken) { error in
            if let error = error {
                NSLog("[EMAuthBridge] logout error: %d %@", error.code.rawValue, error.errorDescription ?? "")
                setResult([
                    "status": "error",
                    "code": Int(error.code.rawValue),
                    "message": error.errorDescription ?? "Logout failed"
                ], for: callbackId)
            } else {
                NSLog("[EMAuthBridge] logout success")
                setResult(["status": "success"], for: callbackId)
            }
        }
    }

    // MARK: - UTS 侧轮询接口

    /// 查询操作状态：返回 "pending" / "success" / "error"
    @objc public static func getResultStatus(operationId: String) -> String {
        guard let result = getResult(for: operationId) else { return "pending" }
        return result["status"] as? String ?? "pending"
    }

    /// 获取错误码（仅 status == "error" 时有效）
    @objc public static func getResultCode(operationId: String) -> Int {
        return getResult(for: operationId)?["code"] as? Int ?? 0
    }

    /// 获取错误描述（仅 status == "error" 时有效）
    @objc public static func getResultMessage(operationId: String) -> String {
        return getResult(for: operationId)?["message"] as? String ?? ""
    }

    /// 清除结果（轮询完成后调用）
    @objc public static func clearResult(operationId: String) {
        removeResult(for: operationId)
    }

    // MARK: - 状态查询

    @objc public static func getCurrentUser() -> String {
        return EMClient.shared().currentUsername ?? ""
    }

    @objc public static func isConnected() -> Bool {
        return EMClient.shared().isConnected
    }

    @objc public static func isLoggedIn() -> Bool {
        return EMClient.shared().isLoggedIn
    }
}

// MARK: - 连接事件存储（线程安全）

private let connEventLock = NSLock()
/// key: listenerId, value: 待消费的事件队列
private var connEventMap: [String: [[String: Any]]] = [:]

private func appendConnEvent(_ event: [String: Any]) {
    connEventLock.lock()
    for key in connEventMap.keys {
        connEventMap[key]?.append(event)
    }
    connEventLock.unlock()
}

// MARK: - 连接监听桥接类

/// 遵循 EMClientDelegate，将事件写入字典供 UTS 轮询消费
@objc
@objcMembers
public class EMConnectionBridge: NSObject, EMClientDelegate {

    @objc public static let shared = EMConnectionBridge()

    // MARK: 注册/注销

    @objc public static func addListener(listenerId: String) {
        connEventLock.lock()
        if connEventMap[listenerId] == nil {
            connEventMap[listenerId] = []
        }
        connEventLock.unlock()
        // 确保 delegate 已注册
        EMClient.shared().add(EMConnectionBridge.shared, delegateQueue: nil)
    }

    @objc public static func removeListener(listenerId: String) {
        connEventLock.lock()
        connEventMap.removeValue(forKey: listenerId)
        connEventLock.unlock()
        if connEventMap.isEmpty {
            EMClient.shared().removeDelegate(EMConnectionBridge.shared)
        }
    }

    // MARK: 事件拉取（UTS 轮询调用）

    /// 返回下一条待处理事件的 type，无事件返回空字符串
    @objc public static func nextEventType(listenerId: String) -> String {
        connEventLock.lock()
        defer { connEventLock.unlock() }
        return connEventMap[listenerId]?.first?["type"] as? String ?? ""
    }

    /// 返回下一条事件的 errorCode（onDisconnected / onLogout 使用）
    @objc public static func nextEventCode(listenerId: String) -> Int {
        connEventLock.lock()
        defer { connEventLock.unlock() }
        return connEventMap[listenerId]?.first?["code"] as? Int ?? 0
    }

    /// 消费（移除）队列头部事件
    @objc public static func consumeEvent(listenerId: String) {
        connEventLock.lock()
        if connEventMap[listenerId]?.isEmpty == false {
            connEventMap[listenerId]?.removeFirst()
        }
        connEventLock.unlock()
    }

    // MARK: EMClientDelegate

    public func connectionStateDidChange(_ aConnectionState: EMConnectionState) {
        if aConnectionState == .connected {
            appendConnEvent(["type": "onConnected"])
        } else {
            appendConnEvent(["type": "onDisconnected", "code": 0])
        }
    }

    public func autoLoginDidCompleteWithError(_ aError: EMError?) {
        if let error = aError {
            appendConnEvent(["type": "onLogout", "code": Int(error.code.rawValue)])
        }
    }

    public func tokenWillExpire(_ aErrorCode: EMErrorCode) {
        appendConnEvent(["type": "onTokenWillExpire", "code": Int(aErrorCode.rawValue)])
    }

    public func tokenDidExpire(_ aErrorCode: EMErrorCode) {
        appendConnEvent(["type": "onTokenExpired", "code": Int(aErrorCode.rawValue)])
    }

    public func onOfflineMessageSyncStart() {
        appendConnEvent(["type": "onOfflineMessageSyncStart"])
    }

    public func onOfflineMessageSyncFinish() {
        appendConnEvent(["type": "onOfflineMessageSyncFinish"])
    }
}
