/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import android.content.Context
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.AnalyticsDelegate
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.analytics.Event
import ch.srgssr.pillarbox.analytics.PageEvent
import ch.srgssr.pillarbox.analytics.R
import ch.srgssr.pillarbox.analytics.commandersact.TCEventUtils.toTagCommanderEvent
import com.tagcommander.lib.serverside.TCPredefinedVariables
import com.tagcommander.lib.serverside.TCServerSide
import com.tagcommander.lib.serverside.TCServerSideConstants
import com.tagcommander.lib.serverside.events.TCEvent

/**
 * Tag commander
 *
 * @property config analytics config.
 * @constructor
 *
 * @param commandersActConfig CommandersAct configuration received from your analytics team.
 * @param appContext application context.
 *
 */
class TagCommander(private val config: AnalyticsConfig, commandersActConfig: Config, appContext: Context) : AnalyticsDelegate {
    /**
     * @property sideId The side id received from CommandersAct team.
     * @property sourceKey The sourceKey received from CommandersAct teams.
     */
    data class Config(val sideId: Int, val sourceKey: String) {
        companion object {
            private const val SITE_SRG = 3666

            /**
             * SRG Production CommandersAct configuration
             */
            val SRG_PROD = Config(SITE_SRG, "3909d826-0845-40cc-a69a-6cec1036a45c")

            /**
             * SRG Debug CommandersAct configuration
             */
            val SRG_DEBUG = Config(SITE_SRG, "6f6bf70e-4129-4e47-a9be-ccd1737ba35f")
        }
    }

    private val tcServerSide: TCServerSide

    /**
     * User id to send with all events
     */
    var userId: String? = null

    init {
        tcServerSide = TCServerSide(commandersActConfig.sideId, commandersActConfig.sourceKey, appContext)

        // Data send with all events that never change
        tcServerSide.addPermanentData(APP_LIBRARY_VERSION, "${BuildConfig.VERSION_NAME}  ${BuildConfig.BUILD_DATE}")
        tcServerSide.addPermanentData(NAVIGATION_APP_SITE_NAME, config.virtualSite)
        tcServerSide.addPermanentData(NAVIGATION_DEVICE, appContext.getString(R.string.tc_analytics_device))
    }

    override fun sendPageViewEvent(pageEvent: PageEvent) {
        require(pageEvent.title.isNotBlank()) { "Empty page title!" }
        sendTcEvent(pageEvent.toTagCommanderEvent(config.distributor.toString()))
    }

    override fun sendEvent(event: Event) {
        sendTcEvent(event.toTagCommanderEvent())
    }

    /**
     * Send tc event to TagCommander.
     *
     * @param event to send
     */
    fun sendTcEvent(event: TCEvent) {
        overrideApplicationNameIfNeeded()
        addUserInfo(event)
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

    private fun addUserInfo(event: TCEvent) {
        val isLogged = userId?.let {
            event.addAdditionalParameter(KEY_USER_ID, userId)
            true
        } ?: false
        event.addAdditionalParameter(KEY_USER_IS_LOGGED, isLogged.toString())
    }

    companion object {
        // Permanent keys
        private const val APP_LIBRARY_VERSION = "app_library_version"
        private const val NAVIGATION_APP_SITE_NAME = "navigation_app_site_name"
        private const val NAVIGATION_DEVICE = "navigation_device"

        private const val KEY_USER_ID = "user_id"
        private const val KEY_USER_IS_LOGGED = "user_is_logged"
    }
}
