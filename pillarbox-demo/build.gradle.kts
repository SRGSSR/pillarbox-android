/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.firebase.appdistribution").version("3.0.3").apply(true)
    id("androidx.navigation.safeargs.kotlin")
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
        // Use RTS keystore signing config
        create("release") {
            try {
                storeFile = property("ch.srg.srgplayer.rts.keystore")?.let { file(it as String) }
                storePassword = property("ch.srg.srgplayer.rts.password") as String?
                keyAlias = property("ch.srg.srgplayer.rts.alias") as String?
                keyPassword = property("ch.srg.srgplayer.rts.password") as String?
            } catch (e: groovy.lang.MissingPropertyException) {

            }
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
            // https://firebase.google.com/docs/app-distribution/android/distribute-gradle?apptype=apk
            firebaseAppDistribution {
                artifactType = "APK"
                releaseNotesFile = "pillarbox-demo/last_release_notes.txt"
                // Other information are passed by the CI or threw Command line options
            }
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

    implementation("androidx.compose.material:material:1.2.1")
    // or only import the main APIs for the underlying toolkit systems,
    // such as input and measurement/layout
    implementation("androidx.compose.ui:ui:1.2.1")
    // Optional - Integration with activities
    implementation("androidx.activity:activity-compose:1.6.0")
    // Optional - Integration with ViewModels
    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:${Dependencies.lifecycleVersion}")
    implementation("androidx.compose.ui:ui-tooling-preview:${rootProject.extra["compose_version"]}")

    androidTestImplementation("androidx.compose.ui:ui-test-junit4:${rootProject.extra["compose_version"]}")
    debugImplementation("androidx.compose.ui:ui-tooling:${rootProject.extra["compose_version"]}")
    debugImplementation("androidx.compose.ui:ui-test-manifest:${rootProject.extra["compose_version"]}")
}
