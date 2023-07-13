/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MemberVisibilityCanBePrivate")

package ch.srgssr.pillarbox.analytics

import android.content.Context
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActSrg
import ch.srgssr.pillarbox.analytics.comscore.ComScore
import ch.srgssr.pillarbox.analytics.comscore.ComScoreSrg

/**
 * Analytics for SRG SSR
 *
 * To retrieve the instance use [Context.analytics] (preferred) or [SRGAnalytics.getInstance].
 *
 * Make sure Application implements [AnalyticsConfigProvider].
 *
 * @property comScore ComScore analytics provider. Don't use it directly unless you have no over choice!
 * @property commandersAct CommandersAct analytics provider. Don't use it directly unless you have no over choice!
 */
class SRGAnalytics internal constructor(
    val comScore: ComScore,
    val commandersAct: CommandersAct
) {
    private constructor(context: Context, config: AnalyticsConfig) :
        this(
            ComScoreSrg.init(context = context, config = config),
            CommandersActSrg(appContext = context.applicationContext, config = config)
        )

    /**
     * Send page view
     *
     * @param pageView the [PageView] to send to CommandersAct and ComScore.
     */
    fun sendPageView(pageView: PageView) {
        commandersAct.sendPageView(pageView)
        comScore.sendPageView(pageView.title)
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
        commandersAct.sendEvent(event)
        // Business decision to not send those event to comScore.
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

    companion object {
        private var analytics: SRGAnalytics? = null

        /**
         * Get instance
         *
         * @param context The application context.
         * @return singleton instance of [SRGAnalytics]
         */
        @JvmStatic
        fun getInstance(context: Context): SRGAnalytics {
            return analytics ?: newAnalytics(context)
        }

        @Synchronized
        private fun newAnalytics(context: Context): SRGAnalytics {
            analytics?.let { return it }
            val config = (context.applicationContext as? AnalyticsConfigProvider)?.analyticsConfig
            requireNotNull(config) { "Make sure that Application implements AnalyticsConfigProvider" }
            val newSRGAnalytics = SRGAnalytics(config = config, context = context)
            analytics = newSRGAnalytics
            return newSRGAnalytics
        }
    }
}

/**
 * Retrieve the unique instance of [SRGAnalytics].
 */
inline val Context.analytics: SRGAnalytics
    get() = SRGAnalytics.getInstance(this)

/**
 * Send page view
 *
 * @param pageView the [PageView] to send to CommandersAct and ComScore.
 */
fun Context.sendPageView(pageView: PageView) {
    this.analytics.sendPageView(pageView)
}

/**
 * Send event to CommandersAct
 *
 * @param event the [Event] to send.
 */
fun Context.sendEvent(event: Event) {
    this.analytics.sendEvent(event)
}
