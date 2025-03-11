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
    api(project(":pillarbox-cast"))
    implementation(project(":pillarbox-core-business"))
    api(project(":pillarbox-player"))
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.media3.cast)
    api(libs.androidx.media3.common)

    testRuntimeOnly(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.robolectric)
    testRuntimeOnly(libs.robolectric.shadows.framework)
}
