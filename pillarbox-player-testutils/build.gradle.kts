/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
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
        sourceCompatibility = AppConfig.javaVersion
        targetCompatibility = AppConfig.javaVersion
    }
    lint {
        // https://developer.android.com/reference/tools/gradle-api/8.1/com/android/build/api/dsl/Lint
        abortOnError = true
        checkAllWarnings = true
        checkDependencies = true
        sarifReport = true
        sarifOutput = file("${rootProject.rootDir}/build/reports/android-lint/pillarbox-player-testutils.sarif")
    }
    kotlinOptions {
        jvmTarget = AppConfig.javaVersion.majorVersion
    }
    buildFeatures {
        resValues = false
    }
}

dependencies {
    api(libs.androidx.media3.common)
    compileOnly(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.test.utils.robolectric)
    implementation(libs.guava)
    runtimeOnly(libs.kotlinx.coroutines.android)
    compileOnly(libs.kotlinx.coroutines.core)
}
