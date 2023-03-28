/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import android.content.Context
import ch.srgssr.pillarbox.analytics.commandersact.TagCommander
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
    val tagCommander =
        TagCommander(
            appContext = appContext,
            config = config.analyticsConfig,
            commandersActConfig = config.commandersAct
        )

    /**
     * ComScore analytics
     */
    val comScore = ComScore

    init {
        ComScore.init(config.analyticsConfig, config.comScore, appContext)
    }

    override fun sendPageViewEvent(pageEvent: PageEvent) {
        tagCommander.sendPageViewEvent(pageEvent)
        comScore.sendPageViewEvent(pageEvent)
    }

    override fun sendEvent(event: Event) {
        tagCommander.sendEvent(event)
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
        val commandersAct: TagCommander.Config = if (BuildConfig.DEBUG) TagCommander.Config.SRG_DEBUG else TagCommander.Config.SRG_PROD,
        val comScore: ComScore.Config = ComScore.Config()
    )
}
