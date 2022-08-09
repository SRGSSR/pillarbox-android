/*
 * Copyright (c) 2022-2022.  SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

val version = VersionConfig.getVersionNameFromProject(project)

android {
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        applicationId = "ch.srg.pillarbox.demo"
        minSdk = AppConfig.minSdk
        targetSdk = AppConfig.targetSdk
        versionCode = 1
        versionName = version

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }
    signingConfigs {
        // Use RTS keystore signing config
        create("release") {
            storeFile = property("ch.srg.srgplayer.rts.keystore")?.let { file(it as String) }
            storePassword = property("ch.srg.srgplayer.rts.password") as String?
            keyAlias = property("ch.srg.srgplayer.rts.alias") as String?
            keyPassword = property("ch.srg.srgplayer.rts.password") as String?
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
    kotlinOptions {
        jvmTarget = "1.8"
    }
    lint {
        //https://developer.android.com/reference/tools/gradle-api/4.1/com/android/build/api/dsl/LintOptions
        abortOnError = true
        checkReleaseBuilds = true
        checkAllWarnings = true
        checkDependencies = true
        xmlReport = true //Enable for Danger Android Lint
        xmlOutput = file("${project.rootDir}/build/reports/android-lint.xml")
    }
}

dependencies {
    implementation(project(mapOf("path" to ":Player")))
    implementation("androidx.core:core-ktx:${Dependancies.ktxVersion}")
    implementation("androidx.appcompat:appcompat:${Dependancies.appCompatVersion}")
    implementation("com.google.android.material:material:${Dependancies.materialVersion}")
    testImplementation("junit:junit:${Dependancies.testVersion}")
    androidTestImplementation("androidx.test.ext:junit:${Dependancies.androidTestVersion}")
    androidTestImplementation("androidx.test.espresso:espresso-core:${Dependancies.experessVersion}")
}
