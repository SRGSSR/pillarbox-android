/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics.commandersact

import android.content.Context
import ch.srgssr.pillarbox.analytics.Analytics
import ch.srgssr.pillarbox.analytics.AnalyticsConfig
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.analytics.Event
import ch.srgssr.pillarbox.analytics.PageEvent
import ch.srgssr.pillarbox.analytics.R
import com.tagcommander.lib.serverside.TCPredefinedVariables
import com.tagcommander.lib.serverside.TCServerSide
import com.tagcommander.lib.serverside.TCServerSideConstants
import com.tagcommander.lib.serverside.events.TCCustomEvent
import com.tagcommander.lib.serverside.events.TCEvent
import com.tagcommander.lib.serverside.events.TCPageViewEvent

/**
 * Tag commander
 *
 * @property config
 * @constructor
 *
 * @param appContext
 * @param sideId
 * @param sourceKey
 */
class TagCommander(private val config: AnalyticsConfig, appContext: Context, sideId: Int, sourceKey: String) : Analytics {
    private val tcServerSide: TCServerSide

    init {
        tcServerSide = TCServerSide(sideId, sourceKey, appContext)

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
        tcServerSide.execute(event)
    }

    /**
     * Enable running in background
     *
     * While the application is going to background, the ServerSide's module sends all data that was already queued then stops.
     * This is in order to preserve battery life and not use carrier data when not required.
     * But some applications need to be able to continue sending data because they have real background activities. For example listening to music.
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
        // Event keys
        private const val TC_EVENT_NAME = "hidden_event"
        private const val EVENT_VALUE = "event_value"
        private const val EVENT_NAME = "event_name"
        private const val EVENT_TYPE = "event_type"
        private const val EVENT_SOURCE = "event_source"
        private const val EVENT_EXTRA_1 = "event_value_1"
        private const val EVENT_EXTRA_2 = "event_value_2"
        private const val EVENT_EXTRA_3 = "event_value_3"
        private const val EVENT_EXTRA_4 = "event_value_4"
        private const val EVENT_EXTRA_5 = "event_value_5"

        // Page View
        private const val NAVIGATION_LEVEL_I = "navigation_level_"
        private const val NAVIGATION_BU_DISTRIBUTER = "navigation_bu_distributer"

        // Permanent keys
        private const val APP_LIBRARY_VERSION = "app_library_version"
        private const val NAVIGATION_APP_SITE_NAME = "navigation_app_site_name"
        private const val NAVIGATION_DEVICE = "navigation_device"

        /**
         * Convert into TagCommander event.
         *
         * @return [TCCustomEvent]
         */
        fun Event.toTagCommanderEvent(): TCCustomEvent {
            val event = TCCustomEvent(TC_EVENT_NAME)
            event.addAdditionalParameter(EVENT_NAME, name)
            type?.let {
                event.addAdditionalParameter(EVENT_TYPE, it)
            }
            value?.let {
                event.addAdditionalParameter(EVENT_VALUE, it)
            }
            source?.let {
                event.addAdditionalParameter(EVENT_SOURCE, it)
            }
            extra1?.let { event.addAdditionalParameter(EVENT_EXTRA_1, it) }
            extra2?.let { event.addAdditionalParameter(EVENT_EXTRA_2, it) }
            extra3?.let { event.addAdditionalParameter(EVENT_EXTRA_3, it) }
            extra4?.let { event.addAdditionalParameter(EVENT_EXTRA_4, it) }
            extra5?.let { event.addAdditionalParameter(EVENT_EXTRA_5, it) }
            customLabels?.let {
                for (entry in it.entries) {
                    event.addAdditionalParameter(entry.key, entry.value)
                }
            }
            return event
        }

        /**
         * Convert into a TagCommander event
         *
         * @param distributor The [AnalyticsConfig.BuDistributor] to send with this event.
         * @return [TCPageViewEvent]
         */
        fun PageEvent.toTagCommanderEvent(distributor: String): TCPageViewEvent {
            val pageViewEvent = TCPageViewEvent()
            pageViewEvent.pageType = title
            for (i in levels.indices) {
                pageViewEvent.addAdditionalParameter(NAVIGATION_LEVEL_I + (i + 1), levels[i])
            }
            pageViewEvent.addAdditionalParameter(NAVIGATION_BU_DISTRIBUTER, distributor)
            customLabels?.let {
                for (entry in it.entries) {
                    pageViewEvent.addAdditionalParameter(entry.key, entry.value)
                }
            }
            return pageViewEvent
        }
    }
}
