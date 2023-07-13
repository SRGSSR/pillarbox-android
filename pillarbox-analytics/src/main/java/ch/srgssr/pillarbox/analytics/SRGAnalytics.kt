/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package ch.srgssr.pillarbox.analytics

import android.app.Application
import ch.srgssr.pillarbox.analytics.SRGAnalytics.initSRGAnalytics
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSrg
import ch.srgssr.pillarbox.analytics.comscore.ComScore
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
     * Initialiaze SRGAnalytics have to be called in [Application.onCreate]
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
     * @param pageView the [PageView] to send to CommandersAct and ComScore.
     */
    fun sendPageView(pageView: PageView) {
        instance?.sendPageView(pageView)
    }

    /**
     * Send page view for convenience without creating [PageView]
     * @see [PageView]
     */
    fun sendPageView(title: String, levels: List<String> = emptyList()) {
        sendPageView(PageView(title = title, levels = levels))
    }

    /**
     * Send event to CommandersAct
     *
     * @param event the [Event] to send.
     */
    fun sendEvent(event: Event) {
        instance?.sendEvent(event)
    }

    /**
     * Send event for convenience without creating [Event]
     * @see [Event]
     */
    fun sendEvent(
        name: String,
        type: String? = null,
        value: String? = null,
        source: String? = null,
        extra1: String? = null,
        extra2: String? = null,
        extra3: String? = null,
        extra4: String? = null,
        extra5: String? = null
    ) {
        sendEvent(
            Event(
                name = name,
                type = type,
                value = value,
                source = source,
                extra1 = extra1,
                extra2 = extra2,
                extra3 = extra3,
                extra4 = extra4,
                extra5 = extra5
            )
        )
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

        fun sendPageView(pageView: PageView) {
            commandersAct.sendPageView(pageView)
            comScore.sendPageView(pageView.title)
        }

        fun sendPageView(title: String, levels: List<String> = emptyList()) {
            sendPageView(PageView(title = title, levels = levels))
        }

        fun sendEvent(event: Event) {
            commandersAct.sendEvent(event)
            // Business decision to not send those event to comScore.
        }
    }
}
