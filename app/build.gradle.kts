plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.salyan.filemanager"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.salyan.filemanager"
        minSdk = 26     // Android 8.0 (API 26) para bom suporte ARMv7
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables.useSupportLibrary = true

        // Ajuda em dispositivos antigos/32-bit
        ndk {
            abiFilters.addAll(listOf("armeabi-v7a", "arm64-v8a"))  // Suporte ARMv7 + 64
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17   // Atualizado para 17 (compatível com JDK 17)
        targetCompatibility = JavaVersion.VERSION_17
    }

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = "1.5.10"  // Versão estável para Kotlin 1.9.x; ajuste se usar Kotlin 2.0+
    }
}

dependencies {
    // ... mantenha as mesmas, mas adicione se necessário:
    implementation("androidx.core:core-ktx:1.13.1")  // Atualize para latest compatível
    // ... resto igual
}
