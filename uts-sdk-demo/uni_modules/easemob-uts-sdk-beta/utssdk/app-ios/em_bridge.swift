import HyphenateChat

// MARK: - 内部代理类（fileprivate，不暴露给 UTS，避免 declare class 冲突）
fileprivate class EMDelegateNative: NSObject, EMClientDelegate {
    var onConnectedCb: (() -> Void)?
    var onDisconnectedCb: ((NSNumber) -> Void)?
    var onLogoutCb: ((NSNumber) -> Void)?
    var onTokenWillExpireCb: (() -> Void)?
    var onTokenExpiredCb: (() -> Void)?

    public func connectionStateDidChange(_ aConnectionState: EMConnectionState) {
        if aConnectionState == EMConnectionState.connected {
            print("[iOS] native onConnected fired")
            self.onConnectedCb?()
        } else {
            print("[iOS] native onDisconnected fired")
            self.onDisconnectedCb?(0)
        }
    }

    public func autoLoginDidCompleteWithError(_ aError: EMError?) {
        if let error = aError {
            let code = error.code.rawValue as NSNumber
            print("[iOS] native autoLogin error: \(code)")
            self.onLogoutCb?(code)
        }
    }

    public func tokenWillExpire(_ aErrorCode: EMErrorCode) {
        print("[iOS] native tokenWillExpire")
        self.onTokenWillExpireCb?()
    }

    public func tokenDidExpire(_ aErrorCode: EMErrorCode) {
        print("[iOS] native tokenDidExpire")
        self.onTokenExpiredCb?()
    }

    public func onOfflineMessageSyncStart() {
        print("[iOS] native onOfflineMessageSyncStart")
    }

    public func onOfflineMessageSyncFinish() {
        print("[iOS] native onOfflineMessageSyncFinish")
    }

    public func userAccountDidLoginFromOtherDeviceWithInfo(_ info: Any) {
        print("[iOS] native loginFromOtherDevice")
        self.onDisconnectedCb?(0)
    }

    public func userAccountDidForced(toLogout aError: EMError?) {
        if let error = aError {
            let code = error.code.rawValue as NSNumber
            print("[iOS] native forcedToLogout: \(code)")
            self.onLogoutCb?(code)
        }
    }
}

// MARK: - 模块级单例
private var _emDelegate: EMDelegateNative?

// MARK: - 独立函数（UTS 直接调用，类似即构 zim.swift 的 setReadonlyProperty）

/// 创建代理并注册到 EMClient（UTS 在 initSDK 后调用）
func emBridgeSetupDelegate() {
    let d = EMDelegateNative()
    _emDelegate = d
    EMClient.shared().add(d, delegateQueue: nil)
}

/// 移除代理并销毁
func emBridgeTeardownDelegate() {
    if let d = _emDelegate {
        EMClient.shared().removeDelegate(d)
        _emDelegate = nil
    }
}

/// 设置 onConnected 回调
func emBridgeSetOnConnected(callback: (() -> Void)?) {
    _emDelegate?.onConnectedCb = callback
}

/// 设置 onDisconnected 回调
func emBridgeSetOnDisconnected(callback: ((NSNumber) -> Void)?) {
    _emDelegate?.onDisconnectedCb = callback
}

/// 设置 onLogout 回调
func emBridgeSetOnLogout(callback: ((NSNumber) -> Void)?) {
    _emDelegate?.onLogoutCb = callback
}

/// 设置 onTokenWillExpire 回调
func emBridgeSetOnTokenWillExpire(callback: (() -> Void)?) {
    _emDelegate?.onTokenWillExpireCb = callback
}

/// 设置 onTokenExpired 回调
func emBridgeSetOnTokenExpired(callback: (() -> Void)?) {
    _emDelegate?.onTokenExpiredCb = callback
}

// MARK: - 消息代理桥接

class EMMessageDelegateNative: NSObject, EMChatManagerDelegate {
    var onMessageReceivedCb: ((String) -> Void)?
    var onConversationReadCb: ((String, String) -> Void)?

