/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

import ch.srgssr.pillarbox.gradle.PillarboxPublishingPlugin
import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlinx.kover)
}

apply<PillarboxPublishingPlugin>()

android {
    namespace = "ch.srgssr.pillarbox.analytics"
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk

        buildConfigField("String", "BUILD_DATE", "\"${AppConfig.getBuildDate()}\"")
        buildConfigField("String", "VERSION_NAME", "\"${version}\"")

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = AppConfig.javaVersion
        targetCompatibility = AppConfig.javaVersion
    }
    kotlinOptions {
        jvmTarget = AppConfig.javaVersion.majorVersion
    }
    buildFeatures {
        buildConfig = true
        resValues = false
    }
    testOptions {
        unitTests {
            isIncludeAndroidResources = true
        }
    }
}

tasks.withType<Test>().configureEach {
    testLogging.exceptionFormat = TestExceptionFormat.FULL
}

dependencies {
    implementation(libs.comscore)
    implementation(libs.tagcommander.core)
    api(libs.tagcommander.serverside)

    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.json) {
        because("The 'org.json' package is included in the Android SDK. Adding this dependency allows us to not mock the Android SDK in unit tests.")
    }
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.dsl)
    testRuntimeOnly(libs.robolectric)
    testImplementation(libs.robolectric.annotations)
    testImplementation(libs.robolectric.shadows.framework)
}

koverReport {
    androidReports("debug") {
        xml {
            title.set(project.path)
        }
    }
}
