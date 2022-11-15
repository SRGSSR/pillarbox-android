/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "ch.srg.pillarbox.ui"
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        version = VersionConfig.getLibraryVersionNameFromProject(project)
        group = VersionConfig.GROUP

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFile("consumer-rules.pro")
    }

    buildFeatures {
        compose = true
    }
    // https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    composeOptions {
        kotlinCompilerExtensionVersion = "1.3.2"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    lint {
        // https://developer.android.com/reference/tools/gradle-api/4.1/com/android/build/api/dsl/LintOptions
        abortOnError = false
    }
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    api(project(mapOf("path" to ":pillarbox-player")))
    implementation("androidx.core:core-ktx:${Dependencies.ktxVersion}")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:${Dependencies.coroutinesVersion}")
    implementation("androidx.compose.material:material:${Dependencies.Compose.materialVersion}")
    implementation("androidx.compose.ui:ui:${Dependencies.Compose.uiVersion}")
    implementation("androidx.compose.ui:ui-tooling-preview:${Dependencies.Compose.version}")
    api("androidx.media3:media3-ui:${Dependencies.media3Version}")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${Dependencies.Compose.version}")
    debugImplementation("androidx.compose.ui:ui-tooling:${Dependencies.Compose.version}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${Dependencies.Compose.version}")
    testImplementation("junit:junit:${Dependencies.testVersion}")
    androidTestImplementation("androidx.test.ext:junit:${Dependencies.androidTestVersion}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Dependencies.espressoVersion}")
}
