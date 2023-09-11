/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package ch.srgssr.pillarbox.analytics

import android.app.Application
import ch.srgssr.pillarbox.analytics.SRGAnalytics.initSRGAnalytics
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActEvent
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActPageView
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSrg
import ch.srgssr.pillarbox.analytics.comscore.ComScore
import ch.srgssr.pillarbox.analytics.comscore.ComScorePageView
import ch.srgssr.pillarbox.analytics.comscore.ComScoreSrg

/**
 * Analytics for SRG SSR
 *
 * Before using SRGAnalytics make sur to call [SRGAnalytics.init] or [initSRGAnalytics]
 *
 * ```kotlin
 * Class MyApplication : Application() {
 *
 *      override fun onCreate() {
 *          super.onCreate()
 *          val config = AnalyticsConfig(
 *              vendor = AnalyticsConfig.Vendor.SRG,
 *              virtualSite = "Your AppSiteName here",
 *              sourceKey = "CommandersAct source key"
 *          )
 *          initSRGAnalytics(config = config)
 *      }
 * }
 * ```
 */
object SRGAnalytics {
    private var instance: Analytics? = null

    /**
     * SRG CommandersAct analytics, do not use it unless you don't have any other choice!
     * Meant to be used internally inside Pillarbox
     */
    val commandersAct: CommandersAct
        get() {
            return instance!!.commandersAct
        }

    /**
     * SRG ComScore analytics, do not use it unless you don't have any other choice!
     * Meant to be used internally inside Pillarbox
     */
    val comScore: ComScore
        get() {
            return instance!!.comScore
        }

    /**
     * Initialize SRGAnalytics have to be called in [Application.onCreate]
     *
     * @param application The application instance where you override onCreate().
     * @param config The [AnalyticsConfig] to initialize with.
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
     * Send page view
     *
     * @param commandersAct The [CommandersActPageView] to send to CommandersAct.
     * @param comScore The [ComScorePageView] to send to ComScore.
     */
    fun sendPageView(commandersAct: CommandersActPageView, comScore: ComScorePageView) {
        instance?.sendPageView(commandersAct, comScore)
    }

    /**
     * Send event to CommandersAct
     *
     * @param event the [CommandersActEvent] to send.
     */
    fun sendEvent(event: CommandersActEvent) {
        instance?.sendEvent(event)
    }

    /**
     * Put persistent labels
     *
     * @param commandersActLabels CommandersAct specific persistent label.
     * @param comScoreLabels ComScore specific persistent label.
     */
    fun putPersistentLabels(
        commandersActLabels: Map<String, String>,
        comScoreLabels: Map<String, String>
    ) {
        instance?.putPersistentLabels(commandersActLabels = commandersActLabels, comScoreLabels = comScoreLabels)
    }

    /**
     * Remove persistent label for CommandersAct and/or ComScore.
     *
     * @param label The label to remove.
     */
    fun removePersistentLabel(label: String) {
        instance?.removePersistentLabel(label)
    }

    /**
     * Remove multiple persistent labels.
     *
     * @param labels List of labels to remove.
     */
    fun removePersistentLabels(labels: List<String>) {
        instance?.let { analytics ->
            for (label in labels) {
                analytics.removePersistentLabel(label)
            }
        }
    }

    /**
     * Get ComScore persistent label
     *
     * @param label The label to get.
     * @return associated ComScore label or null if not found.
     */
    fun getComScorePersistentLabel(label: String): String? {
        return instance?.getComScorePersistentLabel(label)
    }

    /**
     * Get CommandersAct persistent label
     *
     * @param label The label to get.
     * @return associated CommandersAct label or null if not found.
     */
    fun getCommandersActPersistentLabel(label: String): String? {
        return instance?.getCommandersActPermanentData(label)
    }

    /**
     * Set user consent
     *
     * @param userConsent The user consent to apply.
     */
    fun setUserConsent(userConsent: UserConsent) {
        instance?.setUserConsent(userConsent)
    }

    /**
     * Init SRGAnalytics
     *
     * @param config The [AnalyticsConfig] to initialize with.
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
            // Business decision to not send those event to comScore.
        }

        fun putPersistentLabels(
            commandersActLabels: Map<String, String>,
            comScoreLabels: Map<String, String>
        ) {
            comScore.putPersistentLabels(comScoreLabels)
            commandersAct.putPermanentData(commandersActLabels)
        }

        fun removePersistentLabel(label: String) {
            comScore.removePersistentLabel(label)
            commandersAct.removePermanentData(label)
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
