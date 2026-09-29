# Android Release Signing & Bundle Build Guide

This document explains how to generate a production signing key, configure secure local credentials, and build an official Android App Bundle (`.aab`) for Google Play Store release.

---

## 1. Generate Production Keystore

Run the following command using JDK `keytool` to generate a secure RSA 4096-bit key:

```bash
keytool -genkey -v \
  -keystore kcalgrindai-release-key.jks \
  -alias kcalgrindai-release-alias \
  -keyalg RSA \
  -keysize 4096 \
  -validity 10000
```

Store this `.jks` file in a secure backup location (e.g. 1Password / Google Secret Manager). **Never lose this file or password**, as Google Play requires it for update verification.

---

## 2. Configure `keystore.properties`

1. Copy the template from `keystore.properties.example`:
   ```bash
   cp keystore.properties.example keystore.properties
   ```
2. Populate the real values:
   ```properties
   storeFile=/absolute/path/to/kcalgrindai-release-key.jks
   storePassword=YourKeystorePassword
   keyAlias=kcalgrindai-release-alias
   keyPassword=YourKeyPassword
   ```

---

## 3. Gradle Signing Configuration

In `app/build.gradle.kts`, the release signing config is configured to dynamically read `keystore.properties` if present:

```kotlin
val keystorePropertiesFile = rootProject.file("keystore.properties")
val keystoreProperties = Properties()
if (keystorePropertiesFile.exists()) {
    keystoreProperties.load(FileInputStream(keystorePropertiesFile))
}

android {
    signingConfigs {
        create("release") {
            if (keystorePropertiesFile.exists()) {
                storeFile = file(keystoreProperties.getProperty("storeFile"))
                storePassword = keystoreProperties.getProperty("storePassword")
                keyAlias = keystoreProperties.getProperty("keyAlias")
                keyPassword = keystoreProperties.getProperty("keyPassword")
            }
        }
    }

    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
            signingConfig = signingConfigs.getByName("release")
        }
    }
}
```

---

## 4. Build Production Artifacts

To generate the signed release Android App Bundle (`.aab`) for Google Play Console:
```bash
./gradlew bundleRelease
```
The output will be generated at:
`app/build/outputs/bundle/release/app-release.aab`

To test a signed release APK locally on an emulator or physical device:
```bash
./gradlew assembleRelease
adb install -r app/build/outputs/apk/release/app-release.apk
```
