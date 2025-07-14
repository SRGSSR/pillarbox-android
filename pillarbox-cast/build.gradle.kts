/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.publishing)
    alias(libs.plugins.pillarbox.android.library.tested.module)
}

dependencies {
    api(project(":pillarbox-player"))
    implementation(libs.androidx.annotation)
    api(libs.androidx.media3.cast)
    api(libs.androidx.media3.common)
    api(libs.androidx.media3.exoplayer)
    implementation(libs.guava)
    api(libs.kotlinx.coroutines.core)

    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.core)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.dsl)
    testRuntimeOnly(libs.robolectric)
    testImplementation(libs.turbine)
}
