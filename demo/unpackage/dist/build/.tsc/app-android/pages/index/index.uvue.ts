import { ref } from 'vue'
import { 
  init, 
  login as emLogin, 
  logout as emLogout,
  getVersion, 
  isInitialized, 
  isLoggedIn 
} from '@/uni_modules/easemob-im'


const __sfc__ = defineComponent({
  __name: 'index',
  setup(__props) {
const __ins = getCurrentInstance()!;
const _ctx = __ins.proxy as InstanceType<typeof __sfc__>;
const _cache = __ins.renderCache;

const appKey = ref('easemob-demo#support')
const username = ref('hfp')
const password = ref('1')
const initialized = ref(false)
const logined = ref(false)
const logs = ref<string[]>([])

const addLog = (msg: string) => {
  const time = new Date().toLocaleTimeString()
  logs.value.push(`[${time}] ${msg}`)
}

const initSDK = () => {
  if (appKey.value.length == 0) {
    addLog('错误: 请输入AppKey')
    return
  }
  
  addLog('开始初始化SDK...')
  const result = init(appKey.value)
  initialized.value = isInitialized()
  
  if (result) {
    addLog('✅ 初始化成功!')
  } else {
    addLog('❌ 初始化失败!')
  }
}

const login = () => {
  if (username.value.length == 0 || password.value.length == 0) {
    addLog('错误: 请输入用户名和密码')
    return
  }
  
  addLog(`开始登录: ${username.value}...`)
  
  emLogin(
    username.value,
    password.value,
    () => {
      logined.value = isLoggedIn()
      addLog('✅ 登录成功!')
    },
    (code: number, error: string) => {
      addLog(`❌ 登录失败: [${code}] ${error}`)
    }
  )
}

const logout = () => {
  addLog('开始登出...')
  
  emLogout(() => {
    logined.value = isLoggedIn()
    addLog('✅ 登出成功!')
  })
}

const checkVersion = () => {
  const version = getVersion()
  addLog(`SDK版本: ${version}`)
}

const clearLogs = () => {
  logs.value = []
}

return (): any | null => {

  return _cE("view", _uM({ class: "container" }), [
    _cE("text", _uM({ class: "title" }), "环信IM SDK 测试"),
    _cE("view", _uM({ class: "form" }), [
      _cE("text", _uM({ class: "label" }), "AppKey:"),
      _cE("input", _uM({
        class: "input",
        modelValue: appKey.value,
        onInput: ($event: UniInputEvent) => {(appKey).value = $event.detail.value},
        placeholder: "请输入AppKey"
      }), null, 40 /* PROPS, NEED_HYDRATION */, ["modelValue", "onInput"]),
      _cE("text", _uM({ class: "label" }), "用户名:"),
      _cE("input", _uM({
        class: "input",
        modelValue: username.value,
        onInput: ($event: UniInputEvent) => {(username).value = $event.detail.value},
        placeholder: "请输入用户名"
      }), null, 40 /* PROPS, NEED_HYDRATION */, ["modelValue", "onInput"]),
      _cE("text", _uM({ class: "label" }), "密码:"),
      _cE("input", _uM({
        class: "input",
        modelValue: password.value,
        onInput: ($event: UniInputEvent) => {(password).value = $event.detail.value},
        placeholder: "请输入密码",
        password: ""
      }), null, 40 /* PROPS, NEED_HYDRATION */, ["modelValue", "onInput"]),
      _cE("button", _uM({
        class: "btn-primary",
        onClick: initSDK
      }), "1. 初始化SDK"),
      _cE("button", _uM({
        class: "btn-primary",
        onClick: login,
        disabled: !initialized.value
      }), "2. 登录", 8 /* PROPS */, ["disabled"]),
      _cE("button", _uM({
        class: "btn-secondary",
        onClick: logout,
        disabled: !logined.value
      }), "3. 登出", 8 /* PROPS */, ["disabled"]),
      _cE("button", _uM({
        class: "btn-secondary",
        onClick: checkVersion
      }), "获取版本")
    ]),
    _cE("view", _uM({ class: "status" }), [
      _cE("text", _uM({ class: "status-text" }), "初始化: " + _tD(initialized.value ? '✅' : '❌') + " | 登录: " + _tD(logined.value ? '✅' : '❌'), 1 /* TEXT */)
    ]),
    _cE("view", _uM({ class: "logs" }), [
      _cE("text", _uM({ class: "logs-title" }), "运行日志:"),
      _cE("scroll-view", _uM({
        class: "log-scroll",
        "scroll-y": ""
      }), [
        _cE(Fragment, null, RenderHelpers.renderList(logs.value, (log, index, __index, _cached): any => {
          return _cE("text", _uM({
            key: index,
            class: "log-item"
          }), _tD(log), 1 /* TEXT */)
        }), 128 /* KEYED_FRAGMENT */)
      ]),
      _cE("button", _uM({
        class: "btn-clear",
        onClick: clearLogs
      }), "清空日志")
    ])
  ])
}
}

})
export default __sfc__
const GenPagesIndexIndexStyles = [_uM([["container", _pS(_uM([["flexDirection", "column"], ["paddingTop", 20], ["paddingRight", 20], ["paddingBottom", 20], ["paddingLeft", 20], ["backgroundColor", "#f5f5f5"]]))], ["title", _pS(_uM([["fontSize", 22], ["fontWeight", "bold"], ["marginBottom", 20], ["textAlign", "center"], ["color", "#333333"]]))], ["form", _pS(_uM([["flexDirection", "column"], ["backgroundColor", "#ffffff"], ["paddingTop", 15], ["paddingRight", 15], ["paddingBottom", 15], ["paddingLeft", 15], ["borderTopLeftRadius", 8], ["borderTopRightRadius", 8], ["borderBottomRightRadius", 8], ["borderBottomLeftRadius", 8], ["marginBottom", 15]]))], ["label", _pS(_uM([["fontSize", 14], ["color", "#666666"], ["marginBottom", 8]]))], ["input", _pS(_uM([["height", 44], ["borderTopWidth", 1], ["borderRightWidth", 1], ["borderBottomWidth", 1], ["borderLeftWidth", 1], ["borderTopStyle", "solid"], ["borderRightStyle", "solid"], ["borderBottomStyle", "solid"], ["borderLeftStyle", "solid"], ["borderTopColor", "#dddddd"], ["borderRightColor", "#dddddd"], ["borderBottomColor", "#dddddd"], ["borderLeftColor", "#dddddd"], ["borderTopLeftRadius", 6], ["borderTopRightRadius", 6], ["borderBottomRightRadius", 6], ["borderBottomLeftRadius", 6], ["paddingTop", 0], ["paddingRight", 12], ["paddingBottom", 0], ["paddingLeft", 12], ["fontSize", 14], ["marginBottom", 15]]))], ["btn-primary", _pS(_uM([["backgroundColor", "#0066ff"], ["color", "#ffffff"], ["height", 44], ["borderTopLeftRadius", 6], ["borderTopRightRadius", 6], ["borderBottomRightRadius", 6], ["borderBottomLeftRadius", 6], ["fontSize", 16], ["marginBottom", 10]]))], ["btn-secondary", _pS(_uM([["backgroundColor", "#f0f0f0"], ["color", "#333333"], ["height", 44], ["borderTopLeftRadius", 6], ["borderTopRightRadius", 6], ["borderBottomRightRadius", 6], ["borderBottomLeftRadius", 6], ["fontSize", 16], ["marginBottom", 10]]))], ["btn-clear", _pS(_uM([["backgroundColor", "#ff4444"], ["color", "#ffffff"], ["height", 36], ["borderTopLeftRadius", 6], ["borderTopRightRadius", 6], ["borderBottomRightRadius", 6], ["borderBottomLeftRadius", 6], ["fontSize", 14], ["marginTop", 10]]))], ["status", _pS(_uM([["backgroundColor", "#ffffff"], ["paddingTop", 15], ["paddingRight", 15], ["paddingBottom", 15], ["paddingLeft", 15], ["borderTopLeftRadius", 8], ["borderTopRightRadius", 8], ["borderBottomRightRadius", 8], ["borderBottomLeftRadius", 8], ["marginBottom", 15]]))], ["status-text", _pS(_uM([["fontSize", 14], ["color", "#333333"]]))], ["logs", _pS(_uM([["flexGrow", 1], ["flexShrink", 1], ["flexBasis", "0%"], ["flexDirection", "column"], ["backgroundColor", "#ffffff"], ["paddingTop", 15], ["paddingRight", 15], ["paddingBottom", 15], ["paddingLeft", 15], ["borderTopLeftRadius", 8], ["borderTopRightRadius", 8], ["borderBottomRightRadius", 8], ["borderBottomLeftRadius", 8]]))], ["logs-title", _pS(_uM([["fontSize", 14], ["fontWeight", "bold"], ["marginBottom", 10], ["color", "#333333"]]))], ["log-scroll", _pS(_uM([["flexGrow", 1], ["flexShrink", 1], ["flexBasis", "0%"], ["height", 300], ["backgroundColor", "#1e1e1e"], ["borderTopLeftRadius", 6], ["borderTopRightRadius", 6], ["borderBottomRightRadius", 6], ["borderBottomLeftRadius", 6], ["paddingTop", 10], ["paddingRight", 10], ["paddingBottom", 10], ["paddingLeft", 10]]))], ["log-item", _pS(_uM([["fontSize", 12], ["color", "#4caf50"], ["lineHeight", "20px"], ["marginBottom", 4]]))]])]
