/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import android.content.Context
import android.util.Log
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.analytics.R
import com.tagcommander.lib.core.TCDebug
import com.tagcommander.lib.serverside.TCPredefinedVariables
import com.tagcommander.lib.serverside.TCServerSide
import com.tagcommander.lib.serverside.TCServerSideConstants
import com.tagcommander.lib.serverside.events.base.TCEvent

/**
 * CommandersAct for SRG SSR
 *
 * @property config analytics config.
 * @constructor
 *
 * @param appContext application context.
 */
internal class CommandersActSrg(
    private val config: AnalyticsConfig,
    appContext: Context
) :
    CommandersAct {

    /**
     * Debug listener for testing purpose.
     */
    interface DebugListener {
        /**
         * On event sent
         *
         * @param event sent to [tcServerSide]
         */
        fun onEventSent(event: TCEvent)
    }

    private val tcServerSide: TCServerSide

    init {
        tcServerSide = TCServerSide(SITE_SRG, config.sourceKey, appContext)
        TCDebug.setDebugLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)

        // Data send with all events that never change
        tcServerSide.addPermanentData(APP_LIBRARY_VERSION, "${BuildConfig.VERSION_NAME}  ${BuildConfig.BUILD_DATE}")
        tcServerSide.addPermanentData(NAVIGATION_APP_SITE_NAME, config.appSiteName)
        tcServerSide.addPermanentData(NAVIGATION_DEVICE, appContext.getString(R.string.tc_analytics_device))
    }

    override fun sendPageView(pageView: CommandersActPageView) {
        require(pageView.name.isNotBlank()) { "Empty page title!" }
        sendTcEvent(pageView.toTCPageViewEvent(config.vendor))
    }

    override fun sendEvent(event: CommandersActEvent) {
        sendTcEvent(event.toTCCustomEvent())
    }

    override fun sendTcMediaEvent(event: TCMediaEvent) {
        sendTcEvent(event)
    }

    private fun sendTcEvent(event: TCEvent) {
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
    override fun enableRunningInBackground() {
        // on apple always one! Maybe tracker enable it.
        tcServerSide.enableRunningInBackground()
    }

    override fun putPermanentData(labels: Map<String, String>?) {
        if (labels == null) return
        for (entry in labels.entries) {
            tcServerSide.addPermanentData(entry.key, entry.value)
        }
    }

    override fun removePermanentData(label: String) {
        tcServerSide.removePermanentData(label)
    }

    override fun getPermanentDataLabel(label: String): String? {
        return tcServerSide.getPermanentData(label)
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
    }
}
