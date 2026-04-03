package uts.sdk.modules.easemobUtsSdk

import com.hyphenate.chat.EMMessage
import com.hyphenate.EMCallBack
import com.hyphenate.chat.EMCustomMessageBody
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

/**
 * 将JSON字符串转换为Map<String, String>
 * 用于自定义消息的params参数
 * @param paramsJson params的JSON字符串
 * @return Map<String, String>
 */
fun parseCustomParamsFromJson(paramsJson: String): Map<String, String> {
    val result = mutableMapOf<String, String>()
    if (paramsJson.isEmpty()) return result
    try {
        val jsonObject = JSONObject(paramsJson)
        val keys = jsonObject.keys()
        while (keys.hasNext()) {
            val key = keys.next()
            result[key] = jsonObject.getString(key)
        }
    } catch (e: Exception) {
        Log.e("MessageHelper", "解析自定义消息params失败: ${e.message}")
    }
    return result
}

/**
 * 将Map<String, String>转换为JSON字符串
 * 用于获取自定义消息的params
 * @param params Map<String, String>
 * @return JSON字符串
 */
fun convertCustomParamsToJson(params: Map<String, String>?): String {
    if (params == null || params.isEmpty()) return "{}"
    val jsonObject = JSONObject()
    try {
        for ((key, value) in params) {
            jsonObject.put(key, value)
        }
    } catch (e: Exception) {
        Log.e("MessageHelper", "转换自定义消息params失败: ${e.message}")
    }
    return jsonObject.toString()
}

/**
 * 创建并设置自定义消息体
 * @param event 自定义事件名称
 * @param paramsJson params的JSON字符串
 * @return EMCustomMessageBody
 */
fun createCustomMessageBody(event: String, paramsJson: String): EMCustomMessageBody {
    val customBody = EMCustomMessageBody(event)
    if (paramsJson.isNotEmpty() && paramsJson != "{}") {
        try {
            val jsonObject = JSONObject(paramsJson)
            val params = mutableMapOf<String, String>()
            val keys = jsonObject.keys()
            while (keys.hasNext()) {
                val key = keys.next()
                params[key] = jsonObject.getString(key)
            }
            customBody.setParams(params)
        } catch (e: Exception) {
            Log.e("MessageHelper", "创建自定义消息体失败: ${e.message}")
        }
    }
    return customBody
}

/**
 * 获取自定义消息体的event
 * @param body 自定义消息体
 * @return event字符串
 */
fun getCustomMessageEvent(body: EMCustomMessageBody): String {
    return body.event() ?: ""
}

/**
 * 获取自定义消息体的params并转为JSON字符串
 * @param body 自定义消息体
 * @return params的JSON字符串
 */
fun getCustomMessageParamsAsJson(body: EMCustomMessageBody): String {
    val params = body.getParams()
    return convertCustomParamsToJson(params)
}
