/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import ch.srgssr.pillarbox.analytics.AnalyticsConfig.Companion.SOURCE_KEY_SRG_DEBUG
import ch.srgssr.pillarbox.analytics.AnalyticsConfig.Companion.SOURCE_KEY_SRG_PROD
import ch.srgssr.pillarbox.analytics.SRGAnalytics.initSRGAnalytics

/**
 * Represents the configuration for analytics tracking for SRG SSR applications. This should be used in conjunction with
 * [SRGAnalytics.initSRGAnalytics] or [SRGAnalytics.init].
 *
 * This class holds the necessary information for configuring analytics tracking, including the vendor, application details, user consent, and
 * persistent labels.
 *
 * @property vendor The vendor to which the application belongs to.
 * @property appSiteName The name of the app/site being tracked, given by the analytics team.
 * @property sourceKey The CommandersAct source key. Production apps should use [SOURCE_KEY_SRG_PROD], and apps in development should use
 * [SOURCE_KEY_SRG_DEBUG].
 * @property nonLocalizedApplicationName The non-localized name of the application. By default, the application name defined in the manifest is used.
 * @property userConsent The user consent to transmit to ComScore and CommandersAct.
 * @property comScorePersistentLabels The initial persistent labels for ComScore analytics.
 * @property commandersActPersistentLabels The initial persistent labels for Commanders Act analytics.
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
     * Represents the different vendors supported by the application.
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
         * The source key for SRG SSR apps in production.
         */
        const val SOURCE_KEY_SRG_PROD = "3909d826-0845-40cc-a69a-6cec1036a45c"

        /**
         * The source key for SRG SSR apps in development.
         */
        const val SOURCE_KEY_SRG_DEBUG = "6f6bf70e-4129-4e47-a9be-ccd1737ba35f"
    }
}
