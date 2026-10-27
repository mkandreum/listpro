# kotlinx.serialization: conservar los serializadores generados de los modelos
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.**
-keepclassmembers @kotlinx.serialization.Serializable class com.listpro.xyon.pro.** {
    *** Companion;
    kotlinx.serialization.KSerializer serializer(...);
}
-keep,includedescriptorclasses class com.listpro.xyon.pro.**$$serializer { *; }
