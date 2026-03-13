plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    //id("org.jetbrains.kotlin.kapt")
}

android {
    namespace = "com.example.whatsappcleaner"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.whatsappcleaner"
        minSdk = 26
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.0"
    }
}



dependencies {
    val composeBom = platform("androidx.compose:compose-bom:2024.04.01")
    implementation(composeBom)
    implementation("androidx.work:work-runtime-ktx:2.9.0")


    implementation("androidx.work:work-runtime-ktx:2.9.0") // For Timer
    implementation("com.airbnb.android:lottie-compose:6.3.0") // For Animations
    implementation("androidx.compose.material:material-icons-extended:1.6.0") // For Icons

    implementation("androidx.core:core-ktx:1.13.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.8.4")
    implementation("androidx.activity:activity-compose:1.9.1")

    implementation("io.coil-kt:coil-compose:2.4.0")
    implementation("io.coil-kt:coil-video:2.4.0") // <-- Add this for video thumbnails

    implementation("androidx.compose.ui:ui")
    implementation("androidx.compose.material3:material3")
    implementation("androidx.compose.ui:ui-tooling-preview")
    debugImplementation("androidx.compose.ui:ui-tooling")

    //implementation("androidx.room:room-runtime:2.6.1")
    //implementation("androidx.room:room-ktx:2.6.1")


    implementation("androidx.work:work-runtime-ktx:2.9.0")
}
