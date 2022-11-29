/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        applicationId = "ch.srgssr.pillarbox.demo"
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = VersionConfig.versionCode()
        versionName = VersionConfig.getVersionNameFromProject(project)
        applicationIdSuffix = if (VersionConfig.isSnapshot()) ".nightly" else null
        versionNameSuffix = if (VersionConfig.isSnapshot()) "-nightly" else null
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
    }

    signingConfigs {
        create("release") {
            storeFile = file("./demo.keystore")
            storePassword = System.getenv("DEMO_KEY_PASSWORD") ?: ""
            keyAlias = "demo"
            keyPassword = System.getenv("DEMO_KEY_PASSWORD") ?: ""
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
    buildFeatures {
        compose = true
    }
    // https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    lint {
        // https://developer.android.com/reference/tools/gradle-api/4.1/com/android/build/api/dsl/LintOptions
        abortOnError = true
        checkAllWarnings = true
        checkDependencies = false
        xmlReport = true // Enable for Danger Android Lint
        xmlOutput = file("${project.rootDir}/build/reports/android-lint.xml")
    }
    packagingOptions {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(mapOf("path" to ":pillarbox-core-business")))
    implementation(project(mapOf("path" to ":pillarbox-analytics")))
    implementation("androidx.core:core-ktx:${Dependencies.ktxVersion}")
    implementation("androidx.appcompat:appcompat:${Dependencies.appCompatVersion}")
    implementation("com.google.android.material:material:${Dependencies.materialVersion}")

    implementation("androidx.fragment:fragment-ktx:${Dependencies.fragmentVersion}")
    implementation("androidx.navigation:navigation-ui-ktx:${Dependencies.navigationVersion}")
    implementation("androidx.navigation:navigation-fragment-ktx:${Dependencies.navigationVersion}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:${Dependencies.lifecycleVersion}")
    implementation("androidx.media3:media3-ui:${Dependencies.media3Version}")
    implementation("com.google.code.gson:gson:${Dependencies.gsonVersion}")
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:${Dependencies.lifecycleVersion}")
    testImplementation("junit:junit:${Dependencies.testVersion}")
    androidTestImplementation("androidx.test.ext:junit:${Dependencies.androidTestVersion}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Dependencies.espressoVersion}")

    implementation("androidx.compose.material:material:1.3.1")
    implementation("androidx.compose.material:material-icons-extended:1.3.1")
    implementation("androidx.compose.ui:ui:1.3.1")
    implementation("androidx.activity:activity-compose:1.6.1")
    implementation("androidx.navigation:navigation-compose:${Dependencies.navigationVersion}")
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${Dependencies.lifecycleVersion}")
    implementation("androidx.compose.ui:ui-tooling-preview:${Dependencies.composeVersion}")
    implementation("com.google.accompanist:accompanist-pager:0.27.0")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Dependencies.composeVersion}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Dependencies.composeVersion}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${Dependencies.composeVersion}")
}
