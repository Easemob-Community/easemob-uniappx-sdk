@file:Suppress("UNCHECKED_CAST", "USELESS_CAST", "INAPPLICABLE_JVM_NAME", "UNUSED_ANONYMOUS_PARAMETER", "SENSELESS_COMPARISON", "NAME_SHADOWING", "UNNECESSARY_NOT_NULL_ASSERTION")
package uts.sdk.modules.easemobUtsSdk
import com.hyphenate.chat.EMClient
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
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
interface EMInitConfig {
    var appKey: String
    var autoLogin: Boolean?
}
fun initEMClient(appkey: String): Unit {}
fun initSDK(config: EMInitConfig): UTSPromise<Unit> {
    initEMClient(config.appKey)
    return UTSPromise.resolve()
}
val EMClient__1: UTSJSONObject = _uO("init" to fun(config: EMInitConfig): UTSPromise<Unit> {
    return initSDK(config)
}
)
val EMClient = EMClient__1
