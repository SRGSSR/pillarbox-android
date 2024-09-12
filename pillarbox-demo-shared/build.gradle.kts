/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.compose)
}

dependencies {
    compileOnly(project(":pillarbox-core-business"))
    api(project(":pillarbox-player"))

    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    api(libs.androidx.compose.runtime)
    api(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.geometry)
    api(libs.androidx.compose.ui.graphics)
    api(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.unit)
    api(libs.androidx.datastore.preferences)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    api(libs.androidx.navigation.common)
    api(libs.androidx.navigation.runtime)
    implementation(libs.androidx.paging.common)
    api(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp)
    api(libs.srg.data)
    api(libs.srg.dataprovider.paging)
    api(libs.srg.dataprovider.retrofit)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testImplementation(libs.junit)
}
