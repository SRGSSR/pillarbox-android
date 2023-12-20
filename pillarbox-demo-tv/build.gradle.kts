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
    implementation(project(":pillarbox-analytics"))
    implementation(project(":pillarbox-ui"))
    implementation(project(":pillarbox-demo-shared"))
    implementation(libs.androidx.ktx)
    implementation(libs.coil)
    implementation(libs.leanback)
    implementation(libs.srg.dataprovider.retrofit)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.paging.compose)

    // Compose for TV dependencies
    implementation(libs.androidx.tv.foundation)
    implementation(libs.androidx.tv.material)
    implementation(libs.androidx.media3.ui.leanback)
}
