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
