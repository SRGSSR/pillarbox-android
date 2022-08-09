import java.util.*

/*
 * Copyright (c) 2022.  SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
object AppConfig {
    const val minSdk = 21
    const val targetSdk = 32
    const val compileSdk = 32

    @Suppress("SimpleDateFormat")
    fun getBuildDate(): String {
        val sdf = java.text.SimpleDateFormat("yyyy-MM-dd HH:mm:ss")
        return sdf.format(Date())
    }
}