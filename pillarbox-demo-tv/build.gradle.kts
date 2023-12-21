/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    namespace = "ch.srgssr.pillarbox.demo.tv"
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        applicationId = "ch.srgssr.pillarbox.demo.tv"
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = VersionConfig.versionCode()
        versionName = VersionConfig.versionName()
    }

    signingConfigs {
        create("release") {
            val password = System.getenv("DEMO_KEY_PASSWORD") ?: extra.properties["pillarbox.keystore.password"] as String?
            storeFile = file("./demo.keystore")
            storePassword = password
            keyAlias = "demo"
            keyPassword = password
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
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
    buildFeatures {
        compose = true
    }
    lint {
        // https://developer.android.com/reference/tools/gradle-api/8.1/com/android/build/api/dsl/Lint
        abortOnError = true
        checkAllWarnings = true
        checkDependencies = true
        sarifReport = true
        sarifOutput = file("${rootProject.rootDir}/build/reports/android-lint/pillarbox-demo-tv.sarif")
    }
    composeOptions {
        kotlinCompilerExtensionVersion = AppConfig.composeCompiler
    }
}

dependencies {
    implementation(project(":pillarbox-core-business"))
    implementation(project(":pillarbox-demo-shared"))
    implementation(project(":pillarbox-player"))
    implementation(project(":pillarbox-ui"))

    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.compose.ui.util)
    implementation(libs.androidx.fragment)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui.leanback)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.coil)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.leanback)
    implementation(libs.srg.data)
    implementation(libs.srg.dataprovider.retrofit)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
