@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "SENSELESS_COMPARISON", "NAME_SHADOWING", "UNNECESSARY_NOT_NULL_ASSERTION")
package uni.UNI1F192F2
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
import io.dcloud.uniapp.extapi.exit as uni_exit
import uts.sdk.modules.easemobUtsSdk.initSDK
import uts.sdk.modules.easemobUtsSdk.addConnectionListener
import uts.sdk.modules.easemobUtsSdk.addMessageListener
import uts.sdk.modules.easemobUtsSdk.Message
import io.dcloud.uniapp.extapi.showToast as uni_showToast
val runBlock1 = run {
    __uniConfig.getAppStyles = fun(): Map<String, Map<String, Map<String, Any>>> {
        return GenApp.styles
    }
}
open class GenApp : BaseApp {
    constructor(__ins: ComponentInternalInstance) : super(__ins) {
        setCurrentInstance(__ins)
        __ins.proxy = this
        GenApp.setup(this)
    }
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenApp) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenApp
            val _cache = __ins.renderCache
            var firstBackTime: Number = 0
            onLaunch(fun(_options){
                console.log("App Launch")
                initSDK(_uO("appKey" to "easemob-demo#support"))
                val unsubscribe = addConnectionListener(_uO("onConnected" to fun(){
                    console.log("[EMConnection] 已连接到服务器")
                }
                , "onDisconnected" to fun(errorCode: Number){
                    console.log("[EMConnection] 连接断开, errorCode:", errorCode)
                }
                , "onLogout" to fun(errorCode: Number){
                    console.log("[EMConnection] 被登出, errorCode:", errorCode)
                }
                , "onTokenWillExpire" to fun(){
                    console.log("[EMConnection] Token 即将过期")
                }
                , "onTokenExpired" to fun(){
                    console.log("[EMConnection] Token 已过期")
                }
                , "onOfflineMessageSyncStart" to fun(){
                    console.log("[EMConnection] 开始同步离线消息")
                }
                , "onOfflineMessageSyncFinish" to fun(){
                    console.log("[EMConnection] 离线消息同步完成")
                }
                ))
                val unsubscribeMessage = addMessageListener(_uO("onMessageReceived" to fun(messages: UTSArray<Message>){
                    console.log("[EMMessage] 收到消息, 数量:", messages.length)
                    messages.forEach(fun(msg: Message){
                        console.log("[EMMessage] 来自: " + msg.from + ", 类型: " + msg.body.type)
                        if (msg.body.type === "txt") {
                            console.log("[EMMessage] 文本内容:", msg.body.message ?: "")
                        }
                        val ext = msg.ext
                        val extJson = JSON.stringify(ext)
                        if (extJson != "{}" && extJson.length > 2) {
                            console.log("[EMMessage] 扩展字段:", extJson)
                        } else {
                            console.log("[EMMessage] 无扩展字段")
                        }
                    }
                    )
                }
                , "onCmdMessageReceived" to fun(messages: UTSArray<Message>){
                    console.log("[EMMessage] 收到命令消息, 数量:", messages.length)
                    messages.forEach(fun(msg: Message){
                        console.log("[EMMessage] CMD来自: " + msg.from + ", action: " + (msg.body.action ?: ""))
                        val ext = msg.ext
                        val extJson = JSON.stringify(ext)
                        if (extJson != "{}" && extJson.length > 2) {
                            console.log("[EMMessage] CMD扩展字段:", extJson)
                        }
                    }
                    )
                }
                , "onMessageRead" to fun(messages: UTSArray<Message>){
                    console.log("[EMMessage] 消息已读, 数量:", messages.length)
                }
                , "onMessageDelivered" to fun(messages: UTSArray<Message>){
                    console.log("[EMMessage] 消息已送达, 数量:", messages.length)
                }
                , "onMessageRecalled" to fun(messages: UTSArray<Message>){
                    console.log("[EMMessage] 消息被撤回, 数量:", messages.length)
                }
                , "onMessageChanged" to fun(message: Message, change: Any){
                    console.log("[EMMessage] 消息变更, msgId:", message.msgId)
                }
                ))
            }
            )
            onAppShow(fun(_options){
                console.log("App Show")
            }
            )
            onAppHide(fun(){
                console.log("App Hide")
            }
            )
            onLastPageBackPress(fun(){
                console.log("App LastPageBackPress")
                if (firstBackTime == 0) {
                    uni_showToast(ShowToastOptions(title = "再按一次退出应用", position = "bottom"))
                    firstBackTime = Date.now()
                    setTimeout(fun(){
                        firstBackTime = 0
                    }, 2000)
                } else if (Date.now() - firstBackTime < 2000) {
                    firstBackTime = Date.now()
                    uni_exit(null)
                }
            }
            )
            onExit(fun(){
                console.log("App Exit")
            }
            )
            return fun(): Any? {
                return null
            }
        }
        val styles: Map<String, Map<String, Map<String, Any>>> by lazy {
            _nCS(_uA(
                styles0
            ))
        }
        val styles0: Map<String, Map<String, Map<String, Any>>>
            get() {
                return _uM("uni-row" to _pS(_uM("flexDirection" to "row")), "uni-column" to _pS(_uM("flexDirection" to "column")))
            }
    }
}
val GenAppClass = CreateVueAppComponent(GenApp::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "app", name = "", inheritAttrs = true, inject = Map(), props = Map(), propsNeedCastKeys = _uA(), emits = Map(), components = Map(), styles = GenApp.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenApp.setup(props as GenApp)
    }
    )
}
, fun(instance): GenApp {
    return GenApp(instance)
}
)
val GenPagesIndexIndexClass = CreateVueComponent(GenPagesIndexIndex::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesIndexIndex.inheritAttrs, inject = GenPagesIndexIndex.inject, props = GenPagesIndexIndex.props, propsNeedCastKeys = GenPagesIndexIndex.propsNeedCastKeys, emits = GenPagesIndexIndex.emits, components = GenPagesIndexIndex.components, styles = GenPagesIndexIndex.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesIndexIndex.setup(props as GenPagesIndexIndex)
    }
    )
}
, fun(instance, renderer): GenPagesIndexIndex {
    return GenPagesIndexIndex(instance, renderer)
}
)
val GenPagesLoginLoginClass = CreateVueComponent(GenPagesLoginLogin::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesLoginLogin.inheritAttrs, inject = GenPagesLoginLogin.inject, props = GenPagesLoginLogin.props, propsNeedCastKeys = GenPagesLoginLogin.propsNeedCastKeys, emits = GenPagesLoginLogin.emits, components = GenPagesLoginLogin.components, styles = GenPagesLoginLogin.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesLoginLogin.setup(props as GenPagesLoginLogin)
    }
    )
}
, fun(instance, renderer): GenPagesLoginLogin {
    return GenPagesLoginLogin(instance, renderer)
}
)
val GenPagesMessageMessageClass = CreateVueComponent(GenPagesMessageMessage::class.java, fun(): VueComponentOptions {
    return VueComponentOptions(type = "page", name = "", inheritAttrs = GenPagesMessageMessage.inheritAttrs, inject = GenPagesMessageMessage.inject, props = GenPagesMessageMessage.props, propsNeedCastKeys = GenPagesMessageMessage.propsNeedCastKeys, emits = GenPagesMessageMessage.emits, components = GenPagesMessageMessage.components, styles = GenPagesMessageMessage.styles, setup = fun(props: ComponentPublicInstance): Any? {
        return GenPagesMessageMessage.setup(props as GenPagesMessageMessage)
    }
    )
}
, fun(instance, renderer): GenPagesMessageMessage {
    return GenPagesMessageMessage(instance, renderer)
}
)
fun createApp(): UTSJSONObject {
    val app = createSSRApp(GenAppClass)
    return _uO("app" to app)
}
fun main(app: IApp) {
    definePageRoutes()
    defineAppConfig()
    (createApp()["app"] as VueApp).mount(app, GenUniApp())
}
open class UniAppConfig : io.dcloud.uniapp.appframe.AppConfig {
    override var name: String = "uts-sdk-demo"
    override var appid: String = "__UNI__1F192F2"
    override var versionName: String = "1.0.0"
    override var versionCode: String = "100"
    override var uniCompilerVersion: String = "5.06"
    constructor() : super() {}
}
fun definePageRoutes() {
    __uniRoutes.push(UniPageRoute(path = "pages/index/index", component = GenPagesIndexIndexClass, meta = UniPageMeta(isQuit = true), style = _uM("navigationBarTitleText" to "uni-app x")))
    __uniRoutes.push(UniPageRoute(path = "pages/login/login", component = GenPagesLoginLoginClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "登录")))
    __uniRoutes.push(UniPageRoute(path = "pages/message/message", component = GenPagesMessageMessageClass, meta = UniPageMeta(isQuit = false), style = _uM("navigationBarTitleText" to "发送消息")))
}
val __uniLaunchPage: Map<String, Any?> = _uM("url" to "pages/index/index", "style" to _uM("navigationBarTitleText" to "uni-app x"))
fun defineAppConfig() {
    __uniConfig.entryPagePath = "/pages/index/index"
    __uniConfig.globalStyle = _uM("navigationBarTextStyle" to "black", "navigationBarTitleText" to "uni-app x", "navigationBarBackgroundColor" to "#F8F8F8", "backgroundColor" to "#F8F8F8")
    __uniConfig.getTabBarConfig = fun(): Map<String, Any>? {
        return null
    }
    __uniConfig.tabBar = __uniConfig.getTabBarConfig()
    __uniConfig.conditionUrl = ""
    __uniConfig.uniIdRouter = _uM()
    __uniConfig.ready = true
}
open class GenUniApp : UniAppImpl() {
    open val vm: GenApp?
        get() {
            return getAppVm() as GenApp?
        }
    open val `$vm`: GenApp?
        get() {
            return getAppVm() as GenApp?
        }
}
fun getApp(): GenUniApp {
    return getUniApp() as GenUniApp
}
