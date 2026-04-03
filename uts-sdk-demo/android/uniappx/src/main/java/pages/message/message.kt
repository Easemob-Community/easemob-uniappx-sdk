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
import io.dcloud.uniapp.extapi.chooseImage as uni_chooseImage
import uts.sdk.modules.easemobUtsSdk.sendTextMessage
import uts.sdk.modules.easemobUtsSdk.sendImageMessage
open class GenPagesMessageMessage : BasePage {
    constructor(__ins: ComponentInternalInstance, __renderer: String?) : super(__ins, __renderer) {}
    companion object {
        @Suppress("UNUSED_PARAMETER", "UNUSED_VARIABLE")
        var setup: (__props: GenPagesMessageMessage) -> Any? = fun(__props): Any? {
            val __ins = getCurrentInstance()!!
            val _ctx = __ins.proxy as GenPagesMessageMessage
            val _cache = __ins.renderCache
            val targetId = ref("")
            val chatType = ref("single")
            val messageContent = ref("")
            val logContent = ref("")
            val sendOriginalImage = ref(false)
            val canSend = computed(fun(): Boolean {
                return targetId.value.length > 0 && messageContent.value.length > 0
            }
            )
            fun gen_addLog_fn(message: String): Unit {
                val time = Date().toLocaleTimeString()
                logContent.value = "[" + time + "] " + message + "\n" + logContent.value
            }
            val addLog = ::gen_addLog_fn
            fun gen_handleSend_fn(): Unit {
                if (!canSend.value) {
                    addLog("错误：目标ID和消息内容不能为空")
                    return
                }
                val to = targetId.value.trim()
                val content = messageContent.value.trim()
                addLog("发送消息到 " + (if (chatType.value === "single") {
                    "用户"
                } else {
                    "群组"
                }
                ) + ": " + to)
                addLog("消息内容: " + content)
                sendTextMessage(content, to, chatType.value, _uO("onSuccess" to fun(){
                    console.log("[MessagePage] 消息发送成功回调触发")
                    addLog("消息发送成功")
                    messageContent.value = ""
                }
                , "onError" to fun(code: Number, message: String){
                    console.log("[MessagePage] 消息发送失败回调触发: code=" + code + ", message=" + message)
                    addLog("消息发送失败: code=" + code + ", message=" + message)
                }
                , "onProgress" to fun(progress: Number, status: String){
                    console.log("[MessagePage] 消息发送进度回调触发: progress=" + progress + ", status=" + status)
                    addLog("发送进度: " + progress + "%, status=" + status)
                }
                ))
            }
            val handleSend = ::gen_handleSend_fn
            fun gen_handleChooseImage_fn(sourceType: String): Unit {
                if (targetId.value.length == 0) {
                    addLog("错误：请先输入目标ID")
                    return
                }
                addLog("正在打开" + (if (sourceType === "camera") {
                    "相机"
                } else {
                    "相册"
                }
                ) + "...")
                uni_chooseImage(ChooseImageOptions(count = 1, sizeType = _uA(
                    "original",
                    "compressed"
                ), sourceType = _uA<String>(sourceType), success = fun(res){
                    val paths = res.tempFilePaths
                    if (paths != null && paths.length > 0) {
                        val imagePath = paths[0]!!
                        addLog("已选择图片: " + imagePath)
                        sendImageMessage(imagePath, sendOriginalImage.value, targetId.value.trim(), chatType.value, _uO("onSuccess" to fun(){
                            console.log("[MessagePage] 图片发送成功回调触发")
                            addLog("图片发送成功")
                        }
                        , "onError" to fun(code: Number, message: String){
                            console.log("[MessagePage] 图片发送失败回调触发: code=" + code + ", message=" + message)
                            addLog("图片发送失败: code=" + code + ", message=" + message)
                        }
                        , "onProgress" to fun(progress: Number, status: String){
                            console.log("[MessagePage] 图片发送进度回调触发: progress=" + progress)
                            addLog("图片上传进度: " + progress + "%")
                        }
                        ))
                    }
                }
                , fail = fun(err){
                    addLog("选择图片失败")
                }
                ))
            }
            val handleChooseImage = ::gen_handleChooseImage_fn
            onLoad(fun(_options){
                addLog("消息发送页面加载")
            }
            )
            return fun(): Any? {
                return _cE("scroll-view", _uM("class" to "container", "scroll-y" to "true"), _uA(
                    _cE("view", _uM("class" to "content"), _uA(
                        _cE("view", _uM("class" to "section"), _uA(
                            _cE("text", _uM("class" to "section-title"), "接收方信息"),
                            _cE("view", _uM("class" to "input-row"), _uA(
                                _cE("text", _uM("class" to "label"), "目标ID"),
                                _cE("input", _uM("class" to "input", "type" to "text", "modelValue" to unref(targetId), "onInput" to fun(`$event`: UniInputEvent){
                                    trySetRefValue(targetId, `$event`.detail.value)
                                }
                                , "placeholder" to "请输入接收方ID"), null, 40, _uA(
                                    "modelValue"
                                ))
                            )),
                            _cE("view", _uM("class" to "chat-type-row"), _uA(
                                _cE("text", _uM("class" to "label"), "会话类型"),
                                _cE("view", _uM("class" to "radio-group"), _uA(
                                    _cE("view", _uM("class" to "radio-item", "onClick" to fun(){
                                        chatType.value = "single"
                                    }
                                    ), _uA(
                                        _cE("view", _uM("class" to _nC(_uA(
                                            "radio-circle",
                                            if (unref(chatType) === "single") {
                                                "checked"
                                            } else {
                                                ""
                                            }
                                        ))), _uA(
                                            if (unref(chatType) === "single") {
                                                _cE("view", _uM("key" to 0, "class" to "radio-inner"))
                                            } else {
                                                _cC("v-if", true)
                                            }
                                        ), 2),
                                        _cE("text", _uM("class" to "radio-label"), "单聊")
                                    ), 8, _uA(
                                        "onClick"
                                    )),
                                    _cE("view", _uM("class" to "radio-item", "onClick" to fun(){
                                        chatType.value = "group"
                                    }
                                    ), _uA(
                                        _cE("view", _uM("class" to _nC(_uA(
                                            "radio-circle",
                                            if (unref(chatType) === "group") {
                                                "checked"
                                            } else {
                                                ""
                                            }
                                        ))), _uA(
                                            if (unref(chatType) === "group") {
                                                _cE("view", _uM("key" to 0, "class" to "radio-inner"))
                                            } else {
                                                _cC("v-if", true)
                                            }
                                        ), 2),
                                        _cE("text", _uM("class" to "radio-label"), "群组")
                                    ), 8, _uA(
                                        "onClick"
                                    ))
                                ))
                            ))
                        )),
                        _cE("view", _uM("class" to "section"), _uA(
                            _cE("text", _uM("class" to "section-title"), "消息内容"),
                            _cE("textarea", _uM("class" to "textarea", "modelValue" to unref(messageContent), "onInput" to fun(`$event`: UniInputEvent){
                                trySetRefValue(messageContent, `$event`.detail.value)
                            }
                            , "placeholder" to "请输入要发送的文本消息", "maxlength" to 500), null, 40, _uA(
                                "modelValue"
                            )),
                            _cE("text", _uM("class" to "char-count"), _tD(unref(messageContent).length) + "/500", 1)
                        )),
                        _cE("view", _uM("class" to "section"), _uA(
                            _cE("text", _uM("class" to "section-title"), "发送文本消息"),
                            _cE("view", _uM("class" to _nC(_uA(
                                "btn btn-primary",
                                _uM("btn-disabled" to !unref(canSend))
                            )), "hover-class" to "btn-primary-hover", "onClick" to handleSend), _uA(
                                _cE("text", _uM("class" to "btn-text"), "发送文本消息")
                            ), 2)
                        )),
                        _cE("view", _uM("class" to "section"), _uA(
                            _cE("text", _uM("class" to "section-title"), "发送图片"),
                            _cE("view", _uM("class" to "checkbox-row", "onClick" to fun(){
                                sendOriginalImage.value = !unref(sendOriginalImage)
                            }
                            ), _uA(
                                _cE("view", _uM("class" to _nC(_uA(
                                    "checkbox",
                                    if (unref(sendOriginalImage)) {
                                        "checked"
                                    } else {
                                        ""
                                    }
                                ))), _uA(
                                    if (isTrue(unref(sendOriginalImage))) {
                                        _cE("text", _uM("key" to 0, "class" to "checkbox-mark"), "✓")
                                    } else {
                                        _cC("v-if", true)
                                    }
                                ), 2),
                                _cE("text", _uM("class" to "checkbox-label"), "发送原图")
                            ), 8, _uA(
                                "onClick"
                            )),
                            _cE("view", _uM("class" to "image-btn-group"), _uA(
                                _cE("view", _uM("class" to "btn btn-image", "hover-class" to "btn-image-hover", "onClick" to fun(){
                                    handleChooseImage("album")
                                }
                                ), _uA(
                                    _cE("text", _uM("class" to "btn-text"), "从相册选择")
                                ), 8, _uA(
                                    "onClick"
                                )),
                                _cE("view", _uM("class" to "btn btn-image", "hover-class" to "btn-image-hover", "onClick" to fun(){
                                    handleChooseImage("camera")
                                }
                                ), _uA(
                                    _cE("text", _uM("class" to "btn-text"), "拍照")
                                ), 8, _uA(
                                    "onClick"
                                ))
                            ))
                        )),
                        _cE("view", _uM("class" to "section log-section"), _uA(
                            _cE("text", _uM("class" to "section-title"), "发送日志"),
                            _cE("scroll-view", _uM("class" to "log-scroll", "scroll-y" to "true"), _uA(
                                _cE("text", _uM("class" to "log-content"), _tD(unref(logContent)), 1)
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
                return _uM("container" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#f5f5f5")), "content" to _pS(_uM("paddingTop" to 15, "paddingRight" to 15, "paddingBottom" to 15, "paddingLeft" to 15)), "section" to _pS(_uM("backgroundColor" to "#ffffff", "borderTopLeftRadius" to 10, "borderTopRightRadius" to 10, "borderBottomRightRadius" to 10, "borderBottomLeftRadius" to 10, "paddingTop" to 15, "paddingRight" to 15, "paddingBottom" to 15, "paddingLeft" to 15, "marginBottom" to 15)), "section-title" to _pS(_uM("fontSize" to 16, "fontWeight" to "bold", "color" to "#333333", "marginBottom" to 15)), "input-row" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "marginBottom" to 15)), "label" to _pS(_uM("width" to 70, "fontSize" to 14, "color" to "#666666")), "input" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "height" to 40, "borderTopWidth" to 1, "borderRightWidth" to 1, "borderBottomWidth" to 1, "borderLeftWidth" to 1, "borderTopColor" to "#dddddd", "borderRightColor" to "#dddddd", "borderBottomColor" to "#dddddd", "borderLeftColor" to "#dddddd", "borderTopLeftRadius" to 5, "borderTopRightRadius" to 5, "borderBottomRightRadius" to 5, "borderBottomLeftRadius" to 5, "paddingLeft" to 10, "paddingRight" to 10, "fontSize" to 14)), "chat-type-row" to _pS(_uM("flexDirection" to "row", "alignItems" to "center")), "radio-group" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "flexDirection" to "row")), "radio-item" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "marginRight" to 30)), "radio-circle" to _uM("" to _uM("width" to 20, "height" to 20, "borderTopLeftRadius" to 10, "borderTopRightRadius" to 10, "borderBottomRightRadius" to 10, "borderBottomLeftRadius" to 10, "borderTopWidth" to 1, "borderRightWidth" to 1, "borderBottomWidth" to 1, "borderLeftWidth" to 1, "borderTopColor" to "#dddddd", "borderRightColor" to "#dddddd", "borderBottomColor" to "#dddddd", "borderLeftColor" to "#dddddd", "justifyContent" to "center", "alignItems" to "center", "marginRight" to 8), ".checked" to _uM("borderTopColor" to "#07c160", "borderRightColor" to "#07c160", "borderBottomColor" to "#07c160", "borderLeftColor" to "#07c160")), "radio-inner" to _pS(_uM("width" to 12, "height" to 12, "borderTopLeftRadius" to 6, "borderTopRightRadius" to 6, "borderBottomRightRadius" to 6, "borderBottomLeftRadius" to 6, "backgroundColor" to "#07c160")), "radio-label" to _pS(_uM("fontSize" to 14, "color" to "#333333")), "textarea" to _pS(_uM("width" to "100%", "height" to 120, "borderTopWidth" to 1, "borderRightWidth" to 1, "borderBottomWidth" to 1, "borderLeftWidth" to 1, "borderTopColor" to "#dddddd", "borderRightColor" to "#dddddd", "borderBottomColor" to "#dddddd", "borderLeftColor" to "#dddddd", "borderTopLeftRadius" to 5, "borderTopRightRadius" to 5, "borderBottomRightRadius" to 5, "borderBottomLeftRadius" to 5, "paddingTop" to 10, "paddingRight" to 10, "paddingBottom" to 10, "paddingLeft" to 10, "fontSize" to 14, "textAlign" to "left")), "char-count" to _pS(_uM("fontSize" to 12, "color" to "#999999", "textAlign" to "right", "marginTop" to 5)), "btn" to _pS(_uM("height" to 48, "borderTopLeftRadius" to 8, "borderTopRightRadius" to 8, "borderBottomRightRadius" to 8, "borderBottomLeftRadius" to 8, "justifyContent" to "center", "alignItems" to "center")), "btn-primary" to _pS(_uM("backgroundColor" to "#07c160")), "btn-primary-hover" to _pS(_uM("backgroundColor" to "#06ad56")), "btn-disabled" to _pS(_uM("backgroundColor" to "#9ed9ad")), "btn-text" to _pS(_uM("fontSize" to 16, "color" to "#ffffff", "fontWeight" to "bold")), "checkbox-row" to _pS(_uM("flexDirection" to "row", "alignItems" to "center", "marginBottom" to 15)), "checkbox" to _uM("" to _uM("width" to 22, "height" to 22, "borderTopWidth" to 2, "borderRightWidth" to 2, "borderBottomWidth" to 2, "borderLeftWidth" to 2, "borderTopColor" to "#dddddd", "borderRightColor" to "#dddddd", "borderBottomColor" to "#dddddd", "borderLeftColor" to "#dddddd", "borderTopLeftRadius" to 4, "borderTopRightRadius" to 4, "borderBottomRightRadius" to 4, "borderBottomLeftRadius" to 4, "justifyContent" to "center", "alignItems" to "center", "marginRight" to 10), ".checked" to _uM("backgroundColor" to "#07c160", "borderTopColor" to "#07c160", "borderRightColor" to "#07c160", "borderBottomColor" to "#07c160", "borderLeftColor" to "#07c160")), "checkbox-mark" to _pS(_uM("color" to "#ffffff", "fontSize" to 14, "fontWeight" to "bold")), "checkbox-label" to _pS(_uM("fontSize" to 14, "color" to "#333333")), "image-btn-group" to _pS(_uM("flexDirection" to "row", "justifyContent" to "space-between")), "btn-image" to _pS(_uM("flexGrow" to 1, "flexShrink" to 1, "flexBasis" to "0%", "backgroundColor" to "#576b95", "marginRight" to 10, "marginRight:last-child" to 0)), "btn-image-hover" to _pS(_uM("backgroundColor" to "#4a5a80")), "log-section" to _pS(_uM("minHeight" to 150)), "log-scroll" to _pS(_uM("height" to 150, "backgroundColor" to "#f9f9f9", "borderTopLeftRadius" to 5, "borderTopRightRadius" to 5, "borderBottomRightRadius" to 5, "borderBottomLeftRadius" to 5, "paddingTop" to 10, "paddingRight" to 10, "paddingBottom" to 10, "paddingLeft" to 10)), "log-content" to _pS(_uM("fontSize" to 12, "color" to "#666666")))
            }
        var inheritAttrs = true
        var inject: Map<String, Map<String, Any?>> = _uM()
        var emits: Map<String, Any?> = _uM()
        var props = _nP(_uM())
        var propsNeedCastKeys: UTSArray<String> = _uA()
        var components: Map<String, CreateVueComponent> = _uM()
    }
}
