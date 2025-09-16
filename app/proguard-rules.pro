# Базовые правила ProGuard для Kotlin и Android
-keep class androidx.security.crypto.** { *; }
-keep class com.google.crypto.tink.** { *; }

# Сохраняем классы, которые используются через reflection
-keep class com.company.secureapp.** { *; }

# Сохраняем BuildConfig класс
-keep class com.company.secureapp.BuildConfig { *; }

# Сохраняем ViewBinding
-keep class * implements androidx.viewbinding.ViewBinding {
    public static * bind(android.view.View);
    public static * inflate(android.view.LayoutInflater);
}

# OkHttp
-keep class okhttp3.** { *; }
-keep interface okhttp3.** { *; }
-dontwarn okhttp3.**

# Android Support Library
-keep public class * extends android.app.Activity
-keep public class * extends android.app.Application
-keep public class * extends android.app.Service
-keep public class * extends android.content.BroadcastReceiver
-keep public class * extends android.content.ContentProvider
-keep public class * extends android.view.View
-keep public class * extends android.app.Fragment

# Kotlin
-keep class kotlin.** { *; }
-keep class kotlinx.** { *; }
-dontwarn kotlin.**
-keep class org.jetbrains.** { *; }
-dontwarn org.jetbrains.**

# Для корректной работы runtime permissions
-keep class androidx.activity.result.** { *; }
-keep class androidx.fragment.app.Fragment { *; }
