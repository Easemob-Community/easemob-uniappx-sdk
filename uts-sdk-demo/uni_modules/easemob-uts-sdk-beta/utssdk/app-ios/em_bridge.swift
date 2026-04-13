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
