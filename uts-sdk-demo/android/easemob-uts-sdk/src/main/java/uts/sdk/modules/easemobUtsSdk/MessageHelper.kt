package uts.sdk.modules.easemobUtsSdk

import com.hyphenate.chat.EMMessage
import com.hyphenate.EMCallBack
import android.util.Log
import org.json.JSONObject
import org.json.JSONArray

/**
 * 消息发送辅助类
 * 用于解决UTS无法直接调用synchronized方法的问题
 */
object MessageHelper {

    private const val TAG = "MessageHelper"

    /**
     * 设置消息状态回调
     * 该方法在Kotlin中调用synchronized方法，绕过UTS限制
     */
    @JvmStatic
    fun setMessageStatusCallback(message: EMMessage, callback: EMCallBack) {
        Log.d(TAG, "setMessageStatusCallback 被调用, msgId=${message.msgId}")
        message.setMessageStatusCallback(callback)
        Log.d(TAG, "setMessageStatusCallback 设置完成")
    }

    /**
     * 发送消息并设置回调
     * 封装发送消息的完整流程
     */
    @JvmStatic
    fun sendMessageWithCallback(
        message: EMMessage,
        onSuccess: () -> Unit,
        onError: (Int, String) -> Unit,
        onProgress: (Int, String) -> Unit
    ) {
        Log.d(TAG, "sendMessageWithCallback 被调用")
        message.setMessageStatusCallback(object : EMCallBack {
            override fun onSuccess() {
                Log.d(TAG, "onSuccess 回调触发")
                onSuccess.invoke()
            }
            
            override fun onError(code: Int, message: String) {
                Log.d(TAG, "onError 回调触发, code=$code, message=$message")
                onError.invoke(code, message)
            }
            
            override fun onProgress(progress: Int, status: String) {
                Log.d(TAG, "onProgress 回调触发, progress=$progress")
                onProgress.invoke(progress, status)
            }
        })
    }
}

/**
 * 设置消息扩展属性
 * @param message 消息对象
 * @param extJson 扩展属性JSON字符串
 */
fun setMessageExtFromJson(message: EMMessage, extJson: String) {
    if (extJson.isEmpty()) return
    try {
        val jsonObject = JSONObject(extJson)
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            val value = jsonObject.get(key)
            when (value) {
                is String -> message.setAttribute(key, value)
                is Int -> message.setAttribute(key, value)
                is Long -> message.setAttribute(key, value)
                is Float -> message.setAttribute(key, value)
                is Double -> message.setAttribute(key, value)
                is Boolean -> message.setAttribute(key, value)
                is JSONObject -> message.setAttribute(key, value)
                is JSONArray -> message.setAttribute(key, value)
                else -> message.setAttribute(key, value.toString())
            }
        }
        Log.d("MessageHelper", "设置扩展属性成功: $extJson")
    } catch (e: Exception) {
        Log.e("MessageHelper", "设置扩展属性失败: ${e.message}")
    }
}

/**
 * 获取消息扩展属性JSON字符串
 * @param message 消息对象
 * @return 扩展属性JSON字符串
 */
fun getMessageExtAsJson(message: EMMessage): String {
    val extMap = message.ext() ?: return "{}"
    val jsonObject = JSONObject()
    try {
        for ((key, value) in extMap) {
            jsonObject.put(key, value)
        }
    } catch (e: Exception) {
        Log.e("MessageHelper", "获取扩展属性失败: ${e.message}")
    }
    return jsonObject.toString()
}
