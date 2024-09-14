/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle.internal

import org.gradle.api.JavaVersion
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

internal object AppConfig {
    internal const val minSdk = 21
    internal const val targetSdk = 34
    internal const val compileSdk = 34

    internal const val javaVersionName = "17"
    internal val javaVersion = JavaVersion.valueOf("VERSION_$javaVersionName")
    internal val jvmTarget = JvmTarget.fromTarget(javaVersionName)
}
