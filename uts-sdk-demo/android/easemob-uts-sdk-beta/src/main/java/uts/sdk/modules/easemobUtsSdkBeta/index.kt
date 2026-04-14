@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "SENSELESS_COMPARISON", "NAME_SHADOWING", "UNNECESSARY_NOT_NULL_ASSERTION")
package uts.sdk.modules.easemobUtsSdkBeta
import com.hyphenate.EMCallBack
import com.hyphenate.EMConnectionListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMOptions
import io.dcloud.uniapp.*
import io.dcloud.uniapp.extapi.*
import io.dcloud.uniapp.framework.*
import io.dcloud.uniapp.runtime.*
import io.dcloud.uniapp.vue.*
import io.dcloud.uniapp.vue.shared.*
import io.dcloud.uts.*
import io.dcloud.uts.Map
import io.dcloud.uts.Set
import io.dcloud.uts.UTSAndroid
import kotlin.properties.Delegates
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
open class EMError (
    @JsonNotNull
    open var code: Number,
    @JsonNotNull
    open var message: String,
) : UTSObject()
typealias EMErrorListener = (error: EMError) -> Unit
typealias EMTokenWillExpireListener = () -> Unit
typealias EMTokenExpiredListener = () -> Unit
open class ConnectionListenerCallbacks (
    open var onConnected: (() -> Unit)? = null,
    open var onDisconnected: ((errorCode: Number) -> Unit)? = null,
    open var onLogout: ((errorCode: Number) -> Unit)? = null,
    open var onTokenWillExpire: (() -> Unit)? = null,
    open var onTokenExpired: (() -> Unit)? = null,
    open var onOfflineMessageSyncStart: (() -> Unit)? = null,
    open var onOfflineMessageSyncFinish: (() -> Unit)? = null,
) : UTSObject()
interface IEMClient {
    fun onConnected(listener: (() -> Unit)?)
    fun onDisconnected(listener: ((errorCode: Number) -> Unit)?)
    fun onLogout(listener: ((errorCode: Number) -> Unit)?)
    fun onError(listener: EMErrorListener?)
    fun onTokenWillExpire(listener: EMTokenWillExpireListener?)
    fun onTokenExpired(listener: EMTokenExpiredListener?)
    fun initSDK(config: UTSJSONObject): UTSPromise<Unit>
    fun login(config: UTSJSONObject): UTSPromise<Unit>
    fun logout(): UTSPromise<Unit>
    fun destroy()
    fun getVersion(): String
    fun isConnected(): Boolean
    fun isLoggedIn(): Boolean
    fun getCurrentUser(): String?
}
open class EMConnectionListenerImpl : EMConnectionListener {
    open var eventMap: ConnectionListenerCallbacks = ConnectionListenerCallbacks()
    override fun onConnected(): Unit {
        console.log("[Android] native onConnected fired, hasCallback=" + (this.eventMap.onConnected != null))
        this.eventMap.onConnected?.invoke()
    }
    override fun onDisconnected(errorCode: Int): Unit {
        console.log("[Android] native onDisconnected fired, errorCode=" + errorCode + ", hasCallback=" + (this.eventMap.onDisconnected != null))
        this.eventMap.onDisconnected?.invoke(errorCode)
    }
    override fun onLogout(errorCode: Int): Unit {
        console.log("[Android] native onLogout fired, errorCode=" + errorCode + ", hasCallback=" + (this.eventMap.onLogout != null))
        this.eventMap.onLogout?.invoke(errorCode)
    }
    override fun onTokenWillExpire(): Unit {
        this.eventMap.onTokenWillExpire?.invoke()
    }
    override fun onTokenExpired(): Unit {
        this.eventMap.onTokenExpired?.invoke()
    }
    override fun onOfflineMessageSyncStart(): Unit {
        this.eventMap.onOfflineMessageSyncStart?.invoke()
    }
    override fun onOfflineMessageSyncFinish(): Unit {
        this.eventMap.onOfflineMessageSyncFinish?.invoke()
    }
}
open class EMCallBackImpl : EMCallBack {
    open var onSuccessHandler: (() -> Unit)? = null
    open var onErrorHandler: ((code: Number, message: String) -> Unit)? = null
    override fun onSuccess(): Unit {
        this.onSuccessHandler?.invoke()
    }
    override fun onError(code: Int, message: String): Unit {
        this.onErrorHandler?.invoke(code, message)
    }
}
fun makeCallBack(onSuccess: () -> Unit, onError: (code: Number, message: String) -> Unit): EMCallBackImpl {
    val cb = EMCallBackImpl()
    cb.onSuccessHandler = onSuccess
    cb.onErrorHandler = onError
    return cb
}
open class EMClientImpl : IEMClient {
    private var _listener: EMConnectionListenerImpl? = null
    private var _isInitialized = false
    private var _onConnected: (() -> Unit)? = null
    private var _onDisconnected: ((errorCode: Number) -> Unit)? = null
    private var _onLogout: ((errorCode: Number) -> Unit)? = null
    private var _onTokenWillExpire: (() -> Unit)? = null
    private var _onTokenExpired: (() -> Unit)? = null
    private constructor(){}
    override fun initSDK(config: UTSJSONObject): UTSPromise<Unit> {
        return UTSPromise(fun(resolve, reject){
            try {
                if (this._isInitialized) {
                    console.log("[Android] EMClient SDK already initialized")
                    resolve(Unit)
                    return
                }
                val appKey = config["appKey"] as String
                val autoLogin = (config["autoLogin"] as Boolean) ?: false
                val options = EMOptions()
                options.setAppKey(appKey)
                options.setAutoLogin(autoLogin)
                EMClient.getInstance().init(UTSAndroid.getAppContext()!!, options)
                this._listener = EMConnectionListenerImpl()
                EMClient.getInstance().addConnectionListener(this._listener!!)
                this._isInitialized = true
                console.log("[Android] EMClient SDK initialized")
                resolve(Unit)
            }
             catch (error: Throwable) {
                console.error("[Android] EMClient SDK init failed:", error)
                reject(error)
            }
        }
        )
    }
    override fun login(config: UTSJSONObject): UTSPromise<Unit> {
        return UTSPromise(fun(resolve, reject){
            try {
                val userId = config["userId"] as String
                val password = config["password"] as String
                val useToken = (config["useToken"] as Boolean) ?: false
                val callback = makeCallBack(fun(): Unit {
                    console.log("[Android] Login success:", userId)
                    resolve(Unit)
                }
                , fun(code: Number, message: String): Unit {
                    console.error("[Android] Login failed:", code, message)
                    reject(UTSError("Login failed: " + code + " - " + message))
                }
                )
                if (useToken) {
                    EMClient.getInstance().loginWithToken(userId, password, callback)
                } else {
                    EMClient.getInstance().login(userId, password, callback)
                }
            }
             catch (error: Throwable) {
                console.error("[Android] Login error:", error)
                reject(error)
            }
        }
        )
    }
    override fun logout(): UTSPromise<Unit> {
        return UTSPromise(fun(resolve, reject){
            try {
                val callback = makeCallBack(fun(): Unit {
                    console.log("[Android] Logout success")
                    resolve(Unit)
                }
                , fun(code: Number, message: String): Unit {
                    console.error("[Android] Logout failed:", code, message)
                    reject(UTSError("Logout failed: " + code + " - " + message))
                }
                )
                EMClient.getInstance().logout(true, callback)
            }
             catch (error: Throwable) {
                console.error("[Android] Logout error:", error)
                reject(error)
            }
        }
        )
    }
    override fun destroy(): Unit {
        this._listener = null
        this._isInitialized = false
        console.log("[Android] EMClient SDK destroyed")
    }
    override fun onConnected(listener: (() -> Unit)?): Unit {
        this._onConnected = listener
        if (this._listener != null) {
            this._listener!!.eventMap.onConnected = listener
        }
    }
    override fun onDisconnected(listener: ((errorCode: Number) -> Unit)?): Unit {
        this._onDisconnected = listener
        if (this._listener != null) {
            this._listener!!.eventMap.onDisconnected = listener
        }
    }
    override fun onLogout(listener: ((errorCode: Number) -> Unit)?): Unit {
        this._onLogout = listener
        if (this._listener != null) {
            this._listener!!.eventMap.onLogout = listener
        }
    }
    override fun onTokenWillExpire(listener: EMTokenWillExpireListener?): Unit {
        this._onTokenWillExpire = listener
        if (this._listener != null) {
            this._listener!!.eventMap.onTokenWillExpire = listener
        }
    }
    override fun onTokenExpired(listener: EMTokenExpiredListener?): Unit {
        this._onTokenExpired = listener
        if (this._listener != null) {
            this._listener!!.eventMap.onTokenExpired = listener
        }
    }
    override fun onError(_listener: EMErrorListener?): Unit {}
    override fun getVersion(): String {
        return "unknown"
    }
    override fun isConnected(): Boolean {
        return false
    }
    override fun isLoggedIn(): Boolean {
        return false
    }
    override fun getCurrentUser(): String? {
        return null
    }
    companion object {
        private var _instance: EMClientImpl? = null
        fun getInstance(): EMClientImpl {
            if (this._instance == null) {
                this._instance = EMClientImpl()
            }
            return this._instance!!
        }
    }
}
fun initSDK(config: UTSJSONObject): UTSPromise<Unit> {
    return EMClientImpl.getInstance().initSDK(config)
}
fun login(config: UTSJSONObject): UTSPromise<Unit> {
    return EMClientImpl.getInstance().login(config)
}
fun logout(): UTSPromise<Unit> {
    return EMClientImpl.getInstance().logout()
}
fun destroy(): Unit {
    EMClientImpl.getInstance().destroy()
}
fun onConnected(listener: (() -> Unit)?): Unit {
    EMClientImpl.getInstance().onConnected(listener)
}
fun onDisconnected(listener: ((errorCode: Number) -> Unit)?): Unit {
    EMClientImpl.getInstance().onDisconnected(listener)
}
fun onLogout(listener: ((errorCode: Number) -> Unit)?): Unit {
    EMClientImpl.getInstance().onLogout(listener)
}
fun onTokenWillExpire(listener: EMTokenWillExpireListener?): Unit {
    EMClientImpl.getInstance().onTokenWillExpire(listener)
}
fun onTokenExpired(listener: EMTokenExpiredListener?): Unit {
    EMClientImpl.getInstance().onTokenExpired(listener)
}
fun onError(_listener: EMErrorListener?): Unit {}
fun getVersion(): String {
    return "unknown"
}
fun isConnected(): Boolean {
    return false
}
fun isLoggedIn(): Boolean {
    return false
}
fun getCurrentUser(): String? {
    return null
}
