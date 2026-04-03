package uts.sdk.modules.easemobUtsSdk

import com.hyphenate.chat.EMMessage
import com.hyphenate.EMCallBack
import android.util.Log
import org.json.JSONObject
import org.json.JSONArray

/**
 * 消息信息数据类 - 前端友好的消息结构
 * 包含消息核心字段
 */
data class MessageInfo(
    val msgId: String,
    val from: String,
    val to: String,
    val type: String,
    val chatType: String,
    val timestamp: Long
) {
    companion object {
        fun fromEMMessage(message: EMMessage): MessageInfo {
            return MessageInfo(
                msgId = message.msgId,
                from = message.from,
                to = message.to,
                type = message.type.name,
                chatType = when (message.chatType) {
                    EMMessage.ChatType.Chat -> "single"
                    EMMessage.ChatType.GroupChat -> "group"
                    EMMessage.ChatType.ChatRoom -> "chatroom"
                    else -> "unknown"
                },
                timestamp = message.msgTime
            )
        }
    }
}

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
     * 从 EMMessage 创建 MessageInfo
     * 供 UTS 层调用，返回前端友好的消息对象
     */
    @JvmStatic
    fun createMessageInfo(message: EMMessage): MessageInfo {
        return MessageInfo.fromEMMessage(message)
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
