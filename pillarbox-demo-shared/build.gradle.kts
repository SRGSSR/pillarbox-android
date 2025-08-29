/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.compose)
    alias(libs.plugins.kotlin.serialization)
}

android {
    compileOptions {
        isCoreLibraryDesugaringEnabled = true
    }
}

dependencies {
    api(project(":pillarbox-core-business"))
    api(project(":pillarbox-player"))
    api(project(":pillarbox-ui"))

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
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.ktx)
    api(libs.androidx.datastore.core)
    api(libs.androidx.datastore.preferences)
    api(libs.androidx.datastore.preferences.core)
    api(libs.androidx.lifecycle.viewmodel)
    api(libs.androidx.media3.common)
    api(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.navigation.common)
    api(libs.androidx.navigation.runtime)
    implementation(libs.androidx.paging.common)
    coreLibraryDesugaring(libs.desugar.jdk.libs)
    api(libs.kotlinx.coroutines.core)
    api(libs.kotlinx.datetime)
    api(libs.kotlinx.serialization.core)
    implementation(libs.okhttp)
    api(libs.srg.data)
    api(libs.srg.dataprovider.paging)
    api(libs.srg.dataprovider.retrofit)

    debugImplementation(libs.androidx.compose.ui.tooling)

    testCompileOnly(libs.junit)
    testImplementation(libs.kotlin.test)
}
