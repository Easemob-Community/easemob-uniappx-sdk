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
import io.dcloud.uniapp.extapi.`$onThread` as uni__onThread
var isInit = false
var isLogin = false
var msgListener: Any = null
fun init(appKey: String): Boolean {
    try {
        val context = UTSAndroid.getAppContext()
        if (context == null) {
            console.error("[EM] getAppContext failed")
            return false
        }
        val options = com.hyphenate.chat.EMOptions()
        options.setAppKey(appKey)
        var result = false
        var error: Any = null
        uni__onThread(fun(){
            try {
                com.hyphenate.chat.EMClient.getInstance().init(context, options)
                isInit = true
                result = true
                console.log("[EM] init success in main thread")
            }
             catch (e: Throwable) {
                error = e
                console.error("[EM] init failed in main thread:", e)
            }
        }
        )
        if (error != null) {
            throw error
        }
        return result
    }
     catch (e: Throwable) {
        console.error("[EM] init failed:", e)
        return false
    }
}
fun login(username: String, password: String, onSuccess: Any, onFail: Any): Unit {
    if (!isInit) {
        if (onFail) {
            onFail(-1, "SDK not init")
        }
        return
    }
    try {
        com.hyphenate.chat.EMClient.getInstance().login(username, password, com.hyphenate.EMCallBack(object : UTSJSONObject() {
            var onSuccess = fun(){
                isLogin = true
                com.hyphenate.chat.EMClient.getInstance().chatManager().loadAllConversations()
                if (onSuccess) {
                    onSuccess()
                }
            }
            var onError = fun(code: Number, error: String){
                if (onFail) {
                    onFail(code, error)
                }
            }
            var onProgress = fun(progress: Number, status: String){}
        }))
    }
     catch (e: Throwable) {
        if (onFail) {
            onFail(-1, String(e))
        }
    }
}
fun logout(onSuccess: Any): Unit {
    if (!isLogin) {
        if (onSuccess) {
            onSuccess()
        }
        return
    }
    try {
        com.hyphenate.chat.EMClient.getInstance().logout(true, com.hyphenate.EMCallBack(object : UTSJSONObject() {
            var onSuccess = fun(){
                isLogin = false
                if (onSuccess) {
                    onSuccess()
                }
            }
            var onError = fun(code: Number, error: String){
                if (onSuccess) {
                    onSuccess()
                }
            }
        }))
    }
     catch (e: Throwable) {
        isLogin = false
        if (onSuccess) {
            onSuccess()
        }
    }
}
fun sendText(to: String, content: String, onSuccess: Any, onFail: Any): Unit {
    if (!isLogin) {
        if (onFail) {
            onFail(-1, "not login")
        }
        return
    }
    try {
        val msg = com.hyphenate.chat.EMMessage.createTxtSendMessage(content, to)
        if (msg == null) {
            if (onFail) {
                onFail(-1, "create msg failed")
            }
            return
        }
        msg.setMessageStatusCallback(com.hyphenate.EMCallBack(object : UTSJSONObject() {
            var onSuccess = fun(){
                if (onSuccess) {
                    onSuccess()
                }
            }
            var onError = fun(code: Number, error: String){
                if (onFail) {
                    onFail(code, error)
                }
            }
        }))
        com.hyphenate.chat.EMClient.getInstance().chatManager().sendMessage(msg)
    }
     catch (e: Throwable) {
        if (onFail) {
            onFail(-1, String(e))
        }
    }
}
fun onMessage(listener: Any): Unit {
    if (!isInit) {
        return
    }
    if (msgListener != null) {
        com.hyphenate.chat.EMClient.getInstance().chatManager().removeMessageListener(msgListener)
    }
    msgListener = com.hyphenate.EMMessageListener(object : UTSJSONObject() {
        var onMessageReceived = fun(messages: UTSArray<Any>){
            if (listener == null) {
                return
            }
            for(m in resolveUTSValueIterator(messages)){
                try {
                    val body = m.getBody()
                    val type = body.javaClass.getSimpleName()
                    var content = ""
                    if (type.contains("Text")) {
                        content = (body as Any).getMessage()
                    }
                    listener(_uO("from" to m.getFrom(), "to" to m.getTo(), "content" to content, "timestamp" to m.getMsgTime()))
                }
                 catch (e: Throwable) {}
            }
        }
    })
    com.hyphenate.chat.EMClient.getInstance().chatManager().addMessageListener(msgListener)
}
fun isLoggedIn(): Boolean {
    return isLogin
}
fun getVersion(): String {
    return com.hyphenate.chat.EMClient.VERSION
}
