/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

/**
 * SRG Analytics config
 *
 * @property vendor business unit.
 * @property appSiteName The App/Site name given by the analytics team.
 * @property sourceKey The CommandersAct sourceKey given by the analytics team.
 * @property nonLocalizedApplicationName Application name for the analytics, by default the application name defined in the manifest. You can set
 * it to null if the application name is not localized.
 * @property userConsent User consent to transmit to ComScore and CommandersAct.
 * @property comScorePersistentLabels initial ComScore persistent labels.
 * @property commandersActPersistentLabels initial CommandersAct persistent labels.
 */
data class AnalyticsConfig(
    val vendor: Vendor,
    val appSiteName: String,
    val sourceKey: String,
    val nonLocalizedApplicationName: String? = null,
    val userConsent: UserConsent = UserConsent(),
    val comScorePersistentLabels: Map<String, String>? = null,
    val commandersActPersistentLabels: Map<String, String>? = null,
) {

    /**
     * Vendor
     */
    @Suppress("UndocumentedPublicProperty")
    enum class Vendor {
        SRG,
        SWI,
        RTS,
        RSI,
        SRF,
        RTR
    }

    @Suppress("UndocumentedPublicClass")
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
