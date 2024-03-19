/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.publishing)
    alias(libs.plugins.pillarbox.android.library.tested.module)
    alias(libs.plugins.kotlin.parcelize)
    alias(libs.plugins.kotlin.serialization)
}

android {
    defaultConfig {
        buildConfigField("String", "VERSION_NAME", "\"${version}\"")
    }

    buildFeatures {
        buildConfig = true
    }
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core)
    api(libs.androidx.media3.common)
    implementation(libs.androidx.media3.dash)
    implementation(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.datasource.okhttp)
    api(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.hls)
    api(libs.androidx.media3.session)
    api(libs.androidx.media3.ui)
    api(libs.guava)
    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.kotlinx.coroutines.guava)
    runtimeOnly(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.serialization.core)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.ktor.client.content.negotiation)
    api(libs.ktor.client.core)
    implementation(libs.ktor.client.okhttp)
    implementation(libs.ktor.http)
    implementation(libs.ktor.serialization)
    implementation(libs.ktor.serialization.kotlinx.json)
    implementation(libs.ktor.utils)
    api(libs.okhttp)
    implementation(libs.okhttp.logging.interceptor)

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
    testImplementation(libs.robolectric)
    testImplementation(libs.robolectric.annotations)
    testImplementation(libs.robolectric.shadows.framework)
    testImplementation(libs.turbine)
}
