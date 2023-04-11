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
 * Have to be initialized first with [SRGAnalytics.init]
 */
object SRGAnalytics : AnalyticsDelegate {
    private var config: Config? = null
    private var _commandersAct: CommandersAct? = null
    private var _comScore: ComScore? = null

    /**
     * TagCommander analytics
     */
    val commandersAct: CommandersAct
        get() = _commandersAct!!

    /**
     * ComScore analytics
     */
    val comScore: ComScore
        get() = _comScore!!

    /**
     * Init SRGAnalytics
     *
     * @param appContext Application context
     * @param config SRGAnalytics configuration
     */
    fun init(appContext: Context, config: Config): SRGAnalytics {
        if (this.config != null) {
            require(this.config == config) { "Already init with another config" }
            return this
        }
        return synchronized(this) {
            this.config = config
            _commandersAct = CommandersAct(config = config.analyticsConfig, commandersActConfig = config.commandersAct, appContext)
            _comScore = ComScore.init(config = config.analyticsConfig, appContext)
            this
        }
    }

    override fun sendPageViewEvent(pageEvent: PageEvent) {
        checkInitialized()
        commandersAct.sendPageViewEvent(pageEvent)
        comScore.sendPageViewEvent(pageEvent)
    }

    override fun sendEvent(event: Event) {
        checkInitialized()
        commandersAct.sendEvent(event)
        // Business decision to not send those event to comScore.
    }

    private fun checkInitialized() {
        requireNotNull(config) { "SRGAnalytics init has to be called before!" }
    }

    /**
     * Config for SRGAnalytics
     *
     * @property analyticsConfig Global analytics configuration.
     * @property commandersAct CommandersAct specific configuration.
     */
    data class Config(
        val analyticsConfig: AnalyticsConfig,
        val commandersAct: CommandersAct.Config
    )
}
