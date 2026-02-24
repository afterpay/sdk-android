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

# Strip all Timber logging calls from release builds
# Note: Keep these method signatures in sync with Timber APIs used by the SDK
-assumenosideeffects class timber.log.Timber {
    public *** d(...);
    public *** i(...);
    public *** w(...);
    public *** e(...);
    public *** v(...);
    public *** wtf(...);
}

# Strip all AfterpayLog logging calls from release builds
# Note: Keep sanitization helpers as they are pure utility functions
-assumenosideeffects class com.afterpay.android.internal.AfterpayLog {
    public *** d(...);
    public *** i(...);
    public *** w(...);
    public *** e(...);
    public *** apiRequest(...);
    public *** apiSuccess(...);
    public *** apiError(...);
    public *** checkoutEvent(...);
}
