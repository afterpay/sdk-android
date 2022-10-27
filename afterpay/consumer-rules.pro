-keepattributes *Annotation*, InnerClasses

# kotlinx-serialization-json specific. Add this if you have java.lang.NoClassDefFoundError kotlinx.serialization.json.JsonObjectSerializer
-keepclassmembers class kotlinx.serialization.json.** {
    *** Companion;
}

-keepclasseswithmembers class kotlinx.serialization.json.** {
    kotlinx.serialization.KSerializer serializer(...);
}

-keep,includedescriptorclasses class com.afterpay.android.**$$serializer { *; }

-keepclassmembers class com.afterpay.android.** {
    *** Companion;
}

-keepclasseswithmembers class com.afterpay.android.** {
    kotlinx.serialization.KSerializer serializer(...);
}
