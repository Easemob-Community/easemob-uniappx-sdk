@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "SENSELESS_COMPARISON", "NAME_SHADOWING", "UNNECESSARY_NOT_NULL_ASSERTION")
package uts.sdk.modules.easemobUtsSdk
import android.content.Intent
import android.net.Uri
import com.hyphenate.EMCallBack
import com.hyphenate.EMConnectionListener
import com.hyphenate.EMMessageListener
import com.hyphenate.chat.EMClient
import com.hyphenate.chat.EMCmdMessageBody
import com.hyphenate.chat.EMCustomMessageBody
import com.hyphenate.chat.EMFileMessageBody
import com.hyphenate.chat.EMGroupReadAck
import com.hyphenate.chat.EMImageMessageBody
import com.hyphenate.chat.EMLocationMessageBody
import com.hyphenate.chat.EMMessage
import com.hyphenate.chat.EMMessagePinInfo
import com.hyphenate.chat.EMMessageReactionChange
import com.hyphenate.chat.EMOptions
import com.hyphenate.chat.EMRecallMessageInfo
import com.hyphenate.chat.EMTextMessageBody
import com.hyphenate.chat.EMVideoMessageBody
import com.hyphenate.chat.EMVoiceMessageBody
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
import io.dcloud.uniapp.extapi.`$emit` as uni__emit
import com.hyphenate.chat.EMMessage.ChatType as EMMessageChatType
import uts.sdk.modules.easemobUtsSdk.FilePickerHelper
import uts.sdk.modules.easemobUtsSdk.FilePickResult
import uts.sdk.modules.easemobUtsSdk.FilePickerCallback
import uts.sdk.modules.easemobUtsSdk.MessageHelper
import uts.sdk.modules.easemobUtsSdk.getMessageExtAsJson
import uts.sdk.modules.easemobUtsSdk.getCustomMessageEvent
import uts.sdk.modules.easemobUtsSdk.getCustomMessageParamsAsJson
import uts.sdk.modules.easemobUtsSdk.setMessageExtFromJson
import uts.sdk.modules.easemobUtsSdk.createCustomMessageBody
fun initEMClient(appkey: String): Unit {
    val options = EMOptions()
    options.setAppKey(appkey)
    options.setAutoLogin(false)
    console.log(options.getAppKey(), "getappkey")
    EMClient.getInstance().init(UTSAndroid.getAppContext(), options)
}
open class ConnectionListenerCallbacks (
    open var onConnected: (() -> Unit)? = null,
    open var onDisconnected: ((errorCode: Number) -> Unit)? = null,
    open var onLogout: ((errorCode: Number) -> Unit)? = null,
    open var onTokenWillExpire: (() -> Unit)? = null,
    open var onTokenExpired: (() -> Unit)? = null,
    open var onOfflineMessageSyncStart: (() -> Unit)? = null,
    open var onOfflineMessageSyncFinish: (() -> Unit)? = null,
) : UTSObject()
open class InnerConnectionListener : EMConnectionListener {
    private var callbacks: ConnectionListenerCallbacks
    constructor(callbacks: ConnectionListenerCallbacks) : super() {
        this.callbacks = callbacks
    }
    override fun onConnected(): Unit {
        this.callbacks.onConnected?.invoke()
    }
    override fun onDisconnected(errorCode: Int): Unit {
        this.callbacks.onDisconnected?.invoke(errorCode)
    }
    override fun onLogout(errorCode: Int): Unit {
        this.callbacks.onLogout?.invoke(errorCode)
    }
    override fun onTokenWillExpire(): Unit {
        this.callbacks.onTokenWillExpire?.invoke()
    }
    override fun onTokenExpired(): Unit {
        this.callbacks.onTokenExpired?.invoke()
    }
    override fun onOfflineMessageSyncStart(): Unit {
        this.callbacks.onOfflineMessageSyncStart?.invoke()
    }
    override fun onOfflineMessageSyncFinish(): Unit {
        this.callbacks.onOfflineMessageSyncFinish?.invoke()
    }
}
val listenerMap = Map<String, EMConnectionListener>()
fun addConnectionListenerImpl(listenerId: String, listener: ConnectionListenerCallbacks): Unit {
    val androidListener = InnerConnectionListener(listener)
    listenerMap.set(listenerId, androidListener)
    EMClient.getInstance().addConnectionListener(androidListener)
}
fun removeConnectionListenerImpl(listenerId: String): Unit {
    val androidListener = listenerMap.get(listenerId)
    if (androidListener != null) {
        EMClient.getInstance().removeConnectionListener(androidListener)
        listenerMap.`delete`(listenerId)
    }
}
typealias MessageType = String
val MessageTypeValues: UTSJSONObject = _uO("TXT" to "txt", "IMAGE" to "img", "VIDEO" to "video", "LOCATION" to "location", "VOICE" to "voice", "FILE" to "file", "CMD" to "cmd", "CUSTOM" to "custom")
open class MessageBody (
    @JsonNotNull
    open var type: String,
    open var message: String? = null,
    open var remoteUrl: String? = null,
    open var localUrl: String? = null,
    open var thumbnailRemoteUrl: String? = null,
    open var thumbnailLocalUrl: String? = null,
    open var width: Number? = null,
    open var height: Number? = null,
    open var fileSize: Number? = null,
    open var length: Number? = null,
    open var address: String? = null,
    open var latitude: Number? = null,
    open var longitude: Number? = null,
    open var fileName: String? = null,
    open var action: String? = null,
    open var customEvent: String? = null,
    open var customParams: UTSJSONObject? = null,
) : UTSObject()
open class Message (
    @JsonNotNull
    open var msgId: String,
    @JsonNotNull
    open var from: String,
    @JsonNotNull
    open var to: String,
    @JsonNotNull
    open var conversationId: String,
    @JsonNotNull
    open var chatType: Number,
    @JsonNotNull
    open var direction: Number,
    @JsonNotNull
    open var status: Number,
    @JsonNotNull
    open var isRead: Boolean = false,
    @JsonNotNull
    open var isAcked: Boolean = false,
    @JsonNotNull
    open var isDelivered: Boolean = false,
    @JsonNotNull
    open var localTime: Number,
    @JsonNotNull
    open var serverTime: Number,
    @JsonNotNull
    open var body: MessageBody,
    @JsonNotNull
    open var ext: UTSJSONObject,
) : UTSObject()
open class GroupReadAck (
    @JsonNotNull
    open var msgId: String,
    @JsonNotNull
    open var ackId: String,
    @JsonNotNull
    open var from: String,
    @JsonNotNull
    open var content: String,
    @JsonNotNull
    open var count: Number,
    @JsonNotNull
    open var timestamp: Number,
) : UTSObject()
open class RecallMessageInfo (
    open var recallMessage: Message? = null,
    @JsonNotNull
    open var recallBy: String,
    @JsonNotNull
    open var recallExt: String,
) : UTSObject()
open class MessageReactionChange (
    @JsonNotNull
    open var conversationId: String,
    @JsonNotNull
    open var messageId: String,
    @JsonNotNull
    open var from: String,
    @JsonNotNull
    open var reaction: String,
    @JsonNotNull
    open var count: Number,
    @JsonNotNull
    open var isAdded: Boolean = false,
    @JsonNotNull
    open var userList: UTSArray<String>,
) : UTSObject()
open class MessagePinInfo (
    @JsonNotNull
    open var operatorId: String,
    @JsonNotNull
    open var pinTime: Number,
) : UTSObject()
open class MessageListenerCallbacks (
    open var onMessageReceived: ((messages: UTSArray<Message>) -> Unit)? = null,
    open var onStreamMessageReceived: ((messages: UTSArray<Message>) -> Unit)? = null,
    open var onCmdMessageReceived: ((messages: UTSArray<Message>) -> Unit)? = null,
    open var onMessageRead: ((messages: UTSArray<Message>) -> Unit)? = null,
    open var onGroupMessageRead: ((groupReadAcks: UTSArray<GroupReadAck>) -> Unit)? = null,
    open var onReadAckForGroupMessageUpdated: (() -> Unit)? = null,
    open var onMessageDelivered: ((messages: UTSArray<Message>) -> Unit)? = null,
    open var onMessageRecalled: ((messages: UTSArray<Message>) -> Unit)? = null,
    open var onMessageRecalledWithExt: ((recallMessageInfo: UTSArray<RecallMessageInfo>) -> Unit)? = null,
    open var onMessageChanged: ((message: Message, change: Any) -> Unit)? = null,
    open var onReactionChanged: ((messageReactionChangeList: UTSArray<MessageReactionChange>) -> Unit)? = null,
    open var onMessageContentChanged: ((messageModified: Message, operatorId: String, operationTime: Long) -> Unit)? = null,
    open var onMessagePinChanged: ((messageId: String, conversationId: String, pinOperation: Number, pinInfo: MessagePinInfo) -> Unit)? = null,
) : UTSObject()
fun getMessageExtFromEMMessage(msg: EMMessage): UTSJSONObject {
    try {
        val extJson = getMessageExtAsJson(msg)
        if (extJson != null && extJson.length > 2) {
            return JSON.parse(extJson) as UTSJSONObject
        }
    }
     catch (e: Throwable) {
        console.log("[EMMessage] 获取扩展属性失败:", e)
    }
    return UTSJSONObject()
}
fun convertEMMessage(msg: EMMessage): Message {
    val body = msg.getBody()
    var messageBody = MessageBody(type = "txt", message = null, remoteUrl = null, localUrl = null, thumbnailRemoteUrl = null, thumbnailLocalUrl = null, width = null, height = null, fileSize = null, length = null, address = null, latitude = null, longitude = null, fileName = null, action = null, customEvent = null, customParams = null)
    if (body is EMTextMessageBody) {
        messageBody.type = "txt"
        messageBody.message = body.getMessage()
    } else if (body is EMImageMessageBody) {
        messageBody.type = "img"
        messageBody.remoteUrl = body.getRemoteUrl() ?: ""
        messageBody.localUrl = body.getLocalUrl() ?: ""
        messageBody.thumbnailRemoteUrl = body.getThumbnailUrl() ?: ""
        messageBody.thumbnailLocalUrl = body.thumbnailLocalPath() ?: ""
        messageBody.width = body.getWidth()
        messageBody.height = body.getHeight()
        messageBody.fileSize = body.getFileSize()
    } else if (body is EMVoiceMessageBody) {
        messageBody.type = "voice"
        messageBody.remoteUrl = body.getRemoteUrl() ?: ""
        messageBody.localUrl = body.getLocalUrl() ?: ""
        messageBody.length = body.getLength()
        messageBody.fileSize = body.getFileSize()
    } else if (body is EMVideoMessageBody) {
        messageBody.type = "video"
        messageBody.remoteUrl = body.getRemoteUrl() ?: ""
        messageBody.localUrl = body.getLocalUrl() ?: ""
        messageBody.thumbnailRemoteUrl = body.getThumbnailUrl() ?: ""
        messageBody.thumbnailLocalUrl = body.getLocalThumb() ?: ""
        messageBody.length = body.getDuration()
        messageBody.fileSize = body.getVideoFileLength()
    } else if (body is EMLocationMessageBody) {
        messageBody.type = "location"
        messageBody.address = body.getAddress() ?: ""
        messageBody.latitude = body.getLatitude()
        messageBody.longitude = body.getLongitude()
    } else if (body is EMFileMessageBody) {
        messageBody.type = "file"
        messageBody.remoteUrl = body.getRemoteUrl() ?: ""
        messageBody.localUrl = body.getLocalUrl() ?: ""
        messageBody.fileName = body.getFileName() ?: ""
        messageBody.fileSize = 0
    } else if (body is EMCmdMessageBody) {
        messageBody.type = "cmd"
        messageBody.action = body.action() ?: ""
    } else if (body is EMCustomMessageBody) {
        messageBody.type = "custom"
        messageBody.customEvent = getCustomMessageEvent(body)
        val paramsJson = getCustomMessageParamsAsJson(body)
        if (paramsJson.length > 2) {
            messageBody.customParams = JSON.parse(paramsJson) as UTSJSONObject
        }
    }
    return Message(msgId = msg.getMsgId() ?: "", from = msg.getFrom() ?: "", to = msg.getTo() ?: "", conversationId = msg.conversationId() ?: "", chatType = msg.getChatType().ordinal, direction = msg.direct().ordinal, status = msg.status().ordinal, isRead = !msg.isUnread(), isAcked = msg.isAcked(), isDelivered = msg.isDelivered(), localTime = msg.localTime(), serverTime = msg.getMsgTime(), body = messageBody, ext = getMessageExtFromEMMessage(msg))
}
fun convertGroupReadAck(ack: EMGroupReadAck): GroupReadAck {
    return GroupReadAck(msgId = ack.getMsgId() ?: "", ackId = ack.getAckId() ?: "", from = ack.getFrom() ?: "", content = ack.getContent() ?: "", count = ack.getCount(), timestamp = ack.getTimestamp())
}
fun convertRecallMessageInfo(info: EMRecallMessageInfo): RecallMessageInfo {
    val recallMsg = info.getRecallMessage()
    return RecallMessageInfo(recallMessage = if (recallMsg != null) {
        convertEMMessage(recallMsg)
    } else {
        null
    }
    , recallBy = info.getRecallBy() ?: "", recallExt = info.getExt() ?: "")
}
fun convertMessageReactionChange(change: EMMessageReactionChange): MessageReactionChange {
    val reactionList = change.getMessageReactionList()
    val reaction = if (reactionList.size > 0) {
        reactionList.get(0)
    } else {
        null
    }
    return MessageReactionChange(conversationId = change.getConversionID() ?: "", messageId = change.getMessageId() ?: "", from = "", reaction = reaction?.getReaction() ?: "", count = reaction?.getUserCount() ?: 0, isAdded = reaction?.isAddedBySelf() ?: false, userList = _uA())
}
fun convertMessagePinInfo(info: EMMessagePinInfo): MessagePinInfo {
    return MessagePinInfo(operatorId = info.operatorId() ?: "", pinTime = info.pinTime())
}
open class InnerMessageListener : EMMessageListener {
    private var callbacks: MessageListenerCallbacks
    constructor(callbacks: MessageListenerCallbacks) : super() {
        this.callbacks = callbacks
    }
    override fun onMessageReceived(messages: MutableList<EMMessage>): Unit {
        if (this.callbacks.onMessageReceived != null) {
            val msgList: UTSArray<Message> = _uA()
            run {
                var i: Int = 0
                while(i < messages.size){
                    msgList.push(convertEMMessage(messages.get(i)))
                    i++
                }
            }
            this.callbacks.onMessageReceived!!(msgList)
        }
    }
    override fun onStreamMessageReceived(messages: MutableList<EMMessage>): Unit {
        if (this.callbacks.onStreamMessageReceived != null) {
            val msgList: UTSArray<Message> = _uA()
            run {
                var i: Int = 0
                while(i < messages.size){
                    msgList.push(convertEMMessage(messages.get(i)))
                    i++
                }
            }
            this.callbacks.onStreamMessageReceived!!(msgList)
        }
    }
    override fun onCmdMessageReceived(messages: MutableList<EMMessage>): Unit {
        if (this.callbacks.onCmdMessageReceived != null) {
            val msgList: UTSArray<Message> = _uA()
            run {
                var i: Int = 0
                while(i < messages.size){
                    msgList.push(convertEMMessage(messages.get(i)))
                    i++
                }
            }
            this.callbacks.onCmdMessageReceived!!(msgList)
        }
    }
    override fun onMessageRead(messages: MutableList<EMMessage>): Unit {
        if (this.callbacks.onMessageRead != null) {
            val msgList: UTSArray<Message> = _uA()
            run {
                var i: Int = 0
                while(i < messages.size){
                    msgList.push(convertEMMessage(messages.get(i)))
                    i++
                }
            }
            this.callbacks.onMessageRead!!(msgList)
        }
    }
    override fun onGroupMessageRead(groupReadAcks: MutableList<EMGroupReadAck>): Unit {
        if (this.callbacks.onGroupMessageRead != null) {
            val ackList: UTSArray<GroupReadAck> = _uA()
            run {
                var i: Int = 0
                while(i < groupReadAcks.size){
                    ackList.push(convertGroupReadAck(groupReadAcks.get(i)))
                    i++
                }
            }
            this.callbacks.onGroupMessageRead!!(ackList)
        }
    }
    override fun onReadAckForGroupMessageUpdated(): Unit {
        this.callbacks.onReadAckForGroupMessageUpdated?.invoke()
    }
    override fun onMessageDelivered(messages: MutableList<EMMessage>): Unit {
        if (this.callbacks.onMessageDelivered != null) {
            val msgList: UTSArray<Message> = _uA()
            run {
                var i: Int = 0
                while(i < messages.size){
                    msgList.push(convertEMMessage(messages.get(i)))
                    i++
                }
            }
            this.callbacks.onMessageDelivered!!(msgList)
        }
    }
    override fun onMessageRecalled(messages: MutableList<EMMessage>): Unit {
        if (this.callbacks.onMessageRecalled != null) {
            val msgList: UTSArray<Message> = _uA()
            run {
                var i: Int = 0
                while(i < messages.size){
                    msgList.push(convertEMMessage(messages.get(i)))
                    i++
                }
            }
            this.callbacks.onMessageRecalled!!(msgList)
        }
    }
    override fun onMessageRecalledWithExt(recallMessageInfo: MutableList<EMRecallMessageInfo>): Unit {
        if (this.callbacks.onMessageRecalledWithExt != null) {
            val infoList: UTSArray<RecallMessageInfo> = _uA()
            run {
                var i: Int = 0
                while(i < recallMessageInfo.size){
                    infoList.push(convertRecallMessageInfo(recallMessageInfo.get(i)))
                    i++
                }
            }
            this.callbacks.onMessageRecalledWithExt!!(infoList)
        }
    }
    override fun onMessageChanged(message: EMMessage, change: Any): Unit {
        if (this.callbacks.onMessageChanged != null) {
            this.callbacks.onMessageChanged!!(convertEMMessage(message), change)
        }
    }
    override fun onReactionChanged(messageReactionChangeList: MutableList<EMMessageReactionChange>): Unit {
        if (this.callbacks.onReactionChanged != null) {
            val changeList: UTSArray<MessageReactionChange> = _uA()
            run {
                var i: Int = 0
                while(i < messageReactionChangeList.size){
                    changeList.push(convertMessageReactionChange(messageReactionChangeList.get(i)))
                    i++
                }
            }
            this.callbacks.onReactionChanged!!(changeList)
        }
    }
    override fun onMessageContentChanged(messageModified: EMMessage, operatorId: String, operationTime: Long): Unit {
        if (this.callbacks.onMessageContentChanged != null) {
            this.callbacks.onMessageContentChanged!!(convertEMMessage(messageModified), operatorId, operationTime)
        }
    }
    override fun onMessagePinChanged(messageId: String, conversationId: String, pinOperation: EMMessagePinInfo.PinOperation, pinInfo: EMMessagePinInfo): Unit {
        if (this.callbacks.onMessagePinChanged != null) {
            this.callbacks.onMessagePinChanged!!(messageId, conversationId, pinOperation.ordinal, convertMessagePinInfo(pinInfo))
        }
    }
}
val listenerMap__1 = Map<String, EMMessageListener>()
fun addMessageListenerImpl(listenerId: String, callbacks: MessageListenerCallbacks): Unit {
    val androidListener = InnerMessageListener(callbacks)
    listenerMap__1.set(listenerId, androidListener)
    EMClient.getInstance().chatManager().addMessageListener(androidListener)
}
fun removeMessageListenerImpl(listenerId: String): Unit {
    val androidListener = listenerMap__1.get(listenerId)
    if (androidListener != null) {
        EMClient.getInstance().chatManager().removeMessageListener(androidListener)
        listenerMap__1.`delete`(listenerId)
    }
}
val messageCallbacks: Map<String, UTSJSONObject> = Map()
fun generateCallbackId(): String {
    return Date.now().toString(10) + Math.random().toString(36).substring(2, 11)
}
open class MessageSendCallBack : EMCallBack {
    private var callbackId: String
    private var message: EMMessage? = null
    constructor(callbackId: String, message: EMMessage? = null) : super() {
        this.callbackId = callbackId
        this.message = message
    }
    override fun onError(code: Int, message: String): Unit {
        val callback = messageCallbacks.get(this.callbackId)
        if (callback != null) {
            val onError = callback["onError"]
            if (onError != null) {
                (onError as (code: Number, message: String) -> Unit)(code, message)
            }
            messageCallbacks.`delete`(this.callbackId)
        }
        console.log("[EMMessage] 消息发送失败, code:", code, "message:", message)
    }
    override fun onProgress(progress: Int, status: String): Unit {
        val callback = messageCallbacks.get(this.callbackId)
        if (callback != null) {
            val onProgress = callback["onProgress"]
            if (onProgress != null) {
                (onProgress as (progress: Number, status: String) -> Unit)(progress, status)
            }
        }
    }
    override fun onSuccess(): Unit {
        val callback = messageCallbacks.get(this.callbackId)
        if (callback != null) {
            val onSuccess = callback["onSuccess"]
            val msg = this.message
            if (onSuccess != null && msg != null) {
                val messageInfo = UTSJSONObject()
                messageInfo["msgId"] = msg.getMsgId()
                messageInfo["from"] = msg.getFrom()
                messageInfo["to"] = msg.getTo()
                messageInfo["type"] = msg.getType().name
                val chatType = msg.getChatType()
                if (chatType == EMMessageChatType.GroupChat) {
                    messageInfo["chatType"] = "group"
                } else if (chatType == EMMessageChatType.ChatRoom) {
                    messageInfo["chatType"] = "chatroom"
                } else {
                    messageInfo["chatType"] = "single"
                }
                messageInfo["timestamp"] = msg.getMsgTime()
                messageInfo["ext"] = getMessageExt(msg)
                (onSuccess as (messageInfo: UTSJSONObject) -> Unit)(messageInfo)
            }
            messageCallbacks.`delete`(this.callbackId)
        }
        val msg = this.message
        console.log("[EMMessage] 消息发送成功, ID:", if (msg != null) {
            msg.getMsgId()
        } else {
            "unknown"
        }
        )
    }
}
fun setMessageCallback(message: EMMessage, callbackId: String): Unit {
    val callback = MessageSendCallBack(callbackId, message)
    MessageHelper.setMessageStatusCallback(message, callback)
}
fun setEMMessageChatType(message: EMMessage, chatType: String): Unit {
    if (chatType === "group") {
        message.setChatType(EMMessageChatType.GroupChat)
    } else if (chatType === "chatroom") {
        message.setChatType(EMMessageChatType.ChatRoom)
    } else {
        message.setChatType(EMMessageChatType.Chat)
    }
}
fun setMessageExt(message: EMMessage, ext: UTSJSONObject?): Unit {
    if (ext == null) {
        return
    }
    val extJson = JSON.stringify(ext)
    setMessageExtFromJson(message, extJson)
}
fun getMessageExt(message: EMMessage): UTSJSONObject {
    try {
        val extJson = getMessageExtAsJson(message)
        if (extJson != null && extJson.length > 2) {
            return JSON.parse(extJson) as UTSJSONObject
        }
    }
     catch (e: Throwable) {
        console.log("[EMMessage] 获取扩展属性失败:", e)
    }
    return UTSJSONObject()
}
fun sendTextMessageImpl(content: String, to: String, chatType: String, callback: UTSJSONObject, ext: UTSJSONObject? = null): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createTxtSendMessage(content, to)
    setEMMessageChatType(message, chatType)
    setMessageExt(message, ext)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendImageMessageImpl(filePath: String, sendOriginalImage: Boolean, to: String, chatType: String, callback: UTSJSONObject, ext: UTSJSONObject? = null): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createImageSendMessage(filePath, sendOriginalImage, to)
    setEMMessageChatType(message, chatType)
    setMessageExt(message, ext)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendImageMessageWithUriImpl(imageUri: Uri, sendOriginalImage: Boolean, to: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createImageSendMessage(imageUri, sendOriginalImage, to)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendGifImageMessageImpl(gifFilePath: String, to: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createGifImageMessage(gifFilePath, to)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendVoiceMessageImpl(filePath: String, timeLength: Int, to: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createVoiceSendMessage(filePath, timeLength, to)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendVoiceMessageWithUriImpl(fileUri: Uri, timeLength: Int, to: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createVoiceSendMessage(fileUri, timeLength, to)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendVideoMessageImpl(videoFilePath: String, imageThumbPath: String, timeLength: Int, to: String, chatType: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createVideoSendMessage(videoFilePath, imageThumbPath, timeLength, to)
    setEMMessageChatType(message, chatType)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendVideoMessageWithUriImpl(videoUri: Uri, imageThumbPath: String, timeLength: Int, to: String, chatType: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createVideoSendMessage(videoUri, imageThumbPath, timeLength, to)
    setEMMessageChatType(message, chatType)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendVideoMessageWithUrisImpl(videoUri: Uri, thumbUri: Uri, timeLength: Int, to: String, chatType: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createVideoSendMessage(videoUri, thumbUri, timeLength, to)
    setEMMessageChatType(message, chatType)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendLocationMessageImpl(latitude: Double, longitude: Double, locationAddress: String, buildingName: String, to: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, buildingName, to)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendLocationMessageSimpleImpl(latitude: Double, longitude: Double, locationAddress: String, to: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createLocationSendMessage(latitude, longitude, locationAddress, to)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendFileMessageImpl(filePath: String, to: String, chatType: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createFileSendMessage(filePath, to)
    setEMMessageChatType(message, chatType)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendFileMessageWithUriImpl(fileUri: Uri, to: String, chatType: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createFileSendMessage(fileUri, to)
    setEMMessageChatType(message, chatType)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendCmdMessageImpl(action: String, to: String, callback: UTSJSONObject, ext: UTSJSONObject? = null): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createSendMessage(EMMessage.Type.CMD)
    message.setTo(to)
    val body = EMCmdMessageBody(action)
    message.addBody(body)
    setMessageExt(message, ext)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendCustomMessageImpl(event: String, params: UTSJSONObject, to: String, chatType: String, callback: UTSJSONObject, ext: UTSJSONObject? = null): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val message = EMMessage.createSendMessage(EMMessage.Type.CUSTOM)
    message.setTo(to)
    val paramsJson = JSON.stringify(params)
    val customBody = createCustomMessageBody(event, paramsJson)
    message.addBody(customBody)
    setEMMessageChatType(message, chatType)
    setMessageExt(message, ext)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun sendCombinedMessageImpl(title: String, summary: String, compatibleText: String, messageIdList: UTSArray<String>, to: String, callback: UTSJSONObject): Unit {
    val callbackId = generateCallbackId()
    messageCallbacks.set(callbackId, callback)
    val msgIdList = java.util.ArrayList<String>()
    run {
        var i: Number = 0
        while(i < messageIdList.length){
            msgIdList.add(messageIdList[i])
            i++
        }
    }
    val message = EMMessage.createCombinedSendMessage(title, summary, compatibleText, msgIdList, to)
    setMessageCallback(message, callbackId)
    EMClient.getInstance().chatManager().sendMessage(message)
}
fun downloadAttachmentImpl(message: Message, callback: UTSJSONObject): Unit {
    val emMessage = EMClient.getInstance().chatManager().getMessage(message.msgId)
    if (emMessage != null) {
        val callbackId = generateCallbackId()
        messageCallbacks.set(callbackId, callback)
        setMessageCallback(emMessage, callbackId)
        EMClient.getInstance().chatManager().downloadAttachment(emMessage)
    }
}
fun downloadThumbnailImpl(message: Message, callback: UTSJSONObject): Unit {
    val emMessage = EMClient.getInstance().chatManager().getMessage(message.msgId)
    if (emMessage != null) {
        val callbackId = generateCallbackId()
        messageCallbacks.set(callbackId, callback)
        setMessageCallback(emMessage, callbackId)
        EMClient.getInstance().chatManager().downloadThumbnail(emMessage)
    }
}
val loginCallbacks: Map<String, UTSJSONObject> = Map()
fun loginEMClient(userId: String, password: String, callback: UTSJSONObject): Unit {
    val callbackId = Date.now().toString(10) + Math.random().toString(36).substring(2, 11)
    loginCallbacks.set(callbackId, callback)
    EMClient.getInstance().login(userId, password, LoginCallBack(callbackId))
}
fun loginWithToken(username: String, token: String, callback: UTSJSONObject): Unit {
    val callbackId = Date.now().toString(10) + Math.random().toString(36).substring(2, 11)
    loginCallbacks.set(callbackId, callback)
    EMClient.getInstance().loginWithToken(username, token, LoginCallBack(callbackId))
}
fun loginWithAgoraToken(username: String, agoraToken: String, callback: UTSJSONObject): Unit {
    val callbackId = Date.now().toString(10) + Math.random().toString(36).substring(2, 11)
    loginCallbacks.set(callbackId, callback)
    EMClient.getInstance().loginWithAgoraToken(username, agoraToken, LoginCallBack(callbackId))
}
fun logoutSync(unbindToken: Boolean): Number {
    return EMClient.getInstance().logout(unbindToken)
}
fun logoutEMClient(unbindToken: Boolean, callback: UTSJSONObject): Unit {
    EMClient.getInstance().logout(unbindToken, LogoutCallBack(callback))
}
fun queryCurrentUser(): String {
    val user = EMClient.getInstance().getCurrentUser()
    return if (user != null) {
        user
    } else {
        ""
    }
}
fun queryIsLoggedInBefore(): Boolean {
    return EMClient.getInstance().isLoggedInBefore()
}
fun queryIsConnected(): Boolean {
    return EMClient.getInstance().isConnected()
}
fun queryIsLoggedIn(): Boolean {
    return EMClient.getInstance().isLoggedIn()
}
open class LoginCallBack : EMCallBack {
    private var callbackId: String
    constructor(callbackId: String) : super() {
        this.callbackId = callbackId
    }
    override fun onError(code: Int, message: String): Unit {
        val callback = loginCallbacks.get(this.callbackId)
        if (callback != null) {
            val onError = callback["onError"]
            if (onError != null) {
                (onError as (code: Number, message: String) -> Unit)(code, message)
            }
            loginCallbacks.`delete`(this.callbackId)
        }
        console.log("[EMLogin] 登录失败, code:", code, "message:", message)
    }
    override fun onProgress(code: Int, message: String): Unit {
        val callback = loginCallbacks.get(this.callbackId)
        if (callback != null) {
            val onProgress = callback["onProgress"]
            if (onProgress != null) {
                (onProgress as (code: Number, message: String) -> Unit)(code, message)
            }
        }
    }
    override fun onSuccess(): Unit {
        val callback = loginCallbacks.get(this.callbackId)
        if (callback != null) {
            val onSuccess = callback["onSuccess"]
            if (onSuccess != null) {
                (onSuccess as () -> Unit)()
            }
            loginCallbacks.`delete`(this.callbackId)
        }
        console.log("[EMLogin] 登录成功")
    }
}
open class LogoutCallBack : EMCallBack {
    private var callback: UTSJSONObject
    constructor(callback: UTSJSONObject) : super() {
        this.callback = callback
    }
    override fun onError(code: Int, message: String): Unit {
        val onError = this.callback["onError"]
        if (onError != null) {
            (onError as (code: Number, message: String) -> Unit)(code, message)
        }
        console.log("[EMLogout] 登出失败, code:", code, "message:", message)
    }
    override fun onSuccess(): Unit {
        val onSuccess = this.callback["onSuccess"]
        if (onSuccess != null) {
            (onSuccess as () -> Unit)()
        }
        console.log("[EMLogout] 登出成功")
    }
}
val REQUEST_CODE_PICK_FILE: Int = 10001
var activityResultCallback: ((requestCode: Int, resultCode: Int, data: Intent?) -> Unit)? = null
val filePickerCallbacks: Map<String, UTSJSONObject> = Map()
fun generatePickerCallbackId(): String {
    return Date.now().toString(10) + Math.random().toString(36).substring(2, 9)
}
open class FilePickResultData {
    open var filePath: String = ""
    open var fileName: String = ""
    open var fileSize: Number = 0
    open var mimeType: String = ""
}
open class InnerFilePickerCallback : FilePickerCallback {
    private var callbackId: String
    constructor(callbackId: String) : super() {
        this.callbackId = callbackId
    }
    override fun onSuccess(result: FilePickResult): Unit {
        val callback = filePickerCallbacks.get(this.callbackId)
        if (callback != null) {
            val onSuccess = callback["onSuccess"]
            if (onSuccess != null) {
                val resultData = FilePickResultData()
                resultData.filePath = result.filePath
                resultData.fileName = result.fileName
                resultData.fileSize = result.fileSize as Number
                resultData.mimeType = result.mimeType
                (onSuccess as (result: FilePickResultData) -> Unit)(resultData)
            }
            filePickerCallbacks.`delete`(this.callbackId)
        }
    }
    override fun onError(code: Int, message: String): Unit {
        val callback = filePickerCallbacks.get(this.callbackId)
        if (callback != null) {
            val onError = callback["onError"]
            if (onError != null) {
                (onError as (code: Number, message: String) -> Unit)(code as Number, message as String)
            }
            filePickerCallbacks.`delete`(this.callbackId)
        }
    }
    override fun onCancel(): Unit {
        val callback = filePickerCallbacks.get(this.callbackId)
        if (callback != null) {
            val onCancel = callback["onCancel"]
            if (onCancel != null) {
                (onCancel as () -> Unit)()
            }
            filePickerCallbacks.`delete`(this.callbackId)
        }
    }
}
fun registerActivityResultCallback(): Unit {
    if (activityResultCallback == null) {
        activityResultCallback = fun(requestCode: Int, resultCode: Int, data: Intent?): Unit {
            if (requestCode == REQUEST_CODE_PICK_FILE) {
                FilePickerHelper.handleActivityResult(requestCode, resultCode, data)
            }
        }
        UTSAndroid.onAppActivityResult(activityResultCallback!!)
    }
}
fun openFilePickerImpl(callback: UTSJSONObject): Unit {
    val callbackId = generatePickerCallbackId()
    filePickerCallbacks.set(callbackId, callback)
    registerActivityResultCallback()
    val innerCallback = InnerFilePickerCallback(callbackId)
    FilePickerHelper.openFilePicker(innerCallback)
}
fun handleFilePickerResult(requestCode: Number, resultCode: Number, data: Intent?): Boolean {
    return FilePickerHelper.handleActivityResult(requestCode as Int, resultCode as Int, data)
}
val EM_CONNECTION_CONNECTED = "em:connection:connected"
val EM_CONNECTION_DISCONNECTED = "em:connection:disconnected"
val EM_CONNECTION_LOGOUT = "em:connection:logout"
val EM_CONNECTION_TOKEN_WILL_EXPIRE = "em:connection:token_will_expire"
val EM_CONNECTION_TOKEN_EXPIRED = "em:connection:token_expired"
val EM_CONNECTION_OFFLINE_SYNC_START = "em:connection:offline_sync_start"
val EM_CONNECTION_OFFLINE_SYNC_FINISH = "em:connection:offline_sync_finish"
val EM_MESSAGE_RECEIVED = "em:message:received"
val EM_MESSAGE_CMD_RECEIVED = "em:message:cmd_received"
val EM_MESSAGE_STREAM_RECEIVED = "em:message:stream_received"
val EM_MESSAGE_READ = "em:message:read"
val EM_MESSAGE_GROUP_READ = "em:message:group_read"
val EM_MESSAGE_GROUP_READ_ACK_UPDATED = "em:message:group_read_ack_updated"
val EM_MESSAGE_DELIVERED = "em:message:delivered"
val EM_MESSAGE_RECALLED = "em:message:recalled"
val EM_MESSAGE_CONTENT_CHANGED = "em:message:content_changed"
val EM_MESSAGE_REACTION_CHANGED = "em:message:reaction_changed"
val EM_MESSAGE_PIN_CHANGED = "em:message:pin_changed"
open class EMConnectionEventType (
    @JsonNotNull
    open var CONNECTED: String,
    @JsonNotNull
    open var DISCONNECTED: String,
    @JsonNotNull
    open var LOGOUT: String,
    @JsonNotNull
    open var TOKEN_WILL_EXPIRE: String,
    @JsonNotNull
    open var TOKEN_EXPIRED: String,
    @JsonNotNull
    open var OFFLINE_SYNC_START: String,
    @JsonNotNull
    open var OFFLINE_SYNC_FINISH: String,
) : UTSObject()
val EMConnectionEvent = EMConnectionEventType(CONNECTED = EM_CONNECTION_CONNECTED, DISCONNECTED = EM_CONNECTION_DISCONNECTED, LOGOUT = EM_CONNECTION_LOGOUT, TOKEN_WILL_EXPIRE = EM_CONNECTION_TOKEN_WILL_EXPIRE, TOKEN_EXPIRED = EM_CONNECTION_TOKEN_EXPIRED, OFFLINE_SYNC_START = EM_CONNECTION_OFFLINE_SYNC_START, OFFLINE_SYNC_FINISH = EM_CONNECTION_OFFLINE_SYNC_FINISH)
open class EMMessageEventType (
    @JsonNotNull
    open var RECEIVED: String,
    @JsonNotNull
    open var CMD_RECEIVED: String,
    @JsonNotNull
    open var STREAM_RECEIVED: String,
    @JsonNotNull
    open var READ: String,
    @JsonNotNull
    open var GROUP_READ: String,
    @JsonNotNull
    open var GROUP_READ_ACK_UPDATED: String,
    @JsonNotNull
    open var DELIVERED: String,
    @JsonNotNull
    open var RECALLED: String,
    @JsonNotNull
    open var CONTENT_CHANGED: String,
    @JsonNotNull
    open var REACTION_CHANGED: String,
    @JsonNotNull
    open var PIN_CHANGED: String,
) : UTSObject()
val EMMessageEvent = EMMessageEventType(RECEIVED = EM_MESSAGE_RECEIVED, CMD_RECEIVED = EM_MESSAGE_CMD_RECEIVED, STREAM_RECEIVED = EM_MESSAGE_STREAM_RECEIVED, READ = EM_MESSAGE_READ, GROUP_READ = EM_MESSAGE_GROUP_READ, GROUP_READ_ACK_UPDATED = EM_MESSAGE_GROUP_READ_ACK_UPDATED, DELIVERED = EM_MESSAGE_DELIVERED, RECALLED = EM_MESSAGE_RECALLED, CONTENT_CHANGED = EM_MESSAGE_CONTENT_CHANGED, REACTION_CHANGED = EM_MESSAGE_REACTION_CHANGED, PIN_CHANGED = EM_MESSAGE_PIN_CHANGED)
fun initSDK(config: UTSJSONObject): UTSPromise<Unit> {
    initEMClient(config["appKey"] as String)
    return UTSPromise.resolve()
}
fun addConnectionListener(onConnected: (() -> Unit)?, onDisconnected: ((errorCode: Number) -> Unit)?, onLogout: ((errorCode: Number) -> Unit)?, onTokenWillExpire: (() -> Unit)?, onTokenExpired: (() -> Unit)?, onOfflineMessageSyncStart: (() -> Unit)?, onOfflineMessageSyncFinish: (() -> Unit)?): () -> Unit {
    val id = Date.now().toString(10) + Math.random().toString(36).substring(2, 11)
    val cbs = ConnectionListenerCallbacks(onConnected = onConnected, onDisconnected = onDisconnected, onLogout = onLogout, onTokenWillExpire = onTokenWillExpire, onTokenExpired = onTokenExpired, onOfflineMessageSyncStart = onOfflineMessageSyncStart, onOfflineMessageSyncFinish = onOfflineMessageSyncFinish)
    addConnectionListenerImpl(id, cbs)
    return fun(){
        return removeConnectionListenerImpl(id)
    }
}
fun removeConnectionListener(listenerId: String): Unit {
    removeConnectionListenerImpl(listenerId)
}
fun addMessageListener(onMessageReceived: ((messages: UTSArray<Message>) -> Unit)?, onStreamMessageReceived: ((messages: UTSArray<Message>) -> Unit)?, onCmdMessageReceived: ((messages: UTSArray<Message>) -> Unit)?, onMessageRead: ((messages: UTSArray<Message>) -> Unit)?, onGroupMessageRead: ((groupReadAcks: UTSArray<GroupReadAck>) -> Unit)?, onReadAckForGroupMessageUpdated: (() -> Unit)?, onMessageDelivered: ((messages: UTSArray<Message>) -> Unit)?, onMessageRecalled: ((messages: UTSArray<Message>) -> Unit)?, onMessageRecalledWithExt: ((recallMessageInfo: UTSArray<RecallMessageInfo>) -> Unit)?, onMessageChanged: ((message: Message, change: Any) -> Unit)?, onReactionChanged: ((messageReactionChangeList: UTSArray<MessageReactionChange>) -> Unit)?, onMessageContentChanged: ((messageModified: Message, operatorId: String, operationTime: Long) -> Unit)?, onMessagePinChanged: ((messageId: String, conversationId: String, pinOperation: Number, pinInfo: MessagePinInfo) -> Unit)?): () -> Unit {
    val id = Date.now().toString(10) + Math.random().toString(36).substring(2, 11)
    val callbacks = MessageListenerCallbacks(onMessageReceived = onMessageReceived, onStreamMessageReceived = onStreamMessageReceived, onCmdMessageReceived = onCmdMessageReceived, onMessageRead = onMessageRead, onGroupMessageRead = onGroupMessageRead, onReadAckForGroupMessageUpdated = onReadAckForGroupMessageUpdated, onMessageDelivered = onMessageDelivered, onMessageRecalled = onMessageRecalled, onMessageRecalledWithExt = onMessageRecalledWithExt, onMessageChanged = onMessageChanged, onReactionChanged = onReactionChanged, onMessageContentChanged = onMessageContentChanged, onMessagePinChanged = onMessagePinChanged)
    addMessageListenerImpl(id, callbacks)
    return fun(){
        return removeMessageListenerImpl(id)
    }
}
fun loginSDK(userId: String, password: String, onSuccess: (() -> Unit)?, onError: ((code: Number, message: String) -> Unit)?): Unit {
    val callback: UTSJSONObject = _uO("onSuccess" to onSuccess, "onError" to onError)
    loginEMClient(userId, password, callback)
}
fun loginSDKWithToken(username: String, token: String, onSuccess: (() -> Unit)?, onError: ((code: Number, message: String) -> Unit)?): Unit {
    val callback: UTSJSONObject = _uO("onSuccess" to onSuccess, "onError" to onError)
    loginWithToken(username, token, callback)
}
fun loginSDKWithAgoraToken(username: String, agoraToken: String, callback: UTSJSONObject): Unit {
    loginWithAgoraToken(username, agoraToken, callback)
}
fun logoutSDKSync(unbindToken: Boolean): Number {
    return logoutSync(unbindToken)
}
fun logoutSDK(unbindToken: Boolean, onSuccess: (() -> Unit)? = null, onError: ((code: Number, message: String) -> Unit)? = null): Unit {
    val callback: UTSJSONObject = _uO("onSuccess" to onSuccess, "onError" to onError)
    logoutEMClient(unbindToken, callback)
}
fun getCurrentUser(): String {
    return queryCurrentUser()
}
fun isLoggedInBefore(): Boolean {
    return queryIsLoggedInBefore()
}
fun isConnected(): Boolean {
    return queryIsConnected()
}
fun isLoggedIn(): Boolean {
    return queryIsLoggedIn()
}
fun sendTextMessage(content: String, to: String, chatType: String, onSuccess: ((messageInfo: UTSJSONObject) -> Unit)?, onError: ((code: Number, message: String) -> Unit)?, onProgress: ((progress: Number, status: String) -> Unit)? = null, ext: UTSJSONObject? = null): Unit {
    val callback = UTSJSONObject()
    callback["onSuccess"] = onSuccess
    callback["onError"] = onError
    if (onProgress != null) {
        callback["onProgress"] = onProgress
    }
    sendTextMessageImpl(content, to, chatType, callback, ext)
}
fun sendImageMessage(filePath: String, sendOriginalImage: Boolean, to: String, chatType: String, onSuccess: ((messageInfo: UTSJSONObject) -> Unit)?, onError: ((code: Number, message: String) -> Unit)?, onProgress: ((progress: Number, status: String) -> Unit)? = null, ext: UTSJSONObject? = null): Unit {
    val callback = UTSJSONObject()
    callback["onSuccess"] = onSuccess
    callback["onError"] = onError
    if (onProgress != null) {
        callback["onProgress"] = onProgress
    }
    sendImageMessageImpl(filePath, sendOriginalImage, to, chatType, callback, ext)
}
fun sendImageMessageWithUri(imageUri: Uri, sendOriginalImage: Boolean, to: String, callback: UTSJSONObject): Unit {
    sendImageMessageWithUriImpl(imageUri, sendOriginalImage, to, callback)
}
fun sendGifImageMessage(gifFilePath: String, to: String, callback: UTSJSONObject): Unit {
    sendGifImageMessageImpl(gifFilePath, to, callback)
}
fun sendVoiceMessage(filePath: String, timeLength: Int, to: String, callback: UTSJSONObject): Unit {
    sendVoiceMessageImpl(filePath, timeLength, to, callback)
}
fun sendVoiceMessageWithUri(fileUri: Uri, timeLength: Int, to: String, callback: UTSJSONObject): Unit {
    sendVoiceMessageWithUriImpl(fileUri, timeLength, to, callback)
}
fun sendVideoMessage(videoFilePath: String, imageThumbPath: String, timeLength: Int, to: String, chatType: String, onSuccess: ((messageInfo: UTSJSONObject) -> Unit)?, onError: ((code: Number, message: String) -> Unit)?, onProgress: ((progress: Number, status: String) -> Unit)? = null): Unit {
    val callback = UTSJSONObject()
    callback["onSuccess"] = onSuccess
    callback["onError"] = onError
    if (onProgress != null) {
        callback["onProgress"] = onProgress
    }
    sendVideoMessageImpl(videoFilePath, imageThumbPath, timeLength, to, chatType, callback)
}
fun sendVideoMessageWithUri(videoUri: Uri, imageThumbPath: String, timeLength: Int, to: String, chatType: String, callback: UTSJSONObject): Unit {
    sendVideoMessageWithUriImpl(videoUri, imageThumbPath, timeLength, to, chatType, callback)
}
fun sendVideoMessageWithUris(videoUri: Uri, thumbUri: Uri, timeLength: Int, to: String, chatType: String, callback: UTSJSONObject): Unit {
    sendVideoMessageWithUrisImpl(videoUri, thumbUri, timeLength, to, chatType, callback)
}
fun sendLocationMessage(latitude: Double, longitude: Double, locationAddress: String, buildingName: String, to: String, callback: UTSJSONObject): Unit {
    sendLocationMessageImpl(latitude, longitude, locationAddress, buildingName, to, callback)
}
fun sendLocationMessageSimple(latitude: Double, longitude: Double, locationAddress: String, to: String, callback: UTSJSONObject): Unit {
    sendLocationMessageSimpleImpl(latitude, longitude, locationAddress, to, callback)
}
fun sendFileMessage(filePath: String, to: String, chatType: String, onSuccess: ((messageInfo: UTSJSONObject) -> Unit)?, onError: ((code: Number, message: String) -> Unit)?, onProgress: ((progress: Number, status: String) -> Unit)? = null): Unit {
    val callback = UTSJSONObject()
    callback["onSuccess"] = onSuccess
    callback["onError"] = onError
    if (onProgress != null) {
        callback["onProgress"] = onProgress
    }
    sendFileMessageImpl(filePath, to, chatType, callback)
}
fun sendFileMessageWithUri(fileUri: Uri, to: String, chatType: String, callback: UTSJSONObject): Unit {
    sendFileMessageWithUriImpl(fileUri, to, chatType, callback)
}
fun sendCmdMessage(action: String, to: String, onSuccess: ((messageInfo: UTSJSONObject) -> Unit)?, onError: ((code: Number, message: String) -> Unit)?, onProgress: ((progress: Number, status: String) -> Unit)? = null, ext: UTSJSONObject? = null): Unit {
    val callback = UTSJSONObject()
    callback["onSuccess"] = onSuccess
    callback["onError"] = onError
    if (onProgress != null) {
        callback["onProgress"] = onProgress
    }
    sendCmdMessageImpl(action, to, callback, ext)
}
fun sendCustomMessage(event: String, params: UTSJSONObject, to: String, chatType: String, onSuccess: ((messageInfo: UTSJSONObject) -> Unit)?, onError: ((code: Number, message: String) -> Unit)?, onProgress: ((progress: Number, status: String) -> Unit)? = null, ext: UTSJSONObject? = null): Unit {
    val callback = UTSJSONObject()
    callback["onSuccess"] = onSuccess
    callback["onError"] = onError
    if (onProgress != null) {
        callback["onProgress"] = onProgress
    }
    sendCustomMessageImpl(event, params, to, chatType, callback, ext)
}
fun sendCombinedMessage(title: String, summary: String, compatibleText: String, messageIdList: UTSArray<String>, to: String, callback: UTSJSONObject): Unit {
    sendCombinedMessageImpl(title, summary, compatibleText, messageIdList, to, callback)
}
fun downloadAttachment(message: Message, callback: UTSJSONObject): Unit {
    downloadAttachmentImpl(message, callback)
}
fun downloadThumbnail(message: Message, callback: UTSJSONObject): Unit {
    downloadThumbnailImpl(message, callback)
}
fun openFilePicker(onSuccess: ((result: FilePickResultData) -> Unit)?, onError: ((code: Number, message: String) -> Unit)?, onCancel: (() -> Unit)?): Unit {
    val callback = UTSJSONObject()
    callback["onSuccess"] = onSuccess
    callback["onError"] = onError
    callback["onCancel"] = onCancel
    openFilePickerImpl(callback)
}
var eventBusEnabled = false
var connectionListenerId: String? = null
var messageListenerId: String? = null
fun enableEventBus(): Unit {
    if (eventBusEnabled) {
        console.log("[Android] EventBus already enabled")
        return
    }
    val connectionCallbacks = ConnectionListenerCallbacks(onConnected = fun(){
        setTimeout(fun(){
            uni__emit(EMConnectionEvent.CONNECTED, null)
        }
        , 0)
    }
    , onDisconnected = fun(errorCode: Number){
        setTimeout(fun(){
            uni__emit(EMConnectionEvent.DISCONNECTED, errorCode)
        }
        , 0)
    }
    , onLogout = fun(errorCode: Number){
        setTimeout(fun(){
            uni__emit(EMConnectionEvent.LOGOUT, errorCode)
        }
        , 0)
    }
    , onTokenWillExpire = fun(){
        setTimeout(fun(){
            uni__emit(EMConnectionEvent.TOKEN_WILL_EXPIRE, null)
        }
        , 0)
    }
    , onTokenExpired = fun(){
        setTimeout(fun(){
            uni__emit(EMConnectionEvent.TOKEN_EXPIRED, null)
        }
        , 0)
    }
    , onOfflineMessageSyncStart = fun(){
        setTimeout(fun(){
            uni__emit(EMConnectionEvent.OFFLINE_SYNC_START, null)
        }
        , 0)
    }
    , onOfflineMessageSyncFinish = fun(){
        setTimeout(fun(){
            uni__emit(EMConnectionEvent.OFFLINE_SYNC_FINISH, null)
        }
        , 0)
    }
    )
    val connId = Date.now().toString(10) + Math.random().toString(36).substring(2, 11)
    connectionListenerId = connId
    addConnectionListenerImpl(connId, connectionCallbacks)
    val messageCallbacks = MessageListenerCallbacks(onMessageReceived = fun(messages: UTSArray<Message>){
        val json = JSON.stringify(messages)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.RECEIVED, json)
        }
        , 0)
    }
    , onCmdMessageReceived = fun(messages: UTSArray<Message>){
        val json = JSON.stringify(messages)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.CMD_RECEIVED, json)
        }
        , 0)
    }
    , onStreamMessageReceived = fun(messages: UTSArray<Message>){
        val json = JSON.stringify(messages)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.STREAM_RECEIVED, json)
        }
        , 0)
    }
    , onMessageRead = fun(messages: UTSArray<Message>){
        val json = JSON.stringify(messages)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.READ, json)
        }
        , 0)
    }
    , onGroupMessageRead = fun(groupReadAcks: UTSArray<GroupReadAck>){
        val json = JSON.stringify(groupReadAcks)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.GROUP_READ, json)
        }
        , 0)
    }
    , onReadAckForGroupMessageUpdated = fun(){
        setTimeout(fun(){
            uni__emit(EMMessageEvent.GROUP_READ_ACK_UPDATED, null)
        }
        , 0)
    }
    , onMessageDelivered = fun(messages: UTSArray<Message>){
        val json = JSON.stringify(messages)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.DELIVERED, json)
        }
        , 0)
    }
    , onMessageRecalled = fun(messages: UTSArray<Message>){
        val json = JSON.stringify(messages)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.RECALLED, json)
        }
        , 0)
    }
    , onMessageRecalledWithExt = fun(recallMessageInfo: UTSArray<RecallMessageInfo>){
        val json = JSON.stringify(recallMessageInfo)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.RECALLED, json)
        }
        , 0)
    }
    , onMessageChanged = fun(message: Message, change: Any){
        val json = JSON.stringify(message)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.CONTENT_CHANGED, json)
        }
        , 0)
    }
    , onReactionChanged = fun(messageReactionChangeList: UTSArray<MessageReactionChange>){
        val json = JSON.stringify(messageReactionChangeList)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.REACTION_CHANGED, json)
        }
        , 0)
    }
    , onMessageContentChanged = fun(messageModified: Message, operatorId: String, operationTime: Long){
        val json = JSON.stringify(messageModified)
        setTimeout(fun(){
            uni__emit(EMMessageEvent.CONTENT_CHANGED, json)
        }
        , 0)
    }
    , onMessagePinChanged = fun(messageId: String, conversationId: String, pinOperation: Number, pinInfo: MessagePinInfo){
        setTimeout(fun(){
            uni__emit(EMMessageEvent.PIN_CHANGED, messageId)
        }
        , 0)
    }
    )
    val msgId = Date.now().toString(10) + Math.random().toString(36).substring(2, 11) + "_msg"
    messageListenerId = msgId
    addMessageListenerImpl(msgId, messageCallbacks)
    eventBusEnabled = true
    console.log("[Android] EventBus enabled")
}
fun disableEventBus(): Unit {
    if (!eventBusEnabled) {
        return
    }
    val connIdToRemove = connectionListenerId
    if (connIdToRemove != null) {
        removeConnectionListenerImpl(connIdToRemove)
        connectionListenerId = null
    }
    val msgIdToRemove = messageListenerId
    if (msgIdToRemove != null) {
        removeMessageListenerImpl(msgIdToRemove)
        messageListenerId = null
    }
    eventBusEnabled = false
    console.log("[Android] EventBus disabled")
}
