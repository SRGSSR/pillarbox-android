/*
 * Copyright (c) 2022-2022.  SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    id("com.android.library")
    id("org.jetbrains.kotlin.android")
}

android {
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFile("consumer-rules.pro")
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
    lint {
        //https://developer.android.com/reference/tools/gradle-api/4.1/com/android/build/api/dsl/LintOptions
        abortOnError = false
    }
}

dependencies {
    implementation("androidx.core:core-ktx:${Dependancies.ktxVersion}")
    implementation("androidx.appcompat:appcompat:${Dependancies.appCompatVersion}")
    implementation("com.google.android.material:material:${Dependancies.materialVersion}")
    testImplementation("junit:junit:${Dependancies.testVersion}")
    androidTestImplementation("androidx.test.ext:junit:${Dependancies.androidTestVersion}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Dependancies.experessVersion}")
}