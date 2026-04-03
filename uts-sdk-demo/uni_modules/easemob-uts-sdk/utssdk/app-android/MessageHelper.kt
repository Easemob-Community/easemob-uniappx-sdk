package uts.sdk.modules.easemobUtsSdk

import com.hyphenate.chat.EMMessage
import com.hyphenate.EMCallBack
import android.util.Log

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
