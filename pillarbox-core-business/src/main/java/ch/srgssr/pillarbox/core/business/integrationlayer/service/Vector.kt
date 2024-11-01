/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.content.Context
import android.content.res.Configuration
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector.MOBILE
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector.TV

/**
 * Provides constants and utilities to determine the playback vector ([MOBILE] or [TV]).
 */
object Vector {
    /**
     * Constant for the TV vector.
     */
    const val TV = "TVPLAY"

    /**
     * Constant for the mobile vector.
     */
    const val MOBILE = "APPPLAY"

    /**
     * Retrieves the vector based on the device type.
     *
     * @return The vector for the current device type.
     *
     * @receiver The [Context] used to access system resources.
     */
    fun Context.getVector(): String {
        val uiMode = resources.configuration.uiMode
        return if (uiMode and Configuration.UI_MODE_TYPE_MASK == Configuration.UI_MODE_TYPE_TELEVISION) {
            TV
        } else {
            MOBILE
        }
    }
}
