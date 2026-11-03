# kotlinx.serialization: keep generated serializers for the polymorphic Exercise hierarchy.
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers class com.rihla.english.core.** {
    *** Companion;
}
-keepclasseswithmembers class com.rihla.english.core.** {
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.rihla.english.core.**$$serializer { *; }
