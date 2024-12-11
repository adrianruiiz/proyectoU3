plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.z_iti_271304_u3_e01"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.z_iti_271304_u3_e01"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
}

dependencies {

    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.activity)
    implementation(libs.constraintlayout)
    implementation(libs.filament.android)
    testImplementation(libs.junit)
    androidTestImplementation(libs.ext.junit)
    androidTestImplementation(libs.espresso.core)
    implementation ("de.javagl:obj:0.3.0")
    implementation("org.java-websocket:Java-WebSocket:1.5.2")

    // Filament dependencies (version 1.52.0)
    implementation("com.google.android.filament:filament-android:1.52.0")
    implementation("com.google.android.filament:gltfio-android:1.52.0")
    implementation("com.google.android.filament:filament-utils-android:1.52.0")
}