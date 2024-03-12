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
    namespace = "ch.srgssr.pillarbox.player"
    compileSdk = AppConfig.compileSdk

    defaultConfig {
        minSdk = AppConfig.minSdk

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

    // Mockk includes some licenses information, which may conflict with other license files. This block merges all licenses together.
    // Mockk excludes all licenses instead:
    // https://github.com/mockk/mockk/blob/f879502a044c83c2a5fd52992f20903209eb34f3/modules/mockk-android/build.gradle.kts#L14-L19
    packaging {
        resources {
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
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
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core)
    implementation(libs.androidx.media)
    api(libs.androidx.media3.common)
    implementation(libs.androidx.media3.dash)
    api(libs.androidx.media3.datasource)
    api(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.hls)
    api(libs.androidx.media3.session)
    api(libs.androidx.media3.ui)
    api(libs.guava)
    runtimeOnly(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.core)

    testImplementation(project(":pillarbox-player-testutils"))

    testImplementation(libs.androidx.media3.test.utils)
    testImplementation(libs.androidx.media3.test.utils.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.dsl)
    testRuntimeOnly(libs.robolectric)
    testImplementation(libs.robolectric.annotations)
    testImplementation(libs.robolectric.shadows.framework)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.test.ext.junit)
    androidTestImplementation(libs.androidx.test.monitor)
    androidTestRuntimeOnly(libs.androidx.test.runner)
    androidTestImplementation(libs.junit)
    androidTestRuntimeOnly(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.mockk)
    androidTestImplementation(libs.mockk.android)
}

koverReport {
    androidReports("debug") {
        xml {
            title.set(project.path)
        }
    }
}
