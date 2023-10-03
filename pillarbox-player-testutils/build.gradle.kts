/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@Suppress("DSL_SCOPE_VIOLATION") // TODO Remove once KTIJ-19369 is fixed
plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ch.srgssr.pillarbox.player.test.utils"
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    compileOnly(libs.androidx.media3.exoplayer)
    compileOnly(libs.androidx.ktx)
    compileOnly(libs.kotlinx.coroutines.android)
}
