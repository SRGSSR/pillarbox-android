/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * SRG Analytics config
 *
 * @property distributor business unit distributor.
 * @property nonLocalizedApplicationName Application name for the analytics, by default the application name defined in the manifest. You can set
 * it to null if the application name is not localized.
 * @property virtualSite The app site name given by the analytics team.
 * @property sourceKey The sourceKey given by the analytics team.
 */
data class AnalyticsConfig(
    val distributor: BuDistributor,
    val nonLocalizedApplicationName: String? = null,
    val virtualSite: String,
    val sourceKey: String
) {

    /**
     * Bu distributor
     */
    enum class BuDistributor {
        SRG, SWI, RTS, RSI, SRF, RTR
    }

    companion object {
        /**
         * SRG Production CommandersAct configuration
         */
        const val SOURCE_KEY_SRG_PROD = "3909d826-0845-40cc-a69a-6cec1036a45c"

        /**
         * SRG Debug CommandersAct configuration
         */
        const val SOURCE_KEY_SRG_DEBUG = "6f6bf70e-4129-4e47-a9be-ccd1737ba35f"
    }
}
