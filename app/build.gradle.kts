plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.jetbrains.kotlin.android)
}

android {
    namespace = "com.uebrasil.panicbuttonapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.uebrasil.panicbuttonapp"
        minSdk = 28
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
}

dependencies {
    // AndroidX e Material
    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.5.1")
    implementation("com.google.android.material:material:1.7.0")

    // Compose
    implementation("androidx.compose.ui:ui:1.3.0")
    implementation("androidx.compose.material:material:1.3.0")
    implementation("androidx.compose.ui:ui-tooling-preview:1.3.0")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.5.1")

    // Play Services e Localização
    implementation("com.google.android.gms:play-services-location:21.0.1")

    // Socket.IO
    implementation("io.socket:socket.io-client:2.0.0") // ou a versão mais recente

    // JSON
    implementation("org.json:json:20210307")

    // OkHttp para networking
    implementation("com.squareup.okhttp3:okhttp:4.9.3")
    implementation(libs.androidx.core)

    // Testes
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}
