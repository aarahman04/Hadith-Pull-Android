# kotlinx.serialization: keep serializer descriptors and generated $$serializer classes.
# https://github.com/Kotlin/kotlinx.serialization/blob/master/rules/common.pro
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.AnnotationsKt

-keepclasseswithmembers class online.hadithpull.app.**$$serializer {
    static **[] $childSerializers;
    public static ** INSTANCE;
}
-keepclassmembers class online.hadithpull.app.** {
    *** Companion;
}
-keepclasseswithmembers class online.hadithpull.app.** {
    kotlinx.serialization.KSerializer serializer(...);
}
