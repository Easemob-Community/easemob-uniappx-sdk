
const __sfc__ = defineComponent({
  __name: 'index',
  setup(__props) {
const __ins = getCurrentInstance()!;
const _ctx = __ins.proxy as InstanceType<typeof __sfc__>;
const _cache = __ins.renderCache;

// 当前页面不引入任何 SDK 相关代码
// 初始化逻辑统一在 App.uvue 中处理

return (): any | null => {

  return _cE("view", _uM({ class: "content" }), [
    _cE("text", _uM({ class: "title" }), "环信 SDK Demo"),
    _cE("text", _uM({ class: "desc" }), "SDK 初始化请在 App.uvue 中查看")
  ])
}
}

})
export default __sfc__
const GenPagesIndexIndexStyles = [_uM([["content", _pS(_uM([["flexDirection", "column"], ["alignItems", "center"], ["justifyContent", "center"], ["paddingTop", 20], ["paddingRight", 20], ["paddingBottom", 20], ["paddingLeft", 20]]))], ["title", _pS(_uM([["fontSize", 20], ["fontWeight", "bold"], ["marginBottom", 10]]))], ["desc", _pS(_uM([["fontSize", 14], ["color", "#666666"]]))]])]
