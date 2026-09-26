plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}
android {
    namespace = "com.noam.pokelens"
    compileSdk = 34
    defaultConfig {
        applicationId = "com.noam.pokelens"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }
    buildTypes { release { isMinifyEnabled = false } }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions { jvmTarget = "17" }
}
dependencies {
    // זיהוי טקסט אופליין (המודל נארז בתוך ה-APK, לא צריך אינטרנט)
    implementation("com.google.mlkit:text-recognition:16.0.1")
}
