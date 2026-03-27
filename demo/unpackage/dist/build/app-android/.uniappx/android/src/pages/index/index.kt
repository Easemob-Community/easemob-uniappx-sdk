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
import uts.sdk.modules.easemobIm as easemobIM
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
            val target = ref("")
            val content = ref("")
            val logs = ref(_uA<String>())
            val initStatus = ref("未初始化")
            val loginStatus = ref("未登录")
            val addLog = fun(msg: String){
                val time = Date().toLocaleTimeString()
                logs.value.unshift("" + time + ": " + msg)
                if (logs.value.length > 20) {
                    logs.value.pop()
                }
            }
            val initSDK = fun(){
                if (appKey.value.length == 0) {
                    addLog("请输入AppKey")
                    return
                }
                val ok = easemobIM.im.init(appKey.value)
                if (ok) {
                    initStatus.value = "已初始化"
                    addLog("初始化成功")
                    easemobIM.im.onMessage(fun(msg: Any){
                        addLog("收到: " + msg.from + " -> " + msg.content)
                    })
                } else {
                    addLog("初始化失败")
                }
            }
            val doLogin = fun(){
                if (username.value.length == 0 || password.value.length == 0) {
                    addLog("请输入用户名密码")
                    return
                }
                easemobIM.im.login(username.value, password.value, fun(){
                    loginStatus.value = "已登录: " + username.value
                    addLog("登录成功")
                }
                , fun(code: Number, msg: String){
                    addLog("登录失败: " + code + " " + msg)
                }
                )
            }
            val doLogout = fun(){
                easemobIM.im.logout(fun(){
                    loginStatus.value = "未登录"
                    addLog("已登出")
                }
                )
            }
            val sendMsg = fun(){
                if (target.value.length == 0 || content.value.length == 0) {
                    addLog("请输入对方用户名和消息内容")
                    return
                }
                easemobIM.im.sendText(target.value, content.value, fun(){
                    addLog("发送成功")
                    content.value = ""
                }
                , fun(code: Number, msg: String){
                    addLog("发送失败: " + code + " " + msg)
                }
                )
            }
            return fun(): Any? {
                return _cE("view", _uM("class" to "container"), _uA(
                    _cE("text", _uM("class" to "title"), "环信IM Demo"),
                    _cE("view", _uM("class" to "card"), _uA(
                        _cE("text", _uM("class" to "label"), "AppKey:"),
                        _cE("input", _uM("class" to "input", "modelValue" to appKey.value, "onInput" to fun(`$event`: UniInputEvent){
                            appKey.value = `$event`.detail.value
                        }
                        , "placeholder" to "请输入AppKey"), null, 40, _uA(
                            "modelValue",
                            "onInput"
                        )),
                        _cE("button", _uM("class" to "btn", "onClick" to initSDK), "初始化"),
                        _cE("text", _uM("class" to "info"), _tD(initStatus.value), 1)
                    )),
                    _cE("view", _uM("class" to "card"), _uA(
                        _cE("text", _uM("class" to "label"), "用户名:"),
                        _cE("input", _uM("class" to "input", "modelValue" to username.value, "onInput" to fun(`$event`: UniInputEvent){
                            username.value = `$event`.detail.value
                        }
                        , "placeholder" to "用户名"), null, 40, _uA(
                            "modelValue",
                            "onInput"
                        )),
                        _cE("text", _uM("class" to "label"), "密码:"),
                        _cE("input", _uM("class" to "input", "modelValue" to password.value, "onInput" to fun(`$event`: UniInputEvent){
                            password.value = `$event`.detail.value
                        }
                        , "placeholder" to "密码", "password" to ""), null, 40, _uA(
                            "modelValue",
                            "onInput"
                        )),
                        _cE("button", _uM("class" to "btn", "onClick" to doLogin), "登录"),
                        _cE("button", _uM("class" to "btn btn-gray", "onClick" to doLogout), "登出"),
                        _cE("text", _uM("class" to "info"), _tD(loginStatus.value), 1)
                    )),
                    _cE("view", _uM("class" to "card"), _uA(
                        _cE("text", _uM("class" to "label"), "发给:"),
                        _cE("input", _uM("class" to "input", "modelValue" to target.value, "onInput" to fun(`$event`: UniInputEvent){
                            target.value = `$event`.detail.value
                        }
                        , "placeholder" to "对方用户名"), null, 40, _uA(
                            "modelValue",
                            "onInput"
                        )),
                        _cE("text", _uM("class" to "label"), "内容:"),
                        _cE("input", _uM("class" to "input", "modelValue" to content.value, "onInput" to fun(`$event`: UniInputEvent){
                            content.value = `$event`.detail.value
                        }
                        , "placeholder" to "消息内容"), null, 40, _uA(
                            "modelValue",
                            "onInput"
                        )),
                        _cE("button", _uM("class" to "btn", "onClick" to sendMsg), "发送")
                    )),
                    _cE("view", _uM("class" to "card"), _uA(
                        _cE("text", _uM("class" to "label"), "日志:"),
                        _cE("scroll-view", _uM("class" to "log-box"), _uA(
                            _cE(Fragment, null, RenderHelpers.renderList(logs.value, fun(log, i, __index, _cached): Any {
                                return _cE("text", _uM("key" to i, "class" to "log-text"), _tD(log), 1)
                            }
                            ), 128)
                        ))
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
                return _uM("container" to _pS(_uM("paddingTop" to 16, "paddingRight" to 16, "paddingBottom" to 16, "paddingLeft" to 16, "backgroundColor" to "#f5f5f5")), "title" to _pS(_uM("fontSize" to 20, "fontWeight" to "bold", "textAlign" to "center", "marginTop" to 20, "marginRight" to 0, "marginBottom" to 20, "marginLeft" to 0)), "card" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to 8, "borderTopRightRadius" to 8, "borderBottomRightRadius" to 8, "borderBottomLeftRadius" to 8, "paddingTop" to 12, "paddingRight" to 12, "paddingBottom" to 12, "paddingLeft" to 12, "marginBottom" to 12)), "label" to _pS(_uM("fontSize" to 14, "color" to "#666666", "marginBottom" to 6)), "input" to _pS(_uM("height" to 40, "borderTopWidth" to 1, "borderRightWidth" to 1, "borderBottomWidth" to 1, "borderLeftWidth" to 1, "borderTopStyle" to "solid", "borderRightStyle" to "solid", "borderBottomStyle" to "solid", "borderLeftStyle" to "solid", "borderTopColor" to "#dddddd", "borderRightColor" to "#dddddd", "borderBottomColor" to "#dddddd", "borderLeftColor" to "#dddddd", "borderTopLeftRadius" to 6, "borderTopRightRadius" to 6, "borderBottomRightRadius" to 6, "borderBottomLeftRadius" to 6, "paddingTop" to 0, "paddingRight" to 10, "paddingBottom" to 0, "paddingLeft" to 10, "marginBottom" to 10, "fontSize" to 14)), "btn" to _pS(_uM("height" to 40, "backgroundColor" to "#007AFF", "color" to "#ffffff", "borderTopLeftRadius" to 6, "borderTopRightRadius" to 6, "borderBottomRightRadius" to 6, "borderBottomLeftRadius" to 6, "fontSize" to 15, "marginBottom" to 8)), "btn-gray" to _pS(_uM("backgroundColor" to "#999999")), "info" to _pS(_uM("fontSize" to 13, "color" to "#333333", "marginTop" to 6)), "log-box" to _pS(_uM("height" to 150, "backgroundColor" to "#1e1e1e", "borderTopLeftRadius" to 6, "borderTopRightRadius" to 6, "borderBottomRightRadius" to 6, "borderBottomLeftRadius" to 6, "paddingTop" to 8, "paddingRight" to 8, "paddingBottom" to 8, "paddingLeft" to 8)), "log-text" to _pS(_uM("fontSize" to 12, "color" to "#00ff00", "lineHeight" to 1.5)))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
