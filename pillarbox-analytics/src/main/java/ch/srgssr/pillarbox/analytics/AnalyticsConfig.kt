/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * SRG Analytics config
 *
 * @property distributor business unit distributor.
 * @property virtualSite virtual site.
 * @property nonLocalizedApplicationName Application name for the analytics, by default the application name defined in the manifest.
 * You can set it to null if the
 * application name is not localized.
 */
data class AnalyticsConfig(
    val distributor: BuDistributor,
    val virtualSite: String,
    val nonLocalizedApplicationName: String? = null
) {

    /**
     * Bu distributor
     */
    enum class BuDistributor {
        SRG, SWI, RTS, RSI, SRF, RTR
    }
}
