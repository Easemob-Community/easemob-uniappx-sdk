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
import uts.sdk.modules.easemobUtsSdkBeta.initSDK
import uts.sdk.modules.easemobUtsSdkBeta.login
import uts.sdk.modules.easemobUtsSdkBeta.logout
import uts.sdk.modules.easemobUtsSdkBeta.onConnected
import uts.sdk.modules.easemobUtsSdkBeta.onDisconnected
import uts.sdk.modules.easemobUtsSdkBeta.onLogout
open class GenPagesSdkDemoSdkDemo : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesSdkDemoSdkDemo) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesSdkDemoSdkDemo
            val _cache = __ins.renderCache
            val appKey = ref("easemob-demo#support")
            val userId = ref("hfp")
            val password = ref("1")
            val connectionStatus = ref("未连接")
            val logs = ref("")
            val logScrollTop = ref(0)
            fun gen_addLog_fn(message: String): Unit {
                val time = Date().toLocaleTimeString()
                logs.value += "[" + time + "] " + message + "\n"
                setTimeout(fun(){
                    logScrollTop.value = 99999
                }
                , 50)
            }
            val addLog = ::gen_addLog_fn
            fun gen_clearLogs_fn(): Unit {
                logs.value = ""
                logScrollTop.value = 0
            }
            val clearLogs = ::gen_clearLogs_fn
            fun gen_handleInit_fn(): UTSPromise<Unit> {
                return wrapUTSPromise(suspend {
                        try {
                            addLog("开始初始化...")
                            val config: UTSJSONObject = _uO("appKey" to appKey.value, "autoLogin" to false)
                            await(initSDK(config))
                            addLog("初始化成功")
                            onConnected(fun(){
                                console.log("[sdk-demo] onConnected callback executed")
                                setTimeout(fun(){
                                    connectionStatus.value = "已连接"
                                    addLog("[beta] onConnected: 已连接")
                                }
                                , 0)
                            }
                            )
                            onDisconnected(fun(errorCode){
                                console.log("[sdk-demo] onDisconnected callback executed, errorCode=" + errorCode)
                                setTimeout(fun(){
                                    connectionStatus.value = "已断开"
                                    addLog("[beta] onDisconnected: 已断开, errorCode=" + errorCode)
                                }
                                , 0)
                            }
                            )
                            onLogout(fun(errorCode){
                                console.log("[sdk-demo] onLogout callback executed, errorCode=" + errorCode)
                                setTimeout(fun(){
                                    connectionStatus.value = "已登出"
                                    addLog("[beta] onLogout: 被登出, errorCode=" + errorCode)
                                }
                                , 0)
                            }
                            )
                        }
                         catch (error: Throwable) {
                            addLog("初始化失败: " + error)
                        }
                })
            }
            val handleInit = ::gen_handleInit_fn
            fun gen_handleLogin_fn(): UTSPromise<Unit> {
                return wrapUTSPromise(suspend {
                        try {
                            addLog("开始登录...")
                            val config: UTSJSONObject = _uO("userId" to userId.value, "password" to password.value, "useToken" to false)
                            await(login(config))
                            addLog("登录成功")
                        }
                         catch (error: Throwable) {
                            addLog("登录失败: " + error)
                        }
                })
            }
            val handleLogin = ::gen_handleLogin_fn
            fun gen_handleLogout_fn(): UTSPromise<Unit> {
                return wrapUTSPromise(suspend {
                        try {
                            addLog("开始登出...")
                            await(logout())
                            addLog("登出成功")
                        }
                         catch (error: Throwable) {
                            addLog("登出失败: " + error)
                        }
                })
            }
            val handleLogout = ::gen_handleLogout_fn
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container", "scroll-y" to "true"), _uA(
                    _cE("view", _uM("class" to "content"), _uA(
                        _cE("text", _uM("class" to "title"), "SDK Beta 测试页面"),
                        _cE("text", _uM("class" to "subtitle"), "Android / iOS 初始化 + 登录测试"),
                        _cE("view", _uM("class" to "section"), _uA(
                            _cE("text", _uM("class" to "section-title"), "初始化"),
                            _cE("input", _uM("class" to "input", "modelValue" to appKey.value, "onInput" to fun(`$event`: UniInputEvent){
                                appKey.value = `$event`.detail.value
                            }
                            , "placeholder" to "输入 AppKey"), null, 40, _uA(
                                "modelValue",
                                "onInput"
                            )),
                            _cE("button", _uM("class" to "btn", "type" to "primary", "onClick" to handleInit), "初始化 SDK")
                        )),
                        _cE("view", _uM("class" to "section"), _uA(
                            _cE("text", _uM("class" to "section-title"), "登录"),
                            _cE("input", _uM("class" to "input", "modelValue" to userId.value, "onInput" to fun(`$event`: UniInputEvent){
                                userId.value = `$event`.detail.value
                            }
                            , "placeholder" to "输入用户 ID"), null, 40, _uA(
                                "modelValue",
                                "onInput"
                            )),
                            _cE("input", _uM("class" to "input", "modelValue" to password.value, "onInput" to fun(`$event`: UniInputEvent){
                                password.value = `$event`.detail.value
                            }
                            , "placeholder" to "输入密码", "password" to ""), null, 40, _uA(
                                "modelValue",
                                "onInput"
                            )),
                            _cE("button", _uM("class" to "btn", "type" to "primary", "onClick" to handleLogin), "登录"),
                            _cE("button", _uM("class" to "btn", "type" to "default", "onClick" to handleLogout), "登出")
                        )),
                        _cE("view", _uM("class" to "section"), _uA(
                            _cE("text", _uM("class" to "section-title"), "连接状态"),
                            _cE("text", _uM("class" to "status"), "状态: " + _tD(connectionStatus.value), 1)
                        )),
                        _cE("view", _uM("class" to "section"), _uA(
                            _cE("view", _uM("class" to "log-header"), _uA(
                                _cE("text", _uM("class" to "section-title"), "日志"),
                                _cE("button", _uM("class" to "log-clear", "type" to "warn", "size" to "mini", "onClick" to clearLogs), "清空")
                            )),
                            _cE("scroll-view", _uM("class" to "log-scroll", "scroll-y" to "true", "scroll-top" to logScrollTop.value), _uA(
                                _cE("text", _uM("class" to "log-text"), _tD(logs.value), 1)
                            ), 8, _uA(
                                "scroll-top"
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f5f5")), "content" to _pS(_uM("paddingTop" to 15, "paddingRight" to 15, "paddingBottom" to 15, "paddingLeft" to 15)), "title" to _pS(_uM("fontSize" to 24, "fontWeight" to "bold", "textAlign" to "center", "marginBottom" to 10, "color" to "#333333")), "subtitle" to _pS(_uM("fontSize" to 14, "color" to "#666666", "textAlign" to "center", "marginBottom" to 20)), "section" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to 10, "borderTopRightRadius" to 10, "borderBottomRightRadius" to 10, "borderBottomLeftRadius" to 10, "paddingTop" to 15, "paddingRight" to 15, "paddingBottom" to 15, "paddingLeft" to 15, "marginBottom" to 15)), "section-title" to _pS(_uM("fontSize" to 16, "fontWeight" to "bold", "color" to "#333333", "marginBottom" to 10)), "input" to _pS(_uM("height" to 40, "borderTopWidth" to 1, "borderRightWidth" to 1, "borderBottomWidth" to 1, "borderLeftWidth" to 1, "borderTopColor" to "#dddddd", "borderRightColor" to "#dddddd", "borderBottomColor" to "#dddddd", "borderLeftColor" to "#dddddd", "borderTopLeftRadius" to 5, "borderTopRightRadius" to 5, "borderBottomRightRadius" to 5, "borderBottomLeftRadius" to 5, "paddingLeft" to 10, "paddingRight" to 10, "marginBottom" to 10, "backgroundColor" to "#f9f9f9", "fontSize" to 14)), "btn" to _pS(_uM("marginTop" to 10)), "status" to _pS(_uM("fontSize" to 14, "color" to "#333333", "paddingTop" to 10, "paddingRight" to 10, "paddingBottom" to 10, "paddingLeft" to 10, "backgroundColor" to "#f9f9f9", "borderTopLeftRadius" to 5, "borderTopRightRadius" to 5, "borderBottomRightRadius" to 5, "borderBottomLeftRadius" to 5)), "log-header" to _pS(_uM("flexDirection" to "row", "justifyContent" to "space-between", "alignItems" to "center", "marginBottom" to 10)), "log-clear" to _pS(_uM("marginTop" to 0, "marginRight" to 0, "marginBottom" to 0, "marginLeft" to 0)), "log-scroll" to _pS(_uM("height" to 150, "backgroundColor" to "#f9f9f9", "borderTopLeftRadius" to 5, "borderTopRightRadius" to 5, "borderBottomRightRadius" to 5, "borderBottomLeftRadius" to 5, "paddingTop" to 10, "paddingRight" to 10, "paddingBottom" to 10, "paddingLeft" to 10)), "log-text" to _pS(_uM("fontSize" to 12, "color" to "#666666")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
