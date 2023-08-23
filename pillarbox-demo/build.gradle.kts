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
    // https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    composeOptions {
        kotlinCompilerExtensionVersion = Version.composeCompiler
    }
    lint {
        // https://developer.android.com/reference/tools/gradle-api/4.1/com/android/build/api/dsl/LintOptions
        abortOnError = true
        checkAllWarnings = true
        checkDependencies = false
        xmlReport = true // Enable for Danger Android Lint
        xmlOutput = file("${project.rootDir}/build/reports/android-lint.xml")
        disable.add("LogConditional")
    }
    packagingOptions {
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
    implementation(project(mapOf("path" to ":pillarbox-core-business")))
    implementation(project(mapOf("path" to ":pillarbox-analytics")))
    implementation(project(mapOf("path" to ":pillarbox-ui")))
    implementation(libs.androidx.ktx)

    val composeBom = libs.androidx.compose.bom
    implementation(platform(composeBom))
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)

    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")
    implementation( "androidx.activity:activity-compose:1.7.2")
    implementation( "androidx.navigation:navigation-compose:2.7.0")
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.33.0-alpha")
    implementation("com.google.accompanist:accompanist-navigation-material:0.33.0-alpha")

    // Integration layer
    val dataProviderVersion = "0.4.0"
    implementation("ch.srg.data.provider:data:$dataProviderVersion")
    implementation("ch.srg.data.provider:dataprovider-retrofit:$dataProviderVersion")
    implementation("ch.srg.data.provider:dataprovider-paging:$dataProviderVersion")
    implementation("androidx.paging:paging-compose:3.2.0-rc01")

    androidTestImplementation(platform(composeBom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.test.junit)
    androidTestImplementation(libs.androidx.espresso.core)

    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
}
