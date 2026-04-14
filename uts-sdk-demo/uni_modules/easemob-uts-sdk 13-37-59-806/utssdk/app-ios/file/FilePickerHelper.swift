import Foundation
import UIKit
import UniformTypeIdentifiers

// MARK: - 线程安全的结果存储（复用 EMMessageBridge 的设计模式）

private let pickerResultLock = NSLock()
/// key: callbackId, value: { "status": "pending"/"success"/"error"/"cancelled", ... }
private var pickerResultMap: [String: [String: Any]] = [:]

private func setPickerResult(_ dict: [String: Any], for callbackId: String) {
    pickerResultLock.lock()
    pickerResultMap[callbackId] = dict
    pickerResultLock.unlock()
}

private func getPickerResult(for callbackId: String) -> [String: Any]? {
    pickerResultLock.lock()
    defer { pickerResultLock.unlock() }
    return pickerResultMap[callbackId]
}

private func removePickerResult(for callbackId: String) {
    pickerResultLock.lock()
    pickerResultMap.removeValue(forKey: callbackId)
    pickerResultLock.unlock()
}

// MARK: - 文件选择器桥接类（轮询模式，和 EMMessageBridge 一致）

@objc
@objcMembers
public class FilePickerBridge: NSObject {

    private static let shared = FilePickerBridge()
    private weak var presentingViewController: UIViewController?

    // MARK: - 打开文件选择器

    /// 打开文件选择器
    /// - Parameter callbackId: UTS 侧生成的回调 ID
    @objc public static func openFilePicker(callbackId: String) {
        NSLog("[FilePickerBridge] openFilePicker, callbackId: %@", callbackId)

        // 初始化为 pending
        setPickerResult(["status": "pending"], for: callbackId)

        // 保存当前 callbackId
        shared.currentCallbackId = callbackId

        DispatchQueue.main.async {
            // 获取当前显示的 ViewController
            guard let rootVC = UIApplication.shared.keyWindow?.rootViewController else {
                setPickerResult([
                    "status": "error",
                    "code": -1,
                    "message": "无法获取根视图控制器"
                ], for: callbackId)
                return
            }

            // 找到最顶层的 presentedViewController
            var topVC = rootVC
            while let presented = topVC.presentedViewController {
                topVC = presented
            }

            shared.presentingViewController = topVC

            // 创建文档选择器 - 支持所有文件类型
            let documentPicker: UIDocumentPickerViewController
            if #available(iOS 14.0, *) {
                documentPicker = UIDocumentPickerViewController(forOpeningContentTypes: [.item], asCopy: true)
            } else {
                documentPicker = UIDocumentPickerViewController(documentTypes: ["public.item"], in: .open)
            }
            documentPicker.delegate = shared
            documentPicker.allowsMultipleSelection = false

            topVC.present(documentPicker, animated: true)
        }
    }

    // MARK: - UTS 侧轮询接口

    /// 查询文件选择状态："pending" / "success" / "error" / "cancelled"
    @objc public static func getResultStatus(callbackId: String) -> String {
        guard let result = getPickerResult(for: callbackId) else { return "pending" }
        return result["status"] as? String ?? "pending"
    }

    /// 获取选中文件路径
    @objc public static func getResultFilePath(callbackId: String) -> String {
        return getPickerResult(for: callbackId)?["filePath"] as? String ?? ""
    }

    /// 获取选中文件名
    @objc public static func getResultFileName(callbackId: String) -> String {
        return getPickerResult(for: callbackId)?["fileName"] as? String ?? ""
    }

    /// 获取选中文件大小
    @objc public static func getResultFileSize(callbackId: String) -> Int {
        return getPickerResult(for: callbackId)?["fileSize"] as? Int ?? 0
    }

    /// 获取选中文件 MIME 类型
    @objc public static func getResultMimeType(callbackId: String) -> String {
        return getPickerResult(for: callbackId)?["mimeType"] as? String ?? ""
    }

    /// 获取错误码
    @objc public static func getResultErrorCode(callbackId: String) -> Int {
        return getPickerResult(for: callbackId)?["code"] as? Int ?? 0
    }

    /// 获取错误描述
    @objc public static func getResultErrorMessage(callbackId: String) -> String {
        return getPickerResult(for: callbackId)?["message"] as? String ?? ""
    }

    /// 清除结果（轮询完成后调用）
    @objc public static func clearResult(callbackId: String) {
        removePickerResult(for: callbackId)
    }

    // MARK: - 内部状态

    private var currentCallbackId: String = ""

    // MARK: - 获取文件信息

    /// 根据文件扩展名获取 MIME 类型
    private func getMimeType(for url: URL) -> String {
        let pathExtension = url.pathExtension.lowercased()

        let mimeTypes: [String: String] = [
            "pdf": "application/pdf",
            "doc": "application/msword",
            "docx": "application/vnd.openxmlformats-officedocument.wordprocessingml.document",
            "xls": "application/vnd.ms-excel",
            "xlsx": "application/vnd.openxmlformats-officedocument.spreadsheetml.sheet",
            "ppt": "application/vnd.ms-powerpoint",
            "pptx": "application/vnd.openxmlformats-officedocument.presentationml.presentation",
            "txt": "text/plain",
            "rtf": "application/rtf",
            "jpg": "image/jpeg",
            "jpeg": "image/jpeg",
            "png": "image/png",
            "gif": "image/gif",
            "bmp": "image/bmp",
            "webp": "image/webp",
            "mp3": "audio/mpeg",
            "mp4": "video/mp4",
            "mov": "video/quicktime",
            "wav": "audio/wav",
            "m4a": "audio/mp4",
            "zip": "application/zip",
            "rar": "application/x-rar-compressed",
            "7z": "application/x-7z-compressed",
            "json": "application/json",
            "xml": "application/xml",
            "html": "text/html",
            "csv": "text/csv"
        ]

        if let mimeType = mimeTypes[pathExtension] {
            return mimeType
        }

        if #available(iOS 14.0, *) {
            if let utType = UTType(filenameExtension: pathExtension),
               let mimeType = utType.preferredMIMEType {
                return mimeType
            }
        }

        return "application/octet-stream"
    }
}

