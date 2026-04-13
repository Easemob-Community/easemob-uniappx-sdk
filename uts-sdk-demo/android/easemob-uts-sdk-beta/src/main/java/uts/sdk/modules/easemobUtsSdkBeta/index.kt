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
enum class EMConnectionState__1(override val value: Int) : UTSEnumInt {
    CONNECTED(0),
    CONNECTING(1),
    DISCONNECTED(2)
}
enum class EMConnectionEvent__1(override val value: Int) : UTSEnumInt {
    LOGIN_SUCCESS(0),
    LOGOUT(1),
    KICKED_BY_OTHER_DEVICE(2),
    LOGIN_FAILED(3),
    NETWORK_ERROR(4)
}
open class EMConnectionStateChangedEvent {
    open lateinit var state: EMConnectionState__1
    open lateinit var event: EMConnectionEvent__1
    open var ext: String? = null
    constructor(state: EMConnectionState__1, event: EMConnectionEvent__1, ext: String? = null){
        this.state = state
        this.event = event
        this.ext = ext
    }
}
open class EMError (
    @JsonNotNull
    open var code: Number,
    @JsonNotNull
    open var message: String,
) : UTSObject()
typealias EMConnectionStateChangedListener = (event: EMConnectionStateChangedEvent) -> Unit
typealias EMErrorListener = (error: EMError) -> Unit
typealias EMTokenWillExpireListener = () -> Unit
typealias EMTokenExpiredListener = () -> Unit
interface IEMClient {
    fun onConnectionStateChanged(listener: EMConnectionStateChangedListener?)
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
    open var onConnectionStateChanged: EMConnectionStateChangedListener? = null
    override fun onConnected(): Unit {
        val eventData = EMConnectionStateChangedEvent(EMConnectionState__1.CONNECTED, EMConnectionEvent__1.LOGIN_SUCCESS)
        this.onConnectionStateChanged?.invoke(eventData)
    }
    override fun onDisconnected(errorCode: Int): Unit {
        var event = EMConnectionEvent__1.NETWORK_ERROR
        if (errorCode == 206) {
            event = EMConnectionEvent__1.KICKED_BY_OTHER_DEVICE
        } else if (errorCode == 202 || errorCode == 204) {
            event = EMConnectionEvent__1.LOGIN_FAILED
        }
        val eventData = EMConnectionStateChangedEvent(EMConnectionState__1.DISCONNECTED, event, errorCode.toString())
        this.onConnectionStateChanged?.invoke(eventData)
    }
    override fun onTokenWillExpire(): Unit {}
    override fun onTokenExpired(): Unit {}
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
    override fun onConnectionStateChanged(listener: EMConnectionStateChangedListener?): Unit {
        if (this._listener != null) {
            this._listener!!.onConnectionStateChanged = listener
        }
    }
    override fun onError(_listener: EMErrorListener?): Unit {}
    override fun onTokenWillExpire(_listener: EMTokenWillExpireListener?): Unit {}
    override fun onTokenExpired(_listener: EMTokenExpiredListener?): Unit {}
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
fun onConnectionStateChanged(listener: EMConnectionStateChangedListener?): Unit {
    EMClientImpl.getInstance().onConnectionStateChanged(listener)
}
fun onError(_listener: EMErrorListener?): Unit {}
fun onTokenWillExpire(_listener: EMTokenWillExpireListener?): Unit {}
fun onTokenExpired(_listener: EMTokenExpiredListener?): Unit {}
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
