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
     * Tag commander analytics
     */
    val tagCommander =
        TagCommander(
            appContext = appContext,
            config = config.analyticsConfig,
            sideId = config.commandersAct.sideId,
            sourceKey = config.commandersAct.sourceKey
        )
    val comScore = ComScore

    init {
        ComScore.init(config.analyticsConfig, appContext)
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
     */
    data class Config(val analyticsConfig: AnalyticsConfig, val commandersAct: CommandersAct) {
        /**
         * @property sideId The side id received from CommandersAct team.
         * @property sourceKey The sourceKey received from CommandersAct teams.
         */
        data class CommandersAct(val sideId: Int, val sourceKey: String)
    }
}
