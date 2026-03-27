@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "NAME_SHADOWING", "UNNECESSARY_NOT_NULL_ASSERTION")
package uni.UNIAF9C6BE
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
import uts.sdk.modules.easemobIm.init
import uts.sdk.modules.easemobIm.login as emLogin
import uts.sdk.modules.easemobIm.logout as emLogout
import uts.sdk.modules.easemobIm.getVersion
import uts.sdk.modules.easemobIm.isInitialized
import uts.sdk.modules.easemobIm.isLoggedIn
open class GenPagesIndexIndex : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesIndexIndex) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesIndexIndex
            val _cache = __ins.renderCache
            val appKey = ref("easemob-demo#support")
            val username = ref("hfp")
            val password = ref("1")
            val initialized = ref(false)
            val logined = ref(false)
            val logs = ref(_uA<String>())
            val addLog = fun(msg: String){
                val time = Date().toLocaleTimeString()
                logs.value.push("[" + time + "] " + msg)
            }
            val initSDK = fun(){
                if (appKey.value.length == 0) {
                    addLog("错误: 请输入AppKey")
                    return
                }
                addLog("开始初始化SDK...")
                val result = init(appKey.value)
                initialized.value = isInitialized()
                if (result) {
                    addLog("✅ 初始化成功!")
                } else {
                    addLog("❌ 初始化失败!")
                }
            }
            val login = fun(){
                if (username.value.length == 0 || password.value.length == 0) {
                    addLog("错误: 请输入用户名和密码")
                    return
                }
                addLog("开始登录: " + username.value + "...")
                emLogin(username.value, password.value, fun(){
                    logined.value = isLoggedIn()
                    addLog("✅ 登录成功!")
                }
                , fun(code: Number, error: String){
                    addLog("❌ 登录失败: [" + code + "] " + error)
                }
                )
            }
            val logout = fun(){
                addLog("开始登出...")
                emLogout(fun(){
                    logined.value = isLoggedIn()
                    addLog("✅ 登出成功!")
                }
                )
            }
            val checkVersion = fun(){
                val version = getVersion()
                addLog("SDK版本: " + version)
            }
            val clearLogs = fun(){
                logs.value = _uA()
            }
            return fun(): Any? {
                return _cE("view", _uM("class" to "container"), _uA(
                    _cE("text", _uM("class" to "title"), "环信IM SDK 测试"),
                    _cE("view", _uM("class" to "form"), _uA(
                        _cE("text", _uM("class" to "label"), "AppKey:"),
                        _cE("input", _uM("class" to "input", "modelValue" to appKey.value, "onInput" to fun(`$event`: UniInputEvent){
                            appKey.value = `$event`.detail.value
                        }
                        , "placeholder" to "请输入AppKey"), null, 40, _uA(
                            "modelValue",
                            "onInput"
                        )),
                        _cE("text", _uM("class" to "label"), "用户名:"),
                        _cE("input", _uM("class" to "input", "modelValue" to username.value, "onInput" to fun(`$event`: UniInputEvent){
                            username.value = `$event`.detail.value
                        }
                        , "placeholder" to "请输入用户名"), null, 40, _uA(
                            "modelValue",
                            "onInput"
                        )),
                        _cE("text", _uM("class" to "label"), "密码:"),
                        _cE("input", _uM("class" to "input", "modelValue" to password.value, "onInput" to fun(`$event`: UniInputEvent){
                            password.value = `$event`.detail.value
                        }
                        , "placeholder" to "请输入密码", "password" to ""), null, 40, _uA(
                            "modelValue",
                            "onInput"
                        )),
                        _cE("button", _uM("class" to "btn-primary", "onClick" to initSDK), "1. 初始化SDK"),
                        _cE("button", _uM("class" to "btn-primary", "onClick" to login, "disabled" to !initialized.value), "2. 登录", 8, _uA(
                            "disabled"
                        )),
                        _cE("button", _uM("class" to "btn-secondary", "onClick" to logout, "disabled" to !logined.value), "3. 登出", 8, _uA(
                            "disabled"
                        )),
                        _cE("button", _uM("class" to "btn-secondary", "onClick" to checkVersion), "获取版本")
                    )),
                    _cE("view", _uM("class" to "status"), _uA(
                        _cE("text", _uM("class" to "status-text"), "初始化: " + _tD(if (initialized.value) {
                            "✅"
                        } else {
                            "❌"
                        }
                        ) + " | 登录: " + _tD(if (logined.value) {
                            "✅"
                        } else {
                            "❌"
                        }
                        ), 1)
                    )),
                    _cE("view", _uM("class" to "logs"), _uA(
                        _cE("text", _uM("class" to "logs-title"), "运行日志:"),
                        _cE("scroll-view", _uM("class" to "log-scroll", "scroll-y" to ""), _uA(
                            _cE(Fragment, null, RenderHelpers.renderList(logs.value, fun(log, index, __index, _cached): Any {
                                return _cE("text", _uM("key" to index, "class" to "log-item"), _tD(log), 1)
                            }
                            ), 128)
                        )),
                        _cE("button", _uM("class" to "btn-clear", "onClick" to clearLogs), "清空日志")
                    ))
                ))
            }
        }
        val styles: Map<String, Map<String, Map<String, Any>>> by lazy {
            _nCS(_uA(
                styles0
            ), _uA(
                GenApp.styles
            ))
        }
        val styles0: Map<String, Map<String, Map<String, Any>>>
            get() {
                return _uM("container" to _pS(_uM("flexDirection" to "column", "paddingTop" to 20, "paddingRight" to 20, "paddingBottom" to 20, "paddingLeft" to 20, "backgroundColor" to "#f5f5f5")), "title" to _pS(_uM("fontSize" to 22, "fontWeight" to "bold", "marginBottom" to 20, "textAlign" to "center", "color" to "#333333")), "form" to _pS(_uM("flexDirection" to "column", "backgroundColor" to "#ffffff", "paddingTop" to 15, "paddingRight" to 15, "paddingBottom" to 15, "paddingLeft" to 15, "borderTopLeftRadius" to 8, "borderTopRightRadius" to 8, "borderBottomRightRadius" to 8, "borderBottomLeftRadius" to 8, "marginBottom" to 15)), "label" to _pS(_uM("fontSize" to 14, "color" to "#666666", "marginBottom" to 8)), "input" to _pS(_uM("height" to 44, "borderTopWidth" to 1, "borderRightWidth" to 1, "borderBottomWidth" to 1, "borderLeftWidth" to 1, "borderTopStyle" to "solid", "borderRightStyle" to "solid", "borderBottomStyle" to "solid", "borderLeftStyle" to "solid", "borderTopColor" to "#dddddd", "borderRightColor" to "#dddddd", "borderBottomColor" to "#dddddd", "borderLeftColor" to "#dddddd", "borderTopLeftRadius" to 6, "borderTopRightRadius" to 6, "borderBottomRightRadius" to 6, "borderBottomLeftRadius" to 6, "paddingTop" to 0, "paddingRight" to 12, "paddingBottom" to 0, "paddingLeft" to 12, "fontSize" to 14, "marginBottom" to 15)), "btn-primary" to _pS(_uM("backgroundColor" to "#0066ff", "color" to "#ffffff", "height" to 44, "borderTopLeftRadius" to 6, "borderTopRightRadius" to 6, "borderBottomRightRadius" to 6, "borderBottomLeftRadius" to 6, "fontSize" to 16, "marginBottom" to 10)), "btn-secondary" to _pS(_uM("backgroundColor" to "#f0f0f0", "color" to "#333333", "height" to 44, "borderTopLeftRadius" to 6, "borderTopRightRadius" to 6, "borderBottomRightRadius" to 6, "borderBottomLeftRadius" to 6, "fontSize" to 16, "marginBottom" to 10)), "btn-clear" to _pS(_uM("backgroundColor" to "#ff4444", "color" to "#ffffff", "height" to 36, "borderTopLeftRadius" to 6, "borderTopRightRadius" to 6, "borderBottomRightRadius" to 6, "borderBottomLeftRadius" to 6, "fontSize" to 14, "marginTop" to 10)), "status" to _pS(_uM("backgroundColor" to "#ffffff", "paddingTop" to 15, "paddingRight" to 15, "paddingBottom" to 15, "paddingLeft" to 15, "borderTopLeftRadius" to 8, "borderTopRightRadius" to 8, "borderBottomRightRadius" to 8, "borderBottomLeftRadius" to 8, "marginBottom" to 15)), "status-text" to _pS(_uM("fontSize" to 14, "color" to "#333333")), "logs" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "flexDirection" to "column", "backgroundColor" to "#ffffff", "paddingTop" to 15, "paddingRight" to 15, "paddingBottom" to 15, "paddingLeft" to 15, "borderTopLeftRadius" to 8, "borderTopRightRadius" to 8, "borderBottomRightRadius" to 8, "borderBottomLeftRadius" to 8)), "logs-title" to _pS(_uM("fontSize" to 14, "fontWeight" to "bold", "marginBottom" to 10, "color" to "#333333")), "log-scroll" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "height" to 300, "backgroundColor" to "#1e1e1e", "borderTopLeftRadius" to 6, "borderTopRightRadius" to 6, "borderBottomRightRadius" to 6, "borderBottomLeftRadius" to 6, "paddingTop" to 10, "paddingRight" to 10, "paddingBottom" to 10, "paddingLeft" to 10)), "log-item" to _pS(_uM("fontSize" to 12, "color" to "#4caf50", "lineHeight" to "20px", "marginBottom" to 4)))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
