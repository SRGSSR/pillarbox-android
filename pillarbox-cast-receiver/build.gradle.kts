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
    implementation(project(":pillarbox-cast"))
    api(project(":pillarbox-player"))

    implementation(libs.androidx.annotation)
    api(libs.androidx.media3.common)
    implementation(libs.androidx.media3.session)
    implementation(libs.play.services.cast)
    api(libs.play.services.cast.tv)

    testImplementation(libs.androidx.media3.cast)
    testImplementation(libs.androidx.media3.exoplayer)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.robolectric)
}
