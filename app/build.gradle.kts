import java.util.Properties
import java.io.FileInputStream

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
}

val localProperties = Properties().apply {
    val localPropertiesFile = rootProject.file("local.properties")
    if (localPropertiesFile.exists()) {
        load(FileInputStream(localPropertiesFile))
    }
}
val usdaApiKey = localProperties.getProperty("USDA_API_KEY") ?: "DEMO_KEY"

android {
    namespace = "com.kcalgrindai.app"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.kcalgrindai.app"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "0.1.0"
        buildConfigField("String", "USDA_API_KEY", "\"$usdaApiKey\"")
        buildConfigField("String", "BACKEND_URL", "\"${localProperties.getProperty("BACKEND_URL") ?: "http://10.0.2.2:8000/"}\"")
        buildConfigField("boolean", "USE_FIREBASE_EMULATOR", "${(localProperties.getProperty("USE_FIREBASE_EMULATOR") ?: System.getenv("USE_FIREBASE_EMULATOR") ?: "false").toBoolean()}")
    }

    buildTypes {
        getByName("debug") {
            buildConfigField("String", "BACKEND_URL", "\"${localProperties.getProperty("BACKEND_URL") ?: "http://10.0.2.2:8000/"}\"")
        }
        getByName("release") {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            val releaseBackendUrl = localProperties.getProperty("PROD_BACKEND_URL")
                ?: System.getenv("PROD_BACKEND_URL")
                ?: ""
            buildConfigField("String", "BACKEND_URL", "\"$releaseBackendUrl\"")
        }
    }

    buildFeatures {
        compose = true
        buildConfig = true
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    testOptions {
        unitTests {
            isIncludeAndroidResources = true
            isReturnDefaultValues = true
        }
    }

    sourceSets {
        getByName("test").assets.srcDirs("$projectDir/schemas")
    }
}

ksp {
    arg("room.schemaLocation", "$projectDir/schemas")
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.hilt.navigation.compose)
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    implementation(libs.retrofit)
    implementation(libs.retrofit.kotlinx.serialization)
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.coil.compose)
    implementation(libs.androidx.camera.core)
    implementation(libs.androidx.camera.camera2)
    implementation(libs.androidx.camera.lifecycle)
    implementation(libs.androidx.camera.view)
    implementation(libs.androidx.health.connect)
    implementation("com.google.guava:guava:33.3.1-android")
    implementation(libs.mlkit.barcode.scanning)
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.auth)
    implementation(libs.firebase.crashlytics)
    implementation(libs.play.services.auth)
    implementation(libs.hilt.android)
    implementation("dev.chrisbanes.haze:haze:1.1.1")
    implementation(libs.androidx.glance)
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)
    ksp(libs.hilt.compiler)
    ksp(libs.androidx.room.compiler)
    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.androidx.room.testing)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.turbine)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}

tasks.register("validateReleaseBackendUrl") {
    doLast {
        val prodUrl = localProperties.getProperty("PROD_BACKEND_URL")
            ?: System.getenv("PROD_BACKEND_URL")
            ?: ""
        if (prodUrl.isBlank() || !prodUrl.startsWith("https://")) {
            throw GradleException(
                "Release build failed: PROD_BACKEND_URL is missing or does not start with 'https://'. Provided: '$prodUrl'"
            )
        }
    }
}

tasks.matching { it.name == "preReleaseBuild" }.configureEach {
    dependsOn("validateReleaseBackendUrl")
}
