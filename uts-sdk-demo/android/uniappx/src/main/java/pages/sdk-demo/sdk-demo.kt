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
import uts.sdk.modules.easemobUtsSdkBeta.initSDK as initSDK__1
import uts.sdk.modules.easemobUtsSdkBeta.login
import uts.sdk.modules.easemobUtsSdkBeta.logout
import uts.sdk.modules.easemobUtsSdkBeta.onConnectionStateChanged
open class GenPagesSdkDemoSdkDemo : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesSdkDemoSdkDemo) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesSdkDemoSdkDemo
            val _cache = __ins.renderCache
            val appKey = ref("")
            val userId = ref("")
            val password = ref("")
            val connectionStatus = ref("未连接")
            val logs = ref("")
            fun gen_addLog_fn(message: String): Unit {
                val time = Date().toLocaleTimeString()
                logs.value += "[" + time + "] " + message + "\n"
            }
            val addLog = ::gen_addLog_fn
            fun gen_clearLogs_fn(): Unit {
                logs.value = ""
            }
            val clearLogs = ::gen_clearLogs_fn
            fun gen_handleInit_fn(): UTSPromise<Unit> {
                return wrapUTSPromise(suspend {
                        try {
                            addLog("开始初始化...")
                            val config: UTSJSONObject = _uO("appKey" to appKey.value, "autoLogin" to false)
                            await(initSDK__1(config))
                            addLog("初始化成功")
                            onConnectionStateChanged(fun(event){
                                var statusText = "未知"
                                val stateVal = event.state as Number
                                if (stateVal == 0) {
                                    statusText = "已连接"
                                } else if (stateVal == 1) {
                                    statusText = "连接中"
                                } else if (stateVal == 2) {
                                    statusText = "已断开"
                                }
                                connectionStatus.value = statusText
                                addLog("连接状态变更: " + statusText)
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
                return _cE("view", _uM("class" to "container"), _uA(
                    _cE("text", _uM("class" to "title"), "SDK Beta 测试页面"),
                    _cE("text", _uM("class" to "subtitle"), "Android 初始化 + 登录测试"),
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
                        _cE("text", _uM("class" to "section-title"), "日志"),
                        _cE("scroll-view", _uM("class" to "log-view", "scroll-y" to ""), _uA(
                            _cE("text", _uM("class" to "log-text"), _tD(logs.value), 1)
                        )),
                        _cE("button", _uM("class" to "btn", "type" to "warn", "size" to "mini", "onClick" to clearLogs), "清空日志")
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
                return _uM("container" to _pS(_uM("paddingTop" to 20, "paddingRight" to 20, "paddingBottom" to 20, "paddingLeft" to 20)), "title" to _pS(_uM("fontSize" to 24, "fontWeight" to "bold", "textAlign" to "center", "marginBottom" to 10)), "subtitle" to _pS(_uM("fontSize" to 14, "color" to "#666666", "textAlign" to "center", "marginBottom" to 30)), "section" to _pS(_uM("marginBottom" to 25, "paddingTop" to 15, "paddingRight" to 15, "paddingBottom" to 15, "paddingLeft" to 15, "backgroundColor" to "#f5f5f5", "borderTopLeftRadius" to 8, "borderTopRightRadius" to 8, "borderBottomRightRadius" to 8, "borderBottomLeftRadius" to 8)), "section-title" to _pS(_uM("fontSize" to 16, "fontWeight" to "bold", "marginBottom" to 10)), "input" to _pS(_uM("height" to 40, "borderTopWidth" to 1, "borderRightWidth" to 1, "borderBottomWidth" to 1, "borderLeftWidth" to 1, "borderTopStyle" to "solid", "borderRightStyle" to "solid", "borderBottomStyle" to "solid", "borderLeftStyle" to "solid", "borderTopColor" to "#dddddd", "borderRightColor" to "#dddddd", "borderBottomColor" to "#dddddd", "borderLeftColor" to "#dddddd", "borderTopLeftRadius" to 4, "borderTopRightRadius" to 4, "borderBottomRightRadius" to 4, "borderBottomLeftRadius" to 4, "paddingTop" to 0, "paddingRight" to 10, "paddingBottom" to 0, "paddingLeft" to 10, "marginBottom" to 10, "backgroundColor" to "#ffffff")), "btn" to _pS(_uM("marginTop" to 10)), "status" to _pS(_uM("fontSize" to 14, "color" to "#333333", "paddingTop" to 10, "paddingRight" to 10, "paddingBottom" to 10, "paddingLeft" to 10, "backgroundColor" to "#ffffff", "borderTopLeftRadius" to 4, "borderTopRightRadius" to 4, "borderBottomRightRadius" to 4, "borderBottomLeftRadius" to 4)), "log-view" to _pS(_uM("height" to 200, "backgroundColor" to "#000000", "borderTopLeftRadius" to 4, "borderTopRightRadius" to 4, "borderBottomRightRadius" to 4, "borderBottomLeftRadius" to 4, "paddingTop" to 10, "paddingRight" to 10, "paddingBottom" to 10, "paddingLeft" to 10, "marginBottom" to 10)), "log-text" to _pS(_uM("color" to "#00ff00", "fontSize" to 12, "fontFamily" to "monospace", "whiteSpace" to "pre-wrap")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
