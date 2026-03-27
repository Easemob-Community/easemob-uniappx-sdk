# ProGuard rules for UniApp
-keep public class * extends android.app.Application
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Service

# 环信 SDK
-keep class com.hyphenate.** {*;}
-dontwarn  com.hyphenate.**

# UTS
-keep class io.dcloud.** {*;}
-keep class uni.** {*;}
