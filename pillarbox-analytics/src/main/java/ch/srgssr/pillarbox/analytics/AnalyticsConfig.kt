/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * SRG Analytics config
 *
 * @property vendor business unit.
 * @property nonLocalizedApplicationName Application name for the analytics, by default the application name defined in the manifest. You can set
 * it to null if the application name is not localized.
 * @property appSiteName The App/Site name given by the analytics team.
 * @property sourceKey The CommandersAct sourceKey given by the analytics team.
 */
data class AnalyticsConfig(
    val vendor: Vendor,
    val nonLocalizedApplicationName: String? = null,
    val appSiteName: String,
    val sourceKey: String
) {

    /**
     * Vendor
     */
    enum class Vendor {
        SRG, SWI, RTS, RSI, SRF, RTR
    }

    companion object {
        /**
         * SRG Production CommandersAct source key
         */
        const val SOURCE_KEY_SRG_PROD = "3909d826-0845-40cc-a69a-6cec1036a45c"

        /**
         * SRG Debug CommandersAct source key
         */
        const val SOURCE_KEY_SRG_DEBUG = "6f6bf70e-4129-4e47-a9be-ccd1737ba35f"
    }
}
