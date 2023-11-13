/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.content.Context
import android.content.res.Configuration

/**
 * Vector
 */
object Vector {
    /**
     * TV vector
     */
    const val TV = "TVPLAY"

    /**
     * Mobile vector
     */
    const val MOBILE = "APPPLAY"

    /**
     * Get vector
     *
     * @return vector for MediaCompositionService.
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
