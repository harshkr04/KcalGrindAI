# ProGuard / R8 Rules for KcalGrind Release Build

# Room Database & Entities
-keep class androidx.room.** { *; }
-keep class * extends androidx.room.RoomDatabase
-dontwarn androidx.room.paging.**
-keep class com.kcalgrindai.app.data.local.entity.** { *; }
-keep class com.kcalgrindai.app.data.local.dao.** { *; }
-keep class com.kcalgrindai.app.data.local.model.** { *; }
-keep class com.kcalgrindai.app.data.local.converter.** { *; }

# Kotlinx Serialization
-keepattributes *Annotation*, InnerClasses
-dontnote kotlinx.serialization.SerializationKt
-keepclassmembers class * {
    *** Companion;
}
-keepclasseswithmembers class * {
    kotlinx.serialization.KSerializer serializer();
}
-keepclassmembers class * implements kotlinx.serialization.internal.GeneratedSerializer {
    <fields>;
    <init>(...);
}
-keepclassmembers @kotlinx.serialization.Serializable class * {
    *** Companion;
    <fields>;
    <init>(...);
}
-keep @kotlinx.serialization.Serializable class com.kcalgrindai.app.data.remote.dto.** { *; }
-keep class com.kcalgrindai.app.domain.model.** { *; }

# Retrofit & OkHttp
-dontwarn okio.**
-dontwarn retrofit2.**
-keep class retrofit2.** { *; }
-keepclasseswithmembers class * {
    @retrofit2.http.* <methods>;
}
-keep interface com.kcalgrindai.app.data.remote.api.** { *; }

# WorkManager
-keep class * extends androidx.work.Worker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.CoroutineWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class * extends androidx.work.ListenableWorker {
    <init>(android.content.Context, androidx.work.WorkerParameters);
}
-keep class com.kcalgrindai.app.core.worker.** { *; }

# Hilt & Dependency Injection
-dontwarn com.google.errorprone.annotations.**
-keep class * extends androidx.lifecycle.ViewModel {
    <init>(...);
}
-keep class * implements dagger.hilt.internal.GeneratedComponent { *; }
-keep @dagger.hilt.EntryPoint class * { *; }
-keep interface * extends dagger.hilt.EntryPoint { *; }
-keep interface com.kcalgrindai.app.core.notification.ReminderWorkerEntryPoint { *; }

# Glance App Widgets
-keep class * extends androidx.glance.appwidget.GlanceAppWidget { *; }
-keep class * extends androidx.glance.appwidget.GlanceAppWidgetReceiver { *; }
-keep class com.kcalgrindai.app.feature.widget.** { *; }

# Firebase & Crashlytics
-keepattributes *Annotation*,SourceFile,LineNumberTable
-keep public class * extends java.lang.Exception
-keep class com.google.firebase.** { *; }
-dontwarn com.google.firebase.**
