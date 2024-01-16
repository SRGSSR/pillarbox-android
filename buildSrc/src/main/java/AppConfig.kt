/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

import org.gradle.api.JavaVersion
import java.util.Date

object AppConfig {
    const val minSdk = 21
    const val targetSdk = 34
    const val compileSdk = 34
    val javaVersion = JavaVersion.VERSION_17

    // https://developer.android.com/jetpack/androidx/releases/compose-kotlin
    const val composeCompiler = "1.5.8"

    @Suppress("SimpleDateFormat")
    fun getBuildDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
    }
}
