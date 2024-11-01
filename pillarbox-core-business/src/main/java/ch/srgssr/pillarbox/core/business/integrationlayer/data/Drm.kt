/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import ch.srgssr.pillarbox.core.business.integrationlayer.data.Drm.Type
import kotlinx.serialization.Serializable

/**
 * Represents Digital Rights Management (DRM) information for a media stream.
 *
 * @property type The type of DRM used. Note that on Android [FairPlay][Type.FAIRPLAY] is not playable.
 * @property licenseUrl The URL of the license.
 * @property certificateUrl An optional URL pointing to a certificate used for DRM authentication
 */
@Serializable
data class Drm(val type: Type, val licenseUrl: String, val certificateUrl: String? = null) {
    /**
     * Represents the type of DRM used for protecting content.
     */
    enum class Type {
        /**
         * Apple's [FairPlay](https://developer.apple.com/streaming/fps/) Streaming DRM.
         *
         * This is not supported on Android.
         */
        FAIRPLAY,

        /**
         * Google's [Widevine](https://widevine.com/) Modular DRM.
         */
        WIDEVINE,

        /**
         * Microsoft's [PlayReady](https://www.microsoft.com/PlayReady) DRM.
         */
        PLAYREADY
    }
}
