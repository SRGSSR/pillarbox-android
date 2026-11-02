/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.publishing)
}

dependencies {
    api(project(":pillarbox-player"))
    implementation(libs.androidx.annotation)
    api(libs.androidx.media3.common)
    implementation(libs.androidx.media3.database)
    api(libs.androidx.media3.datasource)
    implementation(libs.androidx.media3.datasource.okhttp)
    api(libs.androidx.media3.exoplayer)
    api(libs.kotlinx.coroutines.core)
    api(libs.okhttp)
}
