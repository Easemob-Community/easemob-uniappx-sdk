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
import uts.sdk.modules.easemobUtsSdk.addMessageListener
import uts.sdk.modules.easemobUtsSdk.Message
import uts.sdk.modules.easemobUtsSdk.loginSDK
import uts.sdk.modules.easemobUtsSdk.logoutSDK
import uts.sdk.modules.easemobUtsSdk.getCurrentUser
import uts.sdk.modules.easemobUtsSdk.isLoggedIn as checkIsLoggedIn
import uts.sdk.modules.easemobUtsSdk.isConnected as checkIsConnected
import io.dcloud.uniapp.extapi.navigateTo as uni_navigateTo
open class GenPagesLoginLogin : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesLoginLogin) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesLoginLogin
            val _cache = __ins.renderCache
            val username = ref("hfp")
            val password = ref("1")
            val isLoggingIn = ref(false)
            val isLoggedIn = ref(false)
            val isConnected = ref(false)
            val currentUser = ref("")
            val logContent = ref("")
            val messageList = ref(_uA<Message>())
            var unsubscribeMessage: (() -> Unit)? = null
            val connectionStatus = computed(fun(): String {
                if (isLoggedIn.value) {
                    return "已登录"
                } else if (isConnected.value) {
                    return "已连接"
                } else {
                    return "未连接"
                }
            }
            )
            fun gen_addLog_fn(message: String): Unit {
                val time = Date().toLocaleTimeString()
                logContent.value = "[" + time + "] " + message + "\n" + logContent.value
            }
            val addLog = ::gen_addLog_fn
            fun gen_updateStatus_fn(): Unit {
                currentUser.value = getCurrentUser()
                isConnected.value = checkIsConnected()
                isLoggedIn.value = checkIsLoggedIn()
            }
            val updateStatus = ::gen_updateStatus_fn
            fun gen_handleLogin_fn(): Unit {
                if (username.value.length == 0 || password.value.length == 0) {
                    addLog("错误：用户名和密码不能为空")
                    return
                }
                isLoggingIn.value = true
                addLog("开始登录：" + username.value)
                loginSDK(username.value, password.value, fun(): Unit {
                    addLog("登录成功")
                    isLoggingIn.value = false
                    isLoggedIn.value = true
                    currentUser.value = username.value
                    updateStatus()
                }
                , fun(code: Number, message: String): Unit {
                    addLog("登录失败：code=" + code + ", message=" + message)
                    isLoggingIn.value = false
                    isLoggedIn.value = false
                    updateStatus()
                }
                )
            }
            val handleLogin = ::gen_handleLogin_fn
            fun gen_goToMessage_fn(): Unit {
                uni_navigateTo(NavigateToOptions(url = "/pages/message/message"))
            }
            val goToMessage = ::gen_goToMessage_fn
            fun gen_handleLogout_fn(): Unit {
                addLog("开始登出...")
                logoutSDK(true, fun(): Unit {
                    addLog("登出成功")
                    isLoggedIn.value = false
                    isConnected.value = false
                    currentUser.value = ""
                    updateStatus()
                }
                , fun(code: Number, message: String): Unit {
                    addLog("登出失败：code=" + code + ", message=" + message)
                    updateStatus()
                }
                )
            }
            val handleLogout = ::gen_handleLogout_fn
            onLoad(fun(_options){
                addLog("登录页面加载")
                updateStatus()
                unsubscribeMessage = addMessageListener(fun(messages: UTSArray<Message>): Unit {
                    messages.forEach(fun(msg: Message): Unit {
                        addLog("收到消息: 来自=" + msg.from + ", 类型=" + msg.body.type)
                        if (msg.body.type === "txt") {
                            addLog("内容: " + (msg.body.message ?: ""))
                        }
                        messageList.value.unshift(msg)
                        if (messageList.value.length > 50) {
                            messageList.value.pop()
                        }
                    }
                    )
                }
                , null, null, null, null, null, null, null, null, null, null, null, null)
            }
            )
            onUnload(fun(){
                unsubscribeMessage?.invoke()
                unsubscribeMessage = null
            }
            )
            return fun(): Any? {
                return _cE("view", _uM("class" to "container"), _uA(
                    _cE("view", _uM("class" to "login-box"), _uA(
                        _cE("text", _uM("class" to "title"), "环信 IM 登录"),
                        _cE("view", _uM("class" to "input-row"), _uA(
                            _cE("text", _uM("class" to "label"), "用户名"),
                            _cE("input", _uM("class" to "input", "type" to "text", "modelValue" to unref(username), "onInput" to fun(`$event`: UniInputEvent){
                                trySetRefValue(username, `$event`.detail.value)
                            }
                            , "placeholder" to "请输入用户名", "disabled" to unref(isLoggingIn)), null, 40, _uA(
                                "modelValue",
                                "disabled"
                            ))
                        )),
                        _cE("view", _uM("class" to "input-row"), _uA(
                            _cE("text", _uM("class" to "label"), "密码"),
                            _cE("input", _uM("class" to "input", "type" to "password", "modelValue" to unref(password), "onInput" to fun(`$event`: UniInputEvent){
                                trySetRefValue(password, `$event`.detail.value)
                            }
                            , "placeholder" to "请输入密码", "disabled" to unref(isLoggingIn)), null, 40, _uA(
                                "modelValue",
                                "disabled"
                            ))
                        )),
                        _cE("view", _uM("class" to "status-row"), _uA(
                            _cE("text", _uM("class" to "status-label"), "状态："),
                            _cE("text", _uM("class" to _nC(_uA(
                                "status-value",
                                if (unref(isConnected)) {
                                    "connected"
                                } else {
                                    "disconnected"
                                }
                            ))), _tD(unref(connectionStatus)), 3)
                        )),
                        if (unref(currentUser).length > 0) {
                            _cE("view", _uM("key" to 0, "class" to "status-row"), _uA(
                                _cE("text", _uM("class" to "status-label"), "当前用户："),
                                _cE("text", _uM("class" to "status-value"), _tD(unref(currentUser)), 1)
                            ))
                        } else {
                            _cC("v-if", true)
                        }
                        ,
                        _cE("view", _uM("class" to "button-row"), _uA(
                            _cE("button", _uM("class" to "btn btn-primary", "disabled" to (unref(isLoggingIn) || unref(username).length == 0 || unref(password).length == 0), "onClick" to handleLogin), _tD(if (unref(isLoggingIn)) {
                                "登录中..."
                            } else {
                                "登录"
                            }
                            ), 9, _uA(
                                "disabled"
                            ))
                        )),
                        _cE("view", _uM("class" to "button-row"), _uA(
                            _cE("button", _uM("class" to "btn btn-secondary", "disabled" to !unref(isLoggedIn), "onClick" to goToMessage), " 发送消息 ", 8, _uA(
                                "disabled"
                            ))
                        )),
                        _cE("view", _uM("class" to "button-row"), _uA(
                            _cE("button", _uM("class" to "btn btn-secondary", "disabled" to !unref(isLoggedIn), "onClick" to handleLogout), " 登出 ", 8, _uA(
                                "disabled"
                            ))
                        )),
                        _cE("view", _uM("class" to "log-area"), _uA(
                            _cE("text", _uM("class" to "log-title"), "日志："),
                            _cE("scroll-view", _uM("class" to "log-scroll", "scroll-y" to "true"), _uA(
                                _cE("text", _uM("class" to "log-content"), _tD(unref(logContent)), 1)
                            ))
                        )),
                        _cE("view", _uM("class" to "message-area"), _uA(
                            _cE("text", _uM("class" to "message-title"), "收到消息："),
                            _cE("scroll-view", _uM("class" to "message-scroll", "scroll-y" to "true"), _uA(
                                _cE(Fragment, null, RenderHelpers.renderList(unref(messageList), fun(msg, index, __index, _cached): Any {
                                    return _cE("view", _uM("key" to index, "class" to "message-item"), _uA(
                                        _cE("text", _uM("class" to "message-from"), _tD(msg.from), 1),
                                        _cE("text", _uM("class" to "message-type"), "[" + _tD(msg.body.type) + "]", 1),
                                        if (msg.body.type === "txt") {
                                            _cE("text", _uM("key" to 0, "class" to "message-content"), _tD(msg.body.message), 1)
                                        } else {
                                            _cC("v-if", true)
                                        }
                                    ))
                                }
                                ), 128)
                            ))
                        ))
                    ))
                ))
            }
        }
        val styles: Map<String, Map<String, Map<String, Any>>> by lazy {
            _nCS(_uA(
                styles0
            ))
        }
        val styles0: Map<String, Map<String, Map<String, Any>>>
            get() {
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f5f5", "paddingTop" to 20, "paddingRight" to 20, "paddingBottom" to 20, "paddingLeft" to 20)), "login-box" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to 10, "borderTopRightRadius" to 10, "borderBottomRightRadius" to 10, "borderBottomLeftRadius" to 10, "paddingTop" to 20, "paddingRight" to 20, "paddingBottom" to 20, "paddingLeft" to 20)), "title" to _pS(_uM("fontSize" to 24, "fontWeight" to "bold", "textAlign" to "center", "marginBottom" to 30, "color" to "#333333")), "input-row" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "marginBottom" to 15)), "label" to _pS(_uM("width" to 70, "fontSize" to 16, "color" to "#333333")), "input" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "height" to 45, "borderTopWidth" to 1, "borderRightWidth" to 1, "borderBottomWidth" to 1, "borderLeftWidth" to 1, "borderTopColor" to "#dddddd", "borderRightColor" to "#dddddd", "borderBottomColor" to "#dddddd", "borderLeftColor" to "#dddddd", "borderTopLeftRadius" to 5, "borderTopRightRadius" to 5, "borderBottomRightRadius" to 5, "borderBottomLeftRadius" to 5, "paddingLeft" to 10, "paddingRight" to 10, "fontSize" to 16)), "status-row" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "marginBottom" to 10)), "status-label" to _pS(_uM("fontSize" to 14, "color" to "#666666")), "status-value" to _uM("" to _uM("fontSize" to 14, "color" to "#333333"), ".connected" to _uM("color" to "#07c160"), ".disconnected" to _uM("color" to "#fa5151")), "button-row" to _pS(_uM("marginTop" to 15)), "btn" to _pS(_uM("height" to 45, "borderTopLeftRadius" to 5, "borderTopRightRadius" to 5, "borderBottomRightRadius" to 5, "borderBottomLeftRadius" to 5, "fontSize" to 16)), "btn-primary" to _pS(_uM("backgroundColor" to "#07c160", "color" to "#ffffff", "backgroundColor:disabled" to "#9ed9ad", "color:disabled" to "#ffffff")), "btn-secondary" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopWidth" to 1, "borderRightWidth" to 1, "borderBottomWidth" to 1, "borderLeftWidth" to 1, "borderTopColor" to "#07c160", "borderRightColor" to "#07c160", "borderBottomColor" to "#07c160", "borderLeftColor" to "#07c160", "color" to "#07c160", "borderTopColor:disabled" to "#9ed9ad", "borderRightColor:disabled" to "#9ed9ad", "borderBottomColor:disabled" to "#9ed9ad", "borderLeftColor:disabled" to "#9ed9ad", "color:disabled" to "#9ed9ad")), "log-area" to _pS(_uM("marginTop" to 20)), "log-title" to _pS(_uM("fontSize" to 14, "color" to "#666666", "marginBottom" to 10)), "log-scroll" to _pS(_uM("height" to 150, "backgroundColor" to "#f9f9f9", "borderTopLeftRadius" to 5, "borderTopRightRadius" to 5, "borderBottomRightRadius" to 5, "borderBottomLeftRadius" to 5, "paddingTop" to 10, "paddingRight" to 10, "paddingBottom" to 10, "paddingLeft" to 10)), "log-content" to _pS(_uM("fontSize" to 12, "color" to "#666666")), "message-area" to _pS(_uM("marginTop" to 20)), "message-title" to _pS(_uM("fontSize" to 14, "color" to "#666666", "marginBottom" to 10)), "message-scroll" to _pS(_uM("height" to 200, "backgroundColor" to "#f9f9f9", "borderTopLeftRadius" to 5, "borderTopRightRadius" to 5, "borderBottomRightRadius" to 5, "borderBottomLeftRadius" to 5, "paddingTop" to 10, "paddingRight" to 10, "paddingBottom" to 10, "paddingLeft" to 10)), "message-item" to _pS(_uM("flexDirection" to "row", "alignItems" to "flex-start", "marginBottom" to 8, "flexWrap" to "wrap")), "message-from" to _pS(_uM("fontSize" to 12, "color" to "#07c160", "fontWeight" to "bold", "marginRight" to 8)), "message-type" to _pS(_uM("fontSize" to 12, "color" to "#999999", "marginRight" to 8)), "message-content" to _pS(_uM("fontSize" to 12, "color" to "#333333", "width" to "100%", "marginTop" to 4)))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
