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
    api(project(":pillarbox-core-business"))
    api(project(":pillarbox-cast"))
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testRuntimeOnly(libs.robolectric)
    testImplementation(libs.robolectric.shadows.framework)
}
