package uts.sdk.modules.easemobUtsSdkBeta

import android.app.Activity
import android.content.Context
import android.content.Intent
import android.database.Cursor
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import io.dcloud.uts.UTSAndroid

/**
 * 文件选择结果
 */
data class FilePickResult(
    val filePath: String,
    val fileName: String,
    val fileSize: Long,
    val mimeType: String
)

/**
 * 文件选择器回调接口
 */
interface FilePickerCallback {
    fun onSuccess(result: FilePickResult)
    fun onError(code: Int, message: String)
    fun onCancel()
}

/**
 * 文件选择器工具类
 */
object FilePickerHelper {

    private const val TAG = "FilePickerHelper"
    private const val REQUEST_CODE_PICK_FILE = 10001

    private var currentCallback: FilePickerCallback? = null

    /**
     * 打开文件选择器
     * @param callback 选择结果回调
     */
    @JvmStatic
    fun openFilePicker(callback: FilePickerCallback) {
        val context = UTSAndroid.getAppContext()
        if (context == null) {
            callback.onError(-1, "无法获取应用上下文")
            return
        }

        currentCallback = callback

        try {
            val intent = Intent(Intent.ACTION_OPEN_DOCUMENT).apply {
                addCategory(Intent.CATEGORY_OPENABLE)
                type = "*/*"
                putExtra(Intent.EXTRA_LOCAL_ONLY, true)
            }

            val activity = UTSAndroid.getUniActivity()
            if (activity != null) {
                activity.startActivityForResult(intent, REQUEST_CODE_PICK_FILE)
            } else {
                callback.onError(-2, "无法获取当前Activity")
                currentCallback = null
            }
        } catch (e: Exception) {
            Log.e(TAG, "打开文件选择器失败", e)
            callback.onError(-3, "打开文件选择器失败: ${e.message}")
            currentCallback = null
        }
    }

    /**
     * 处理 Activity 结果
     */
    @JvmStatic
    fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?): Boolean {
        if (requestCode != REQUEST_CODE_PICK_FILE) {
            return false
        }

        val callback = currentCallback
        currentCallback = null

        if (callback == null) {
            return true
        }

        if (resultCode == Activity.RESULT_OK && data != null) {
            val uri = data.data
            if (uri != null) {
                try {
                    val context = UTSAndroid.getAppContext()!!
                    val result = getFilePickResult(context, uri)
                    callback.onSuccess(result)
                } catch (e: Exception) {
                    Log.e(TAG, "获取文件信息失败", e)
                    callback.onError(-4, "获取文件信息失败: ${e.message}")
                }
            } else {
                callback.onError(-5, "未获取到文件URI")
            }
        } else if (resultCode == Activity.RESULT_CANCELED) {
            callback.onCancel()
        } else {
            callback.onError(-6, "文件选择失败")
        }

        return true
    }

    /**
     * 从 Uri 获取文件选择结果
     */
    @JvmStatic
    fun getFilePickResult(context: Context, uri: Uri): FilePickResult {
        val fileName = getFileName(context, uri)
        val fileSize = getFileSize(context, uri)
        val mimeType = context.contentResolver.getType(uri) ?: "application/octet-stream"
        val filePath = uri.toString()

        return FilePickResult(
            filePath = filePath,
            fileName = fileName,
            fileSize = fileSize,
            mimeType = mimeType
        )
    }

    /**
     * 获取文件名
     */
    @JvmStatic
    fun getFileName(context: Context, uri: Uri): String {
        var fileName = "unknown"

        if (uri.scheme == "content") {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val nameIndex = cursor.getColumnIndex(OpenableColumns.DISPLAY_NAME)
                    if (nameIndex >= 0) {
                        fileName = cursor.getString(nameIndex)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取文件名失败", e)
            } finally {
                cursor?.close()
            }
        } else if (uri.scheme == "file") {
            fileName = uri.lastPathSegment ?: "unknown"
        }

        return fileName
    }

    /**
     * 获取文件大小
     */
    @JvmStatic
    fun getFileSize(context: Context, uri: Uri): Long {
        var fileSize: Long = 0

        if (uri.scheme == "content") {
            var cursor: Cursor? = null
            try {
                cursor = context.contentResolver.query(uri, null, null, null, null)
                if (cursor != null && cursor.moveToFirst()) {
                    val sizeIndex = cursor.getColumnIndex(OpenableColumns.SIZE)
                    if (sizeIndex >= 0 && !cursor.isNull(sizeIndex)) {
                        fileSize = cursor.getLong(sizeIndex)
                    }
                }
            } catch (e: Exception) {
                Log.e(TAG, "获取文件大小失败", e)
            } finally {
                cursor?.close()
            }
        }

        return fileSize
    }
}
