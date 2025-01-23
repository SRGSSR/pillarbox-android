/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.compose)
    alias(libs.plugins.pillarbox.android.library.publishing)
    alias(libs.plugins.pillarbox.android.library.tested.module)
}

dependencies {
    implementation(project(":pillarbox-player"))
    implementation(platform(libs.androidx.compose.bom))
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    api(libs.androidx.media3.cast)
    implementation(libs.guava)
}
