/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

import java.text.SimpleDateFormat
import java.util.Date

plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.publishing)
    alias(libs.plugins.pillarbox.android.library.tested.module)
}

android {
    defaultConfig {
        val buildDate = SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(Date())

        buildConfigField("String", "BUILD_DATE", "\"$buildDate\"")
        buildConfigField("String", "VERSION_NAME", "\"${version}\"")
    }

    buildFeatures {
        buildConfig = true
    }
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
