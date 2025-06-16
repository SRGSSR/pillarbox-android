/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    alias(libs.plugins.pillarbox.android.application)
    alias(libs.plugins.kotlin.serialization)
}

android {
    val versionDimension = "version"
    flavorDimensions += versionDimension
    productFlavors {
        register("prod") {
            dimension = versionDimension
        }

        if (System.getenv("CI") == "true") {
            register("nightly") {
                dimension = versionDimension
                applicationIdSuffix = ".nightly"
                versionNameSuffix = "-nightly"
            }
        }
    }

    buildFeatures {
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

dependencies {
    implementation(project(":pillarbox-core-business"))
    implementation(project(":pillarbox-demo-shared"))
    implementation(project(":pillarbox-player"))
    implementation(project(":pillarbox-ui"))

    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.runtime.saveable)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.core)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)
    implementation(libs.androidx.tv.material)
    implementation(libs.coil)
    implementation(libs.coil.compose)
    implementation(libs.coil.core)
    implementation(libs.coil.network.cache.control)
    implementation(libs.coil.network.core)
    implementation(libs.coil.network.okhttp)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.kotlinx.datetime)
    implementation(libs.kotlinx.serialization.core)
    implementation(libs.okhttp)
    implementation(libs.srg.data)
    implementation(libs.srg.dataprovider.retrofit)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
