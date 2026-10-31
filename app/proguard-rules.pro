# SoulSpace Proguard Rules
-keepattributes *Annotation*
-keepclassmembers class * {
    @androidx.room.* *;
}
-dontwarn androidx.media3.**

