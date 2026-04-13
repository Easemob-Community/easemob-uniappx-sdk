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
import io.dcloud.uniapp.extapi.navigateTo as uni_navigateTo
open class GenPagesIndexIndex : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesIndexIndex) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesIndexIndex
            val _cache = __ins.renderCache
            val title = ref("Hello")
            fun gen_goToLogin_fn(): Unit {
                uni_navigateTo(NavigateToOptions(url = "/pages/login/login"))
            }
            val goToLogin = ::gen_goToLogin_fn
            fun gen_goToSDKDemo_fn(): Unit {
                uni_navigateTo(NavigateToOptions(url = "/pages/sdk-demo/sdk-demo"))
            }
            val goToSDKDemo = ::gen_goToSDKDemo_fn
            return fun(): Any? {
                return _cE("view", _uM("class" to "container"), _uA(
                    _cE("image", _uM("class" to "logo", "src" to "/static/logo.png")),
                    _cE("text", _uM("class" to "title"), _tD(unref(title)), 1),
                    _cE("view", _uM("class" to "button-area"), _uA(
                        _cE("button", _uM("class" to "btn", "type" to "primary", "onClick" to goToLogin), "前往登录"),
                        _cE("button", _uM("class" to "btn", "type" to "default", "onClick" to goToSDKDemo), "SDK 测试页面")
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "alignItems" to "center")), "logo" to _pS(_uM("height" to 100, "width" to 100, "marginTop" to 100, "marginRight" to "auto", "marginBottom" to 25, "marginLeft" to "auto")), "title" to _pS(_uM("fontSize" to 18, "color" to "#8f8f94", "textAlign" to "center")), "button-area" to _pS(_uM("marginTop" to 30, "width" to 200)), "btn" to _pS(_uM("width" to "100%")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
