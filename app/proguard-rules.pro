# Firebase
-keep class com.google.firebase.** { *; }
-keep class com.foyer.gestion.data.model.** { *; }

# Hilt
-keep class dagger.hilt.** { *; }
-keepnames class * extends androidx.lifecycle.ViewModel

# Kotlin serialization
-keepattributes *Annotation*
-keepattributes SourceFile,LineNumberTable
