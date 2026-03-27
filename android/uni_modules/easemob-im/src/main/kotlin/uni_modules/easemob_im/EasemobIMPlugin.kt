/**
 * 环信 IM SDK - Kotlin 混编实现
 * 
 * 此文件是 UTS 插件的 Kotlin 混编实现
 * 用于在没有 UTS 编译插件时直接编译
 * 
 * 功能与 utssdk/app-android/index.uts 完全一致
 */

package uni_modules.easemob_im

import com.hyphenate.EMCallBack
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMOptions
import com.hyphenate.chat.EMTextMessageBody
import io.dcloud.uts.UTSAndroid
import android.util.Log

// 全局状态
private var gInited = false
private var gLogined = false

// 存储回调函数
private var gLoginSuccess: (() -> Unit)? = null
private var gLoginFail: ((Int, String) -> Unit)? = null
private var gLogoutSuccess: (() -> Unit)? = null
private var gSendSuccess: (() -> Unit)? = null
private var gSendFail: ((Int, String) -> Unit)? = null
private var gMsgCallback: ((EMMessageData) -> Unit)? = null

// 全局监听器实例
private var gMsgListener: EMMessageListenerImpl? = null

/**
 * 消息数据类
 */
data class EMMessageData(
    val messageId: String,
    val from: String,
    val to: String,
    val content: String,
    val timestamp: Long
)

/**
 * 登录回调实现 - extends 抽象类 EMCallBack
 */
class EMLoginCallBack : EMCallBack() {
    override fun onSuccess() {
        gLogined = true
        // 登录成功后加载会话
        EMClient.getInstance().chatManager().loadAllConversations()
        val callback = gLoginSuccess
        callback?.invoke()
        gLoginSuccess = null
        gLoginFail = null
    }

    override fun onError(code: Int, error: String?) {
        val callback = gLoginFail
        callback?.invoke(code, error ?: "unknown error")
        gLoginSuccess = null
        gLoginFail = null
    }

    override fun onProgress(progress: Int, status: String?) {
        // 暂不处理进度
    }
}

/**
 * 登出回调实现
 */
class EMLogoutCallBack : EMCallBack() {
    override fun onSuccess() {
        gLogined = false
        val callback = gLogoutSuccess
        callback?.invoke()
        gLogoutSuccess = null
    }

    override fun onError(code: Int, error: String?) {
        // 即使失败也视为登出
        gLogined = false
        val callback = gLogoutSuccess
        callback?.invoke()
        gLogoutSuccess = null
    }

    override fun onProgress(progress: Int, status: String?) {
        // 暂不处理
    }
}

/**
 * 发送消息回调实现
 */
class EMSendCallBack : EMCallBack() {
    override fun onSuccess() {
        val callback = gSendSuccess
        callback?.invoke()
        gSendSuccess = null
        gSendFail = null
    }

    override fun onError(code: Int, error: String?) {
        val callback = gSendFail
        callback?.invoke(code, error ?: "unknown error")
        gSendSuccess = null
        gSendFail = null
    }

    override fun onProgress(progress: Int, status: String?) {
        // 暂不处理
    }
}

/**
 * 消息监听器实现 - extends 抽象类 EMMessageListener
 */
class EMMessageListenerImpl : EMMessageListener() {
    override fun onMessageReceived(messages: MutableList<EMMessage>?) {
        val callback = gMsgCallback
        if (callback == null || messages == null) return

        for (msg in messages) {
            try {
                val body = msg.body
                var content = ""

                // 判断是否是文本消息
                if (body is EMTextMessageBody) {
                    content = body.message
                }

                val messageData = EMMessageData(
                    messageId = msg.msgId,
                    from = msg.from,
                    to = msg.to,
                    content = content,
                    timestamp = msg.msgTime
                )

                callback.invoke(messageData)
            } catch (e: Exception) {
                Log.e("EM", "process message error: ${e.message}")
            }
        }
    }
}

/**
 * 插件主类
 */
class EasemobIMPlugin {
    
    companion object {
        private const val TAG = "EasemobIM"
        
        /**
         * 初始化 SDK
         */
        @JvmStatic
        fun init(appKey: String): Boolean {
            return try {
                val context = UTSAndroid.getAppContext()
                if (context == null) {
                    Log.e(TAG, "getAppContext is null")
                    return false
                }

                val options = EMOptions()
                options.appKey = appKey

                EMClient.getInstance().init(context, options)
                gInited = true

                Log.i(TAG, "init success")
                true
            } catch (e: Exception) {
                Log.e(TAG, "init failed: ${e.message}")
                false
            }
        }

        /**
         * 登录
         */
        @JvmStatic
        fun login(
            username: String,
            password: String,
            onSuccess: () -> Unit,
            onFail: (Int, String) -> Unit
        ) {
            if (!gInited) {
                onFail(-1, "SDK not initialized")
                return
            }

            try {
                gLoginSuccess = onSuccess
                gLoginFail = onFail
                EMClient.getInstance().login(username, password, EMLoginCallBack())
            } catch (e: Exception) {
                gLoginSuccess = null
                gLoginFail = null
                onFail(-1, e.message ?: "unknown error")
            }
        }

        /**
         * 登出
         */
        @JvmStatic
        fun logout(onSuccess: () -> Unit) {
            if (!gLogined) {
                onSuccess()
                return
            }

            try {
                gLogoutSuccess = onSuccess
                EMClient.getInstance().logout(true, EMLogoutCallBack())
            } catch (e: Exception) {
                gLogoutSuccess = null
                gLogined = false
                onSuccess()
            }
        }

        /**
         * 发送文本消息
         */
        @JvmStatic
        fun sendTextMessage(
            to: String,
            content: String,
            onSuccess: () -> Unit,
            onFail: (Int, String) -> Unit
        ) {
            if (!gLogined) {
                onFail(-1, "Not logged in")
                return
            }

            try {
                val msg = EMMessage.createTxtSendMessage(content, to)
                if (msg == null) {
                    onFail(-1, "Create message failed")
                    return
                }

                gSendSuccess = onSuccess
                gSendFail = onFail
                msg.setMessageStatusCallback(EMSendCallBack())
                EMClient.getInstance().chatManager().sendMessage(msg)
            } catch (e: Exception) {
                gSendSuccess = null
                gSendFail = null
                onFail(-1, e.message ?: "unknown error")
            }
        }

        /**
         * 设置消息监听
         */
        @JvmStatic
        fun onMessageReceived(callback: (EMMessageData) -> Unit) {
            if (!gInited) return

            // 保存回调
            gMsgCallback = callback

            // 移除旧监听器
            gMsgListener?.let {
                EMClient.getInstance().chatManager().removeMessageListener(it)
            }

            // 创建新监听器
            val newListener = EMMessageListenerImpl()
            gMsgListener = newListener
            EMClient.getInstance().chatManager().addMessageListener(newListener)
        }

        /**
         * 移除消息监听
         */
        @JvmStatic
        fun offMessageReceived() {
            gMsgListener?.let {
                EMClient.getInstance().chatManager().removeMessageListener(it)
                gMsgListener = null
                gMsgCallback = null
            }
        }

        /**
         * 是否已登录
         */
        @JvmStatic
        fun isLoggedIn(): Boolean = gLogined

        /**
         * 是否已初始化
         */
        @JvmStatic
        fun isInitialized(): Boolean = gInited

        /**
         * 获取 SDK 版本
         */
        @JvmStatic
        fun getVersion(): String = EMClient.VERSION
    }
}
