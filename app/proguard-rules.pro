# kotlinx.serialization: keep generated serializers for the polymorphic Exercise hierarchy.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.rihla.english.core.model.** {
    *** Companion;
}
-keepclasseswithmembers class com.rihla.english.core.model.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.rihla.english.core.model.**$$serializer { *; }
