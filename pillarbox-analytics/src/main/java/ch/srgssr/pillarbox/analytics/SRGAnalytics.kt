/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */

package ch.srgssr.pillarbox.analytics

import android.app.Application
import ch.srgssr.pillarbox.analytics.SRGAnalytics.init
import ch.srgssr.pillarbox.analytics.SRGAnalytics.initSRGAnalytics
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActEvent
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActPageView
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSrg
import ch.srgssr.pillarbox.analytics.commandersact.NoOpCommandersAct
import ch.srgssr.pillarbox.analytics.comscore.ComScore
import ch.srgssr.pillarbox.analytics.comscore.ComScorePageView
import ch.srgssr.pillarbox.analytics.comscore.ComScoreSrg
import ch.srgssr.pillarbox.analytics.comscore.NoOpComScore

/**
 * SRG Analytics entry point.
 *
 * This object provides a facade for interacting with both Commanders Act and ComScore analytics services. It allows for sending page views, events,
 * and managing persistent labels for both services.
 *
 * Before using any functionality, `SRGAnalytics` must be initialized in your [Application]'s [onCreate()][Application.onCreate] method using either
 * the [initSRGAnalytics()][initSRGAnalytics] or the [init()][init] method and providing an [AnalyticsConfig] instance.
 *
 * ```kotlin
 * class MyApplication : Application() {
 *      override fun onCreate() {
 *          super.onCreate()
 *
 *          val config = AnalyticsConfig(
 *              vendor = AnalyticsConfig.Vendor.SRG,
 *              appSiteName = "Your AppSiteName here",
 *              sourceKey = AnalyticsConfig.SOURCE_KEY_SRG_DEBUG,
 *          )
 *
 *          initSRGAnalytics(config)
 *          // or
 *          SRGAnalytics.init(this, config)
 *      }
 * }
 * ```
 */
object SRGAnalytics {
    private var instance: Analytics? = null

    /**
     * Provides access to the [CommandersAct] instance.
     *
     * If an instance of [CommandersAct] is available, it is returned. Otherwise, a no-op instance is returned, preventing any actions from being
     * performed.
     *
     * Do not use it unless you don't have any other choice! Meant to be used internally inside Pillarbox.
     *
     * @return The [CommandersAct] instance, or a no-op instance if none is available.
     */
    val commandersAct: CommandersAct
        get() {
            return instance?.commandersAct ?: NoOpCommandersAct
        }

    /**
     * Provides access to the [ComScore] instance.
     *
     * If an instance of [ComScore] is available, it is returned. Otherwise, a no-op instance is returned, preventing any actions from being
     * performed.
     *
     * Do not use it unless you don't have any other choice! Meant to be used internally inside Pillarbox.
     *
     * @return The [ComScore] instance, or a no-op instance if none is available.
     */
    val comScore: ComScore
        get() {
            return instance?.comScore ?: NoOpComScore
        }

    /**
     * Initializes the [SRGAnalytics] instance.
     *
     * This method should be called only once, typically in your [Application]'s [onCreate()][Application.onCreate] method. It initializes the
     * various analytics services like ComScore and Commanders Act based on the provided configuration.
     *
     * @param application The [Application] instance.
     * @param config The [AnalyticsConfig] object containing the configuration for the analytics services.
     *
     * @throws IllegalStateException If the [SRGAnalytics] instance is already initialized.
     */
    @Synchronized
    @JvmStatic
    fun init(application: Application, config: AnalyticsConfig) {
        require(instance == null) { "Already initialized" }
        instance = Analytics(
            comScore = ComScoreSrg.init(config = config, context = application),
            commandersAct = CommandersActSrg(config = config, appContext = application)
        )
    }

    /**
     * Sends a page view event to both Commanders Act and ComScore.
     *
     * @param commandersAct The page view data for Commanders Act.
     * @param comScore The page view data for ComScore.
     */
    fun sendPageView(commandersAct: CommandersActPageView, comScore: ComScorePageView) {
        instance?.sendPageView(commandersAct, comScore)
    }

