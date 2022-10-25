/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.integrationlayer.data

/**
 * Drm information
 *
 * @property type
 * @property licenseUrl
 * @property certificateUrl
 */
data class Drm(val type: Type, val licenseUrl: String, val certificateUrl: String? = null) {

    /**
     * Drm Type, on Android [FAIRPLAY] is not playable!
     */
    enum class Type {
        FAIRPLAY, WIDEVINE, PLAYREADY
    }
}
