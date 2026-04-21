import Foundation
import HyphenateChat

@objc
@objcMembers
public class EMMessageHelper: NSObject {
    public static func createCustomMessageBody(event: String, paramsJson: String?) -> EMCustomMessageBody? {
        guard let json = paramsJson else {
            return EMCustomMessageBody(event: event, customExt: nil)
        }
        guard let data = json.data(using: .utf8),
              let dict = try? JSONSerialization.jsonObject(with: data, options: []) as? [String: String] else {
            return EMCustomMessageBody(event: event, customExt: nil)
        }
        return EMCustomMessageBody(event: event, customExt: dict)
    }

    public static func setMessageExt(_ message: EMChatMessage, extJson: String?) {
        guard let json = extJson else { return }
        guard let data = json.data(using: .utf8),
              let dict = try? JSONSerialization.jsonObject(with: data, options: []) as? [AnyHashable: Any] else {
            return
        }
        message.ext = dict
    }
}
