/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
import java.util.*

object AppConfig {
    const val minSdk = 21
    const val targetSdk = 33
    const val compileSdk = 34

    @Suppress("SimpleDateFormat")
    fun getBuildDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
    }
}
