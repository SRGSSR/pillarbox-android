/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import android.content.Context
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.comscore.ComScore

/**
 * Analytics for SRGSSR
 *
 * @param appContext Application context.
 * @param config Global analytics config.
 */
class SRGAnalytics(appContext: Context, config: Config) : AnalyticsDelegate {

    /**
     * TagCommander analytics
     */
    val commandersAct by lazy {
        CommandersAct(
            appContext = appContext,
            config = config.analyticsConfig,
            commandersActConfig = config.commandersAct
        )
    }

    /**
     * ComScore analytics
     */
    val comScore by lazy { ComScore.init(config.analyticsConfig, config.comScore, appContext) }

    override fun sendPageViewEvent(pageEvent: PageEvent) {
        commandersAct.sendPageViewEvent(pageEvent)
        comScore.sendPageViewEvent(pageEvent)
    }

    override fun sendEvent(event: Event) {
        commandersAct.sendEvent(event)
        // Business decision to not send those event to comScore.
    }

    /**
     * Config for SRGAnalytics
     *
     * @property analyticsConfig Global analytics configuration.
     * @property commandersAct CommandersAct specific configuration.
     * @property comScore ComScore specific configuration.
     */
    data class Config(
        val analyticsConfig: AnalyticsConfig,
        val commandersAct: CommandersAct.Config = if (BuildConfig.DEBUG) CommandersAct.Config.SRG_DEBUG else CommandersAct.Config.SRG_PROD,
        val comScore: ComScore.Config = ComScore.Config()
    )
}
