@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "NAME_SHADOWING", "UNNECESSARY_NOT_NULL_ASSERTION")
package uts.sdk.modules.easemobIm
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
typealias EMLoginSuccess = () -> Unit
typealias EMLoginFail = (code: Number, message: String) -> Unit
typealias EMSendSuccess = () -> Unit
typealias EMSendFail = (code: Number, message: String) -> Unit
typealias EMMessageCallback = (message: EMMessage) -> Unit
interface EMMessage {
    var messageId: String
    var from: String
    var to: String
    var content: String
    var timestamp: Number
}
var gInited = false
var gLogined = false
var gMsgListener: com.hyphenate.EMMessageListener? = null
open class EMCallBackImpl : com.hyphenate.EMCallBack {
    private var successFn: (() -> Unit)?
    private var failFn: ((code: Number, error: String) -> Unit)?
    constructor(success: (() -> Unit)?, fail: ((code: Number, error: String) -> Unit)?){
        this.successFn = success
        this.failFn = fail
    }
    open fun onSuccess(): Unit {
        if (this.successFn != null) {
            this.successFn!!()
        }
    }
    open fun onError(code: Number, error: String): Unit {
        if (this.failFn != null) {
            this.failFn!!(code, error)
        }
    }
    open fun onProgress(progress: Number, status: String): Unit {}
}
open class EMMessageListenerImpl : com.hyphenate.EMMessageListener {
    private var callback: EMMessageCallback?
    constructor(callback: EMMessageCallback?){
        this.callback = callback
    }
    open fun onMessageReceived(messages: UTSArray<com.hyphenate.chat.EMMessage>): Unit {
        if (this.callback == null) {
            return
        }
        for(msg in resolveUTSValueIterator(messages)){
            try {
                val body = msg.getBody()
                var content = ""
                if (body is com.hyphenate.chat.EMTextMessageBody) {
                    content = (body as com.hyphenate.chat.EMTextMessageBody).getMessage()
                }
                val message = EMMessage(messageId = msg.getMsgId(), from = msg.getFrom(), to = msg.getTo(), content = content, timestamp = msg.getMsgTime())
                this.callback!!(message)
            }
             catch (e: Throwable) {
                console.error("[EM] process message error:", e)
            }
        }
    }
}
fun init(appKey: String): Boolean {
    try {
        val context = UTSAndroid.getAppContext()
        if (context == null) {
            console.error("[EM] getAppContext is null")
            return false
        }
        val options = com.hyphenate.chat.EMOptions()
        options.setAppKey(appKey)
        com.hyphenate.chat.EMClient.getInstance().init(context, options)
        gInited = true
        console.log("[EM] init success")
        return true
    }
     catch (e: Throwable) {
        console.error("[EM] init failed:", e)
        return false
    }
}
fun login(username: String, password: String, onSuccess: EMLoginSuccess, onFail: EMLoginFail): Unit {
    if (gInited == false) {
        onFail(-1, "SDK not initialized")
        return
    }
    try {
        val callback = EMCallBackImpl(fun(){
            gLogined = true
            com.hyphenate.chat.EMClient.getInstance().chatManager().loadAllConversations()
            onSuccess()
        }
        , fun(code: Number, error: String){
            onFail(code, error)
        }
        )
        com.hyphenate.chat.EMClient.getInstance().login(username, password, callback)
    }
     catch (e: Throwable) {
        onFail(-1, String(e))
    }
}
fun logout(onSuccess: () -> Unit): Unit {
    if (gLogined == false) {
        onSuccess()
        return
    }
    try {
        val callback = EMCallBackImpl(fun(){
            gLogined = false
            onSuccess()
        }
        , null)
        com.hyphenate.chat.EMClient.getInstance().logout(true, callback)
    }
     catch (e: Throwable) {
        gLogined = false
        onSuccess()
    }
}
fun sendTextMessage(to: String, content: String, onSuccess: EMSendSuccess, onFail: EMSendFail): Unit {
    if (gLogined == false) {
        onFail(-1, "Not logged in")
        return
    }
    try {
        val msg = com.hyphenate.chat.EMMessage.createTxtSendMessage(content, to)
        if (msg == null) {
            onFail(-1, "Create message failed")
            return
        }
        val callback = EMCallBackImpl(fun(){
            onSuccess()
        }
        , fun(code: Number, error: String){
            onFail(code, error)
        }
        )
        msg.setMessageStatusCallback(callback)
        com.hyphenate.chat.EMClient.getInstance().chatManager().sendMessage(msg)
    }
     catch (e: Throwable) {
        onFail(-1, String(e))
    }
}
fun onMessageReceived(callback: EMMessageCallback): Unit {
    if (gInited == false) {
        return
    }
    callback
    if (gMsgListener != null) {
        com.hyphenate.chat.EMClient.getInstance().chatManager().removeMessageListener(gMsgListener)
    }
    gMsgListener = EMMessageListenerImpl(callback)
    com.hyphenate.chat.EMClient.getInstance().chatManager().addMessageListener(gMsgListener)
}
fun offMessageReceived(): Unit {
    if (gMsgListener != null) {
        com.hyphenate.chat.EMClient.getInstance().chatManager().removeMessageListener(gMsgListener)
        gMsgListener = null
        null
    }
}
fun isLoggedIn(): Boolean {
    return gLogined
}
fun isInitialized(): Boolean {
    return gInited
}
fun getVersion(): String {
    return com.hyphenate.chat.EMClient.VERSION
}