// MARK: - UIDocumentPickerDelegate

extension FilePickerBridge: UIDocumentPickerDelegate {

    public func documentPicker(_ controller: UIDocumentPickerViewController, didPickDocumentsAt urls: [URL]) {
        let cid = currentCallbackId

        guard let url = urls.first else {
            setPickerResult([
                "status": "error",
                "code": -5,
                "message": "未获取到文件URL"
            ], for: cid)
            return
        }

        // 安全访问文件
        let accessing = url.startAccessingSecurityScopedResource()

        // 获取文件信息
        let fileName = url.lastPathComponent
        let filePath = url.path  // 使用本地路径
        let mimeType = getMimeType(for: url)

        var fileSize: Int = 0
        do {
            let attributes = try FileManager.default.attributesOfItem(atPath: url.path)
            fileSize = attributes[.size] as? Int ?? 0
        } catch {
            NSLog("[FilePickerBridge] 获取文件大小失败: \(error)")
        }

        if accessing {
            url.stopAccessingSecurityScopedResource()
        }

        NSLog("[FilePickerBridge] 文件选择成功: %@, size: %d", fileName, fileSize)

        setPickerResult([
            "status": "success",
            "filePath": filePath,
            "fileName": fileName,
            "fileSize": fileSize,
            "mimeType": mimeType
        ], for: cid)
    }

    public func documentPickerWasCancelled(_ controller: UIDocumentPickerViewController) {
        let cid = currentCallbackId
        NSLog("[FilePickerBridge] 用户取消文件选择, callbackId: %@", cid)
        setPickerResult(["status": "cancelled"], for: cid)
    }
}
