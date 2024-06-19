/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

import java.io.ByteArrayOutputStream

plugins {
    alias(libs.plugins.pillarbox.android.library)
    alias(libs.plugins.pillarbox.android.library.publishing)
    alias(libs.plugins.pillarbox.android.library.tested.module)
    alias(libs.plugins.kotlin.parcelize)
}

android {
    buildFeatures {
        buildConfig = true
    }

    buildTypes {
        debug {
            buildConfigField("String", "VERSION_NAME", "\"debug\"")
        }

        release {
            val latestTagHash = execCommand("git", "rev-list", "--tags", "--max-count=1")
            val versionName = execCommand("git", "describe", "--tags", "$latestTagHash")

            buildConfigField("String", "VERSION_NAME", "\"${versionName}\"")
        }
    }

    // Mockk includes some licenses information, which may conflict with other license files. This block merges all licenses together.
    // Mockk excludes all licenses instead:
    // https://github.com/mockk/mockk/blob/f879502a044c83c2a5fd52992f20903209eb34f3/modules/mockk-android/build.gradle.kts#L14-L19
    packaging {
        resources {
            merges += "META-INF/LICENSE.md"
            merges += "META-INF/LICENSE-notice.md"
        }
    }
}

dependencies {
    implementation(libs.androidx.annotation)
    implementation(libs.androidx.core)
    api(libs.androidx.media)
    api(libs.androidx.media3.common)
    implementation(libs.androidx.media3.dash)
    implementation(libs.androidx.media3.datasource)
    api(libs.androidx.media3.exoplayer)
    implementation(libs.androidx.media3.hls)
    api(libs.androidx.media3.session)
    api(libs.androidx.media3.ui)
    api(libs.guava)
    implementation(libs.kotlin.parcelize.runtime)
    implementation(libs.kotlinx.coroutines.guava)
    runtimeOnly(libs.kotlinx.coroutines.android)
    api(libs.kotlinx.coroutines.core)

    testImplementation(project(":pillarbox-player-testutils"))

    testImplementation(libs.androidx.media3.test.utils)
    testImplementation(libs.androidx.media3.test.utils.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.test.ext.junit)
    testImplementation(libs.junit)
    testImplementation(libs.kotlin.test)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.mockk)
    testImplementation(libs.mockk.dsl)
    testImplementation(libs.robolectric)
    testImplementation(libs.robolectric.annotations)
    testImplementation(libs.robolectric.shadows.framework)
    testImplementation(libs.turbine)

    androidTestImplementation(libs.androidx.test.monitor)
    androidTestRuntimeOnly(libs.androidx.test.runner)
    androidTestImplementation(libs.junit)
    androidTestRuntimeOnly(libs.kotlinx.coroutines.android)
    androidTestImplementation(libs.mockk)
}

private fun execCommand(vararg commandSegments: String): String {
    return ByteArrayOutputStream().use { stream ->
        exec {
            commandLine = commandSegments.toList()
            standardOutput = stream
        }
        stream.toString(Charsets.UTF_8).trim()
    }
}