    @objc public func messagesDidReceive(_ aMessages: [EMChatMessage]) {
        print("[iOS] native messagesDidReceive, count: \(aMessages.count)")
        let dictList = aMessages.map { msg -> [String: Any] in
            var bodyDict: [String: Any] = ["type": "txt", "message": NSNull()]
            if let textBody = msg.body as? EMTextMessageBody {
                bodyDict["type"] = "txt"
                bodyDict["message"] = textBody.text
            } else if let imageBody = msg.body as? EMImageMessageBody {
                bodyDict["type"] = "img"
                bodyDict["message"] = imageBody.remotePath ?? imageBody.localPath ?? NSNull()
            } else if let voiceBody = msg.body as? EMVoiceMessageBody {
                bodyDict["type"] = "voice"
                bodyDict["message"] = voiceBody.remotePath ?? voiceBody.localPath ?? NSNull()
            } else if let videoBody = msg.body as? EMVideoMessageBody {
                bodyDict["type"] = "video"
                bodyDict["message"] = videoBody.remotePath ?? videoBody.localPath ?? NSNull()
            } else if let locBody = msg.body as? EMLocationMessageBody {
                bodyDict["type"] = "location"
                bodyDict["message"] = locBody.address ?? NSNull()
            } else if let fileBody = msg.body as? EMFileMessageBody {
                bodyDict["type"] = "file"
                bodyDict["message"] = fileBody.remotePath ?? fileBody.localPath ?? NSNull()
            } else if let cmdBody = msg.body as? EMCmdMessageBody {
                bodyDict["type"] = "cmd"
                bodyDict["message"] = cmdBody.action
            } else if let customBody = msg.body as? EMCustomMessageBody {
                bodyDict["type"] = "custom"
                bodyDict["message"] = customBody.event
            }

            return [
                "msgId": msg.messageId ?? "",
                "from": msg.from ?? "",
                "to": msg.to ?? "",
                "conversationId": msg.conversationId ?? "",
                "chatType": msg.chatType.rawValue,
                "body": bodyDict
            ]
        }

        do {
            let data = try JSONSerialization.data(withJSONObject: dictList, options: [])
            if let jsonString = String(data: data, encoding: .utf8) {
                DispatchQueue.main.async {
                    self.onMessageReceivedCb?(jsonString)
                }
            }
        } catch {
            print("[iOS] JSON serialization error: \(error)")
        }
    }

    @objc public func onConversationRead(_ from: String, to: String) {
        print("[iOS] native onConversationRead fired, from: \(from), to: \(to)")
        DispatchQueue.main.async {
            self.onConversationReadCb?(from, to)
        }
    }
}

private var _emMessageDelegate: EMMessageDelegateNative?

func emBridgeSetupMessageDelegate() {
    let d = EMMessageDelegateNative()
    _emMessageDelegate = d
    EMClient.shared().chatManager?.add(d, delegateQueue: nil)
    print("[iOS] EMMessageDelegateNative registered")
}

func emBridgeTeardownMessageDelegate() {
    if let d = _emMessageDelegate {
        EMClient.shared().chatManager?.remove(d)
        _emMessageDelegate = nil
    }
}

func emBridgeSetOnMessageReceived(callback: ((String) -> Void)?) {
    _emMessageDelegate?.onMessageReceivedCb = callback
}

func emBridgeSetOnConversationRead(callback: ((String, String) -> Void)?) {
    _emMessageDelegate?.onConversationReadCb = callback
}

func emBridgeDeleteConversationFromServer(convId: String, convType: Int, isDeleteServerMessages: Bool, onSuccess: @escaping () -> Void, onError: @escaping (Int, String) -> Void) {
    guard let type = EMConversationType(rawValue: convType) else {
        onError(EMErrorCode.INVALID_PARAMS.rawValue, "invalid conversation type")
        return
    }
    EMClient.shared().chatManager?.removeConversationFromServer(convId, type: type, isDeleteMessages: isDeleteServerMessages, completion: { error in
        if let error = error {
            onError(Int(error.code.rawValue), error.errorDescription ?? "delete conversation from server failed")
        } else {
            onSuccess()
        }
    })
}

func emBridgeDeleteConversation(convId: String, withMessage: Bool) -> Bool {
    return EMClient.shared().chatManager?.deleteConversation(convId, isDeleteMessages: withMessage) ?? false
}