    /**
     * Sends an event to Commanders Act.
     *
     * @param event The event to send.
     */
    fun sendEvent(event: CommandersActEvent) {
        instance?.sendEvent(event)
    }

    /**
     * Puts persistent labels for both Commanders Act and ComScore. These labels will be included in all subsequent tracking events until they are
     * overwritten.
     *
     * @param commandersActLabels A map representing the Commanders Act labels.
     * @param comScoreLabels A map representing the ComScore labels.
     */
    fun putPersistentLabels(
        commandersActLabels: Map<String, String>,
        comScoreLabels: Map<String, String>
    ) {
        instance?.putPersistentLabels(commandersActLabels = commandersActLabels, comScoreLabels = comScoreLabels)
    }

    /**
     * Removes a persistent label from both Commanders Act and ComScore.
     *
     * @param label The name of the persistent label to remove.
     */
    fun removePersistentLabel(label: String) {
        instance?.removePersistentLabel(label)
    }

    /**
     * Removes the specified persistent labels from both Commanders Act and ComScore.
     *
     * @param labels A list of persistent label names to remove.
     */
    fun removePersistentLabels(labels: List<String>) {
        instance?.let { analytics ->
            for (label in labels) {
                analytics.removePersistentLabel(label)
            }
        }
    }

    /**
     * Retrieves the ComScore persistent label associated with the given label.
     *
     * @param label The persistent label to retrieve.
     * @return The ComScore label associated with the provided persistent label, or `null` if the label is not found.
     */
    fun getComScorePersistentLabel(label: String): String? {
        return instance?.getComScorePersistentLabel(label)
    }

    /**
     * Retrieves the Commanders Act persistent label associated with the given label.
     *
     * @param label The persistent label to retrieve.
     * @return The Commanders Act label associated with the provided persistent label, or `null` if the label is not found.
     */
    fun getCommandersActPersistentLabel(label: String): String? {
        return instance?.getCommandersActPermanentData(label)
    }

    /**
     * Sets the user consent for both Commanders Act and ComScore.
     *
     * @param userConsent The [UserConsent] object containing the user consent settings.
     */
    fun setUserConsent(userConsent: UserConsent) {
        instance?.setUserConsent(userConsent)
    }

    /**
     * Initializes the [SRGAnalytics] instance.
     *
     * This method should be called only once, typically in your [Application]'s [onCreate()][Application.onCreate] method. It initializes the
     * various analytics services like ComScore and Commanders Act based on the provided configuration.
     *
     * @param config The [AnalyticsConfig] object containing the configuration for the analytics services.
     *
     * @throws IllegalStateException If the [SRGAnalytics] instance is already initialized.
     */
    fun Application.initSRGAnalytics(config: AnalyticsConfig) {
        init(this, config)
    }

    internal class Analytics(
        var comScore: ComScore,
        var commandersAct: CommandersAct
    ) {

        fun sendPageView(commandersAct: CommandersActPageView, comScore: ComScorePageView) {
            this.commandersAct.sendPageView(commandersAct)
            this.comScore.sendPageView(comScore)
        }

        fun sendEvent(commandersAct: CommandersActEvent) {
            this.commandersAct.sendEvent(commandersAct)
            // Business decision to not send those events to comScore.
        }

        fun putPersistentLabels(
            commandersActLabels: Map<String, String>,
            comScoreLabels: Map<String, String>
        ) {
            comScore.putPersistentLabels(comScoreLabels)
            commandersAct.putPermanentData(commandersActLabels)
        }

        fun removePersistentLabel(label: String) {
            commandersAct.removePermanentData(label)
            comScore.removePersistentLabel(label)
        }

        fun getComScorePersistentLabel(label: String): String? {
            return comScore.getPersistentLabel(label)
        }

        fun getCommandersActPermanentData(label: String): String? {
            return commandersAct.getPermanentDataLabel(label)
        }

        fun setUserConsent(userConsent: UserConsent) {
            comScore.setUserConsent(userConsent.comScore)
            commandersAct.setConsentServices(userConsent.commandersActConsentServices)
        }
    }
}
