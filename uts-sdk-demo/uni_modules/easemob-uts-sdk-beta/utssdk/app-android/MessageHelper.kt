package uts.sdk.modules.easemobUtsSdkBeta

import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMConversation
import com.hyphenate.chat.EMClient
import com.hyphenate.EMCallBack
import com.hyphenate.EMValueCallBack
import com.hyphenate.chat.EMCursorResult
import android.util.Log
import org.json.JSONObject
import org.json.JSONArray
import io.dcloud.uts.UTSJSONObject
import io.dcloud.uts.UTSArray

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

/**
 * 从服务器分页获取会话列表
 * @param limit 每页返回的会话数
 * @param cursor 游标位置
 * @param onSuccess 成功回调
 * @param onError 失败回调
 */
fun fetchConversationsFromServer(
    limit: Int,
    cursor: String,
    onSuccess: (conversations: UTSArray<UTSJSONObject>, nextCursor: String) -> Unit,
    onError: (code: Int, message: String) -> Unit
) {
    EMClient.getInstance().chatManager().asyncFetchConversationsFromServer(
        limit,
        cursor,
        object : EMValueCallBack<EMCursorResult<EMConversation>> {
            override fun onSuccess(result: EMCursorResult<EMConversation>) {
                val conversations = result.data ?: emptyList()
                val conversationList = conversations.map { conv ->
                    conversationToUTSJSONObject(conv)
                }
                val conversationArray = UTSArray<UTSJSONObject>()
                conversationArray.addAll(conversationList)
                onSuccess(conversationArray, result.cursor ?: "")
            }

            override fun onError(error: Int, errorMsg: String) {
                onError(error, errorMsg)
            }
        }
    )
}

private fun conversationToUTSJSONObject(conv: com.hyphenate.chat.EMConversation): UTSJSONObject {
    val lastMsg = conv.getLastMessage()
    val lastMessageJson = if (lastMsg != null) {
        val body = lastMsg.getBody()
        val bodyObj = UTSJSONObject()
        when (body) {
            is com.hyphenate.chat.EMTextMessageBody -> {
                bodyObj["type"] = "txt"
                bodyObj["message"] = body.getMessage()
            }
            is com.hyphenate.chat.EMImageMessageBody -> {
                bodyObj["type"] = "img"
                bodyObj["message"] = body.getRemoteUrl() ?: body.getLocalUrl()
            }
            is com.hyphenate.chat.EMVoiceMessageBody -> {
                bodyObj["type"] = "voice"
                bodyObj["message"] = body.getRemoteUrl() ?: body.getLocalUrl()
            }
            is com.hyphenate.chat.EMVideoMessageBody -> {
                bodyObj["type"] = "video"
                bodyObj["message"] = body.getRemoteUrl() ?: body.getLocalUrl()
            }
            is com.hyphenate.chat.EMLocationMessageBody -> {
                bodyObj["type"] = "location"
                bodyObj["message"] = body.getAddress()
            }
            is com.hyphenate.chat.EMFileMessageBody -> {
                bodyObj["type"] = "file"
                bodyObj["message"] = body.getRemoteUrl() ?: body.getLocalUrl()
            }
            is com.hyphenate.chat.EMCmdMessageBody -> {
                bodyObj["type"] = "cmd"
                bodyObj["message"] = body.action()
            }
            is com.hyphenate.chat.EMCustomMessageBody -> {
                bodyObj["type"] = "custom"
                bodyObj["message"] = body.event()
            }
            else -> {
                bodyObj["type"] = "unknown"
                bodyObj["message"] = ""
            }
        }
        val msgObj = UTSJSONObject()
        msgObj["msgId"] = lastMsg.getMsgId()
        msgObj["from"] = lastMsg.getFrom()
        msgObj["to"] = lastMsg.getTo()
        msgObj["conversationId"] = lastMsg.conversationId()
        msgObj["chatType"] = lastMsg.getChatType().ordinal
        msgObj["body"] = bodyObj
        msgObj
    } else null
    val obj = UTSJSONObject()
    obj["conversationId"] = conv.conversationId()
    obj["type"] = conv.getType().ordinal
    obj["unreadMsgCount"] = conv.getUnreadMsgCount()
    obj["lastMessage"] = lastMessageJson
    return obj
}

fun getAllConversationsBySortInternal(): UTSArray<UTSJSONObject> {
    val conversations = EMClient.getInstance().chatManager().getAllConversationsBySort()
    val conversationList = conversations.map { conv ->
        conversationToUTSJSONObject(conv)
    }
    val conversationArray = UTSArray<UTSJSONObject>()
    conversationArray.addAll(conversationList)
    return conversationArray
}

fun getAllConversationsInternal(): UTSArray<UTSJSONObject> {
    val conversations = EMClient.getInstance().chatManager().getAllConversations()
    val conversationList = conversations.values.map { conv ->
        conversationToUTSJSONObject(conv)
    }
    val conversationArray = UTSArray<UTSJSONObject>()
    conversationArray.addAll(conversationList)
    return conversationArray
}

fun sendConversationReadAckInternal(
    conversationId: String,
    onSuccess: () -> Unit,
    onError: (code: Int, message: String) -> Unit
) {
    try {
        EMClient.getInstance().chatManager().ackConversationRead(conversationId)
        onSuccess()
    } catch (e: Exception) {
        Log.e("MessageHelper", "sendConversationReadAck failed", e)
        onError(com.hyphenate.EMError.GENERAL_ERROR, e.message ?: "send conversation read ack failed")
    }
}

fun deleteConversationFromServerInternal(
    convId: String,
    convType: Int,
    isDeleteServerMessages: Boolean,
    onSuccess: () -> Unit,
    onError: (code: Int, message: String) -> Unit
) {
    try {
        val type = com.hyphenate.chat.EMConversation.EMConversationType.values()[convType]
        EMClient.getInstance().chatManager().deleteConversationFromServer(
            convId,
            type,
            isDeleteServerMessages,
            object : EMCallBack {
                override fun onSuccess() {
                    onSuccess()
                }
                override fun onError(error: Int, errorMsg: String) {
                    onError(error, errorMsg)
                }
            }
        )
    } catch (e: Exception) {
        Log.e("MessageHelper", "deleteConversationFromServer failed", e)
        onError(com.hyphenate.EMError.GENERAL_ERROR, e.message ?: "delete conversation from server failed")
    }
}

fun deleteConversationInternal(convId: String, withMessage: Boolean): Boolean {
    return EMClient.getInstance().chatManager().deleteConversation(convId, withMessage)
}