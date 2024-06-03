/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.gradle.internal

internal object AppConfig {
    internal const val minSdk = 21
    internal const val targetSdk = 34
    internal const val compileSdk = 34

    // When changing this value, don't forget to also update the Detekt config in the root `build.gradle.kts` file
    internal val javaVersion = 17
}
