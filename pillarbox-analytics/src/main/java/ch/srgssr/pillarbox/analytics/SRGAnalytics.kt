/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.analytics

import android.content.Context
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActConfig
import ch.srgssr.pillarbox.analytics.commandersact.CommandersActImpl
import ch.srgssr.pillarbox.analytics.comscore.ComScore

/**
 * Analytics for SRGSSR
 *
 * Have to be initialized first with [SRGAnalytics.init]
 */
object SRGAnalytics : PageViewAnalytics, EventAnalytics, UserAnalytics {
    private var config: Config? = null
    private var _commandersAct: CommandersActImpl? = null
    private var _comScore: ComScore? = null

    /**
     * TagCommander analytics
     */
    val commandersAct: CommandersAct
        get() = _commandersAct!!

    /**
     * ComScore analytics
     */
    val comScore: PageViewAnalytics
        get() = _comScore!!

    override var userId: String? = null
        set(value) {
            field = value
            _commandersAct?.userId = field
        }
    override var isLogged: Boolean = false
        set(value) {
            field = value
            _commandersAct?.isLogged = field
        }

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
            _commandersAct = CommandersActImpl(config = config.analyticsConfig, commandersActConfig = config.commandersAct, appContext).also {
                it.userId = userId
                it.isLogged = isLogged
            }
            _comScore = ComScore.init(config = config.analyticsConfig, appContext)
            this
        }
    }

    override fun sendPageView(pageView: PageView) {
        checkInitialized()
        commandersAct.sendPageView(pageView)
        comScore.sendPageView(pageView)
    }

    /**
     * Send page view for convenience without creating [PageView]
     * @see [PageView]
     */
    fun sendPageView(title: String, levels: Array<String> = emptyArray()) {
        sendPageView(PageView(title = title, levels = levels))
    }

    override fun sendEvent(event: Event) {
        checkInitialized()
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
        val commandersAct: CommandersActConfig
    )
}
