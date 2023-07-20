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
    }
}
