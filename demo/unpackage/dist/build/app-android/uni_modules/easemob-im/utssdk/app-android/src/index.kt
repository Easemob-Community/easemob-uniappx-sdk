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
var gLoginSuccess: EMLoginSuccess? = null
var gLoginFail: EMLoginFail? = null
var gLogoutSuccess: (() -> Unit)? = null
var gSendSuccess: EMSendSuccess? = null
var gSendFail: EMSendFail? = null
var gMsgCallback: EMMessageCallback? = null
open class EMLoginCallBack : com.hyphenate.EMCallBack {
    constructor() : super() {}
    override fun onSuccess(): Unit {
        gLogined = true
        com.hyphenate.chat.EMClient.getInstance().chatManager().loadAllConversations()
        val callback = gLoginSuccess
        if (callback != null) {
            callback()
        }
        gLoginSuccess = null
        gLoginFail = null
    }
    override fun onError(code: Int, error: String): Unit {
        val callback = gLoginFail
        if (callback != null) {
            callback(code as Number, error)
        }
        gLoginSuccess = null
        gLoginFail = null
    }
    override fun onProgress(progress: Int, status: String): Unit {}
}
open class EMLogoutCallBack : com.hyphenate.EMCallBack {
    constructor() : super() {}
    override fun onSuccess(): Unit {
        gLogined = false
        val callback = gLogoutSuccess
        if (callback != null) {
            callback()
        }
        gLogoutSuccess = null
    }
    override fun onError(code: Int, error: String): Unit {
        gLogined = false
        val callback = gLogoutSuccess
        if (callback != null) {
            callback()
        }
        gLogoutSuccess = null
    }
    override fun onProgress(progress: Int, status: String): Unit {}
}
open class EMSendCallBack : com.hyphenate.EMCallBack {
    constructor() : super() {}
    override fun onSuccess(): Unit {
        val callback = gSendSuccess
        if (callback != null) {
            callback()
        }
        gSendSuccess = null
        gSendFail = null
    }
    override fun onError(code: Int, error: String): Unit {
        val callback = gSendFail
        if (callback != null) {
            callback(code as Number, error)
        }
        gSendSuccess = null
        gSendFail = null
    }
    override fun onProgress(progress: Int, status: String): Unit {}
}
open class EMMessageListenerImpl : com.hyphenate.EMMessageListener {
    constructor() : super() {}
    override fun onMessageReceived(messages: MutableList<com.hyphenate.chat.EMMessage>): Unit {
        val callback = gMsgCallback
        if (callback == null) {
            return
        }
        run {
            var i: Number = 0
            while(i < messages.size){
                val msg = messages.get(i)
                if (msg == null) {
                    i++
                    continue
                }
                try {
                    val body = msg.getBody()
                    var content = ""
                    if (body is com.hyphenate.chat.EMTextMessageBody) {
                        content = (body as com.hyphenate.chat.EMTextMessageBody).getMessage()
                    }
                    val message = EMMessage(messageId = msg.getMsgId(), from = msg.getFrom(), to = msg.getTo(), content = content, timestamp = msg.getMsgTime())
                    callback(message)
                }
                 catch (e: Throwable) {
                    console.error("[EM] process message error:", e)
                }
                i++
            }
        }
    }
}
var gMsgListener: EMMessageListenerImpl? = null
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
        gLoginSuccess = onSuccess
        gLoginFail = onFail
        com.hyphenate.chat.EMClient.getInstance().login(username, password, EMLoginCallBack())
    }
     catch (e: Throwable) {
        gLoginSuccess = null
        gLoginFail = null
        val errorMsg = if (e != null) {
            (e as UTSError).message
        } else {
            "unknown error"
        }
        onFail(-1, errorMsg)
    }
}
fun logout(onSuccess: () -> Unit): Unit {
    if (gLogined == false) {
        onSuccess()
        return
    }
    try {
        gLogoutSuccess = onSuccess
        com.hyphenate.chat.EMClient.getInstance().logout(true, EMLogoutCallBack())
    }
     catch (e: Throwable) {
        gLogoutSuccess = null
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
        gSendSuccess = onSuccess
        gSendFail = onFail
        msg.setMessageStatusCallback(EMSendCallBack())
        com.hyphenate.chat.EMClient.getInstance().chatManager().sendMessage(msg)
    }
     catch (e: Throwable) {
        gSendSuccess = null
        gSendFail = null
        val errorMsg = if (e != null) {
            (e as UTSError).message
        } else {
            "unknown error"
        }
        onFail(-1, errorMsg)
    }
}
fun onMessageReceived(callback: EMMessageCallback): Unit {
    if (gInited == false) {
        return
    }
    gMsgCallback = callback
    if (gMsgListener != null) {
        com.hyphenate.chat.EMClient.getInstance().chatManager().removeMessageListener(gMsgListener)
    }
    gMsgListener = EMMessageListenerImpl()
    com.hyphenate.chat.EMClient.getInstance().chatManager().addMessageListener(gMsgListener)
}
fun offMessageReceived(): Unit {
    if (gMsgListener != null) {
        com.hyphenate.chat.EMClient.getInstance().chatManager().removeMessageListener(gMsgListener)
        gMsgListener = null
        gMsgCallback = null
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
