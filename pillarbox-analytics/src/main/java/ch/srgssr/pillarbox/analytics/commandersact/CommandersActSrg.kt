/*
 * Copyright (c) SRG SSR. All rights reserved.
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
import com.tagcommander.lib.serverside.schemas.TCDevice

/**
 * CommandersAct for SRG SSR
 *
 * @param tcServerSide TCServiceSide to use.
 * @param config analytics config.
 * @param navigationDevice The navigation device.
 *
 * @constructor
 */
internal class CommandersActSrg(
    private val tcServerSide: TCServerSide,
    private val config: AnalyticsConfig,
    navigationDevice: String
) : CommandersAct {

    init {
        TCDebug.setDebugLevel(if (BuildConfig.DEBUG) Log.DEBUG else Log.INFO)

        config.commandersActPersistentLabels?.let {
            putPermanentData(it)
        }

        // Data send with all events that never change
        tcServerSide.addPermanentData(APP_LIBRARY_VERSION, "${BuildConfig.VERSION_NAME}  ${BuildConfig.BUILD_DATE}")
        tcServerSide.addPermanentData(NAVIGATION_APP_SITE_NAME, config.appSiteName)
        tcServerSide.addPermanentData(NAVIGATION_DEVICE, navigationDevice)
    }

    constructor(
        config: AnalyticsConfig,
        appContext: Context
    ) : this(
        tcServerSide = TCServerSide(SITE_SRG, config.sourceKey, appContext),
        config = config,
        navigationDevice = appContext.getString(R.string.tc_analytics_device)
    ) {
        workaroundUniqueIdV4Tov5()
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

    override fun putPermanentData(labels: Map<String, String>) {
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

    override fun setConsentServices(consentServices: List<String>) {
        tcServerSide.addPermanentData(CommandersActLabels.CONSENT_SERVICES.label, consentServices.joinToString(","))
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

    /*
  * From issue :
  *  - https://github.com/SRGSSR/srgletterbox-android/issues/522
  *  - https://github.com/CommandersAct/iOSV5/issues/13
  *
  * And after discussion with CommandersAct teams and SRG ADI team.
  */
    private fun workaroundUniqueIdV4Tov5() {
        // 1. Use the TC unique id value for new `device.sdk_id` property.
        useLegacyUniqueIDForSdkID()
        // 2. Use the TC unique id value for the new `user.consistent_anonymous_id` property.
        TCPredefinedVariables.getInstance().useLegacyUniqueIDForAnonymousID()
    }

    private fun useLegacyUniqueIDForSdkID() {
        TCDevice.getInstance().sdkID = TCPredefinedVariables.getInstance().uniqueIdentifier
    }

    companion object {
        private const val SITE_SRG = 3666

        // Permanent keys
        private const val APP_LIBRARY_VERSION = "app_library_version"
        private const val NAVIGATION_APP_SITE_NAME = "navigation_app_site_name"
        private const val NAVIGATION_DEVICE = "navigation_device"
    }
}
