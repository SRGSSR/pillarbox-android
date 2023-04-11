/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import android.content.Context
import android.util.Log
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.AnalyticsDelegate
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.analytics.Event
import ch.srgssr.pillarbox.analytics.PageView
import ch.srgssr.pillarbox.analytics.R
import ch.srgssr.pillarbox.analytics.commandersact.TCEventUtils.toTCCustomEvent
import com.tagcommander.lib.core.TCDebug
import com.tagcommander.lib.serverside.TCPredefinedVariables
import com.tagcommander.lib.serverside.TCServerSide
import com.tagcommander.lib.serverside.TCServerSideConstants
import com.tagcommander.lib.serverside.events.TCEvent

/**
 * CommandersAct AnalyticsDelegate
 *
 * @property config analytics config.
 * @constructor
 *
 * @param commandersActConfig CommandersAct configuration received from your analytics team.
 * @param appContext application context.
 *
 */
class CommandersAct(private val config: AnalyticsConfig, commandersActConfig: Config, appContext: Context) : AnalyticsDelegate {
    /**
     * Config
     *
     * @property virtualSite The app site name given by the analytics team.
     * @property sourceKey The sourceKey given by the analytics team.
     */
    data class Config(
        val virtualSite: String,
        val sourceKey: String = if (BuildConfig.DEBUG) SOURCE_KEY_SRG_DEBUG else SOURCE_KEY_SRG_PROD
    ) {

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

    private val tcServerSide: TCServerSide

    init {
        tcServerSide = TCServerSide(SITE_SRG, commandersActConfig.sourceKey, appContext)
        TCDebug.setDebugLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)

        // Data send with all events that never change
        tcServerSide.addPermanentData(APP_LIBRARY_VERSION, "${BuildConfig.VERSION_NAME}  ${BuildConfig.BUILD_DATE}")
        tcServerSide.addPermanentData(NAVIGATION_APP_SITE_NAME, commandersActConfig.virtualSite)
        tcServerSide.addPermanentData(NAVIGATION_DEVICE, appContext.getString(R.string.tc_analytics_device))
    }

    override fun sendPageView(pageView: PageView) {
        require(pageView.title.isNotBlank()) { "Empty page title!" }
        sendTcEvent(pageView.toTCCustomEvent(config.distributor.toString()))
    }

    override fun sendEvent(event: Event) {
        sendTcEvent(event.toTCCustomEvent())
    }

    /**
     * Send tc event to TagCommander.
     *
     * @param event to send
     */
    fun sendTcEvent(event: TCEvent) {
        overrideApplicationNameIfNeeded()
        tcServerSide.execute(event)
    }

    /**
     * Enable running in background
     *
     * While the application is going to background, the ServerSide's module sends all data that was already queued then stops.
     * This is in order to preserve battery life and not use carrier data when not required.
     * But some applications need to be able to continue sending data because they have real background activities.
     * For example listening to music.
     */
    fun enableRunningInBackground() {
        // on apple always one! Maybe tracker enable it.
        tcServerSide.enableRunningInBackground()
    }

    /**
     * Override application name if [AnalyticsConfig.nonLocalizedApplicationName] is not empty.
     * Useful for application that localized their application name and want to have same name for analytics.
     */
    private fun overrideApplicationNameIfNeeded() {
        if (!config.nonLocalizedApplicationName.isNullOrBlank()) {
            TCPredefinedVariables.getInstance()
                .addData(TCServerSideConstants.kTCPredefinedVariable_ApplicationName, config.nonLocalizedApplicationName)
        }
    }

    companion object {
        private const val SITE_SRG = 3666

        // Permanent keys
        private const val APP_LIBRARY_VERSION = "app_library_version"
        private const val NAVIGATION_APP_SITE_NAME = "navigation_app_site_name"
        private const val NAVIGATION_DEVICE = "navigation_device"

        /**
         * Custom label key for user_id
         */
        const val KEY_USER_ID = "user_id"

        /**
         * Custom label Key for user_is_logged
         */
        const val KEY_USER_IS_LOGGED = "user_is_logged"
    }
}
