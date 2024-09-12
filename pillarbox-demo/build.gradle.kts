/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
plugins {
    alias(libs.plugins.pillarbox.android.application)
}

android {
    val versionDimension = "version"
    flavorDimensions += versionDimension
    productFlavors {
        create("prod") {
            dimension = versionDimension
        }

        create("nightly") {
            dimension = versionDimension
            applicationIdSuffix = ".nightly"
            versionNameSuffix = "-nightly"
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

    // Hide nightly flavors from BuildVariants in AndroidStudio
    androidComponents {
        beforeVariants { variant ->
            val isCI = System.getenv("CI")?.toBooleanStrictOrNull() ?: false

            variant.enable = isCI || variant.flavorName != "nightly"
        }
    }
}

dependencies {
    implementation(project(":pillarbox-analytics"))
    implementation(project(":pillarbox-core-business"))
    implementation(project(":pillarbox-demo-shared"))
    implementation(project(":pillarbox-player"))
    implementation(project(":pillarbox-ui"))

    implementation(libs.androidx.activity)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.compose.animation)
    implementation(libs.androidx.compose.animation.core)
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.foundation)
    implementation(libs.androidx.compose.foundation.layout)
    implementation(libs.androidx.compose.material.icons.core)
    implementation(libs.androidx.compose.material.icons.extended)
    implementation(libs.androidx.compose.material.navigation)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material3.window.size)
    implementation(libs.androidx.compose.runtime)
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.geometry)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.text)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.ui.unit)
    implementation(libs.androidx.core)
    implementation(libs.androidx.lifecycle.common)
    implementation(libs.androidx.lifecycle.runtime)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.media3.common)
    implementation(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.session)
    implementation(libs.androidx.media3.ui)
    implementation(libs.androidx.navigation.common)
    implementation(libs.androidx.navigation.compose)
    implementation(libs.androidx.navigation.runtime)
    implementation(libs.androidx.paging.common)
    implementation(libs.androidx.paging.compose)
    implementation(libs.coil)
    implementation(libs.coil.base)
    implementation(libs.guava)
    implementation(libs.kotlin.stdlib)
    implementation(libs.kotlinx.coroutines.core)
    implementation(libs.okhttp)
    implementation(libs.srg.data)
    implementation(libs.srg.dataprovider.retrofit)

    debugImplementation(libs.androidx.compose.ui.tooling)
}
