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
    implementation(Dependencies.AndroidX.core)


    implementation("androidx.lifecycle:lifecycle-viewmodel-compose:2.6.1")

    implementation(platform(Dependencies.Compose.bom))
    implementation(Dependencies.Compose.material)
    implementation(Dependencies.Compose.materialIconsExtended)
    implementation(Dependencies.Compose.ui)
    implementation(Dependencies.Compose.uiToolingPreview)

    implementation(Dependencies.Compose.activity)
    implementation(Dependencies.Compose.navigation)
    implementation("com.google.accompanist:accompanist-systemuicontroller:0.33.0-alpha")
    implementation("com.google.accompanist:accompanist-navigation-material:0.33.0-alpha")

    // Integration layer
    val dataProviderVersion = "0.4.0"
    implementation("ch.srg.data.provider:data:$dataProviderVersion")
    implementation("ch.srg.data.provider:dataprovider-retrofit:$dataProviderVersion")
    implementation("ch.srg.data.provider:dataprovider-paging:$dataProviderVersion")
    implementation("androidx.paging:paging-compose:3.2.0-rc01")

    androidTestImplementation(platform(Dependencies.Compose.bom))
    androidTestImplementation(Dependencies.Compose.uiTestJunit4)
    testImplementation(Dependencies.Test.junit)
    androidTestImplementation(Dependencies.Test.androidJunit)
    androidTestImplementation(Dependencies.Test.espressoCore)

    debugImplementation(Dependencies.Compose.uiTooling)
    debugImplementation(Dependencies.Compose.uiTestManifest)
}
