/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.lint)
}

dependencies {
    api(libs.androidx.media3.common)
    compileOnly(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.test.utils)
    implementation(libs.androidx.media3.test.utils.robolectric)
    implementation(libs.guava)
    runtimeOnly(libs.kotlinx.coroutines.android)
    compileOnly(libs.kotlinx.coroutines.core)
}
