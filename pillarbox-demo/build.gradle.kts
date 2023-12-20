/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
}

android {
    compileSdk = AppConfig.compileSdk
    defaultConfig {
        applicationId = "ch.srgssr.pillarbox.demo"
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = VersionConfig.versionCode()
        versionName = VersionConfig.versionName()
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
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
            signingConfig = signingConfigs.getByName("release")
            isMinifyEnabled = false
            isDebuggable = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        val versionDimension = "version"
        flavorDimensions += versionDimension
        productFlavors {
            create("prod") {

                dimension = versionDimension
            }
            create("nightly") {
                dimension = versionDimension
                applicationIdSuffix = ".nightly"
                versionNameSuffix = "-nightly"
            }
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

    composeOptions {
        kotlinCompilerExtensionVersion = AppConfig.composeCompiler
    }
    lint {
        // https://developer.android.com/reference/tools/gradle-api/8.1/com/android/build/api/dsl/Lint
        abortOnError = true
        checkAllWarnings = true
        checkDependencies = true
        sarifReport = true
        sarifOutput = file("${rootProject.rootDir}/build/reports/android-lint/pillarbox-demo.sarif")
        disable.add("LogConditional")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    namespace = "ch.srgssr.pillarbox.demo"

    // Hide nightly flavors from BuildVariants in AndroidStudio
    androidComponents {
        beforeVariants { variant ->
            variant.enable = VersionConfig.isCI || variant.flavorName != "nightly"
        }
    }
}

dependencies {
    implementation(project(":pillarbox-core-business"))
    implementation(project(":pillarbox-analytics"))
    implementation(project(":pillarbox-ui"))
    implementation(project(":pillarbox-demo-shared"))
    implementation(libs.androidx.ktx)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.accompanist.navigation.material)

    // Integration layer
    implementation(libs.srg.data)
    implementation(libs.srg.dataprovider.retrofit)
    implementation(libs.srg.dataprovider.paging)
    implementation(libs.androidx.paging.compose)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
