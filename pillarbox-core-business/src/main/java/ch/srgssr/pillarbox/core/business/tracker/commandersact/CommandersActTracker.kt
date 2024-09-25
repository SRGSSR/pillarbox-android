/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import kotlin.coroutines.CoroutineContext

/**
 * Commanders act tracker
 *
 *  https://confluence.srg.beecollaboration.com/display/INTFORSCHUNG/standard+streaming+events%3A+sequence+of+events+for+media+player+actions
 *
 * @param commandersAct CommandersAct to send stream events
 * @param coroutineContext The coroutine context in which to track the events
 */
class CommandersActTracker(
    private val commandersAct: CommandersAct,
    private val coroutineContext: CoroutineContext,
) : MediaItemTracker<CommandersActTracker.Data> {

    /**
     * Data for CommandersAct
     *
     * @property assets labels to send to CommandersAct
     * @property sourceId TBD
     */
    data class Data(val assets: Map<String, String>, val sourceId: String? = null)

    private var analyticsStreaming: CommandersActStreaming? = null

    override fun start(player: ExoPlayer, data: Data) {
        analyticsStreaming = CommandersActStreaming(
            player = player,
            commandersAct = commandersAct,
            currentData = data,
            coroutineContext = coroutineContext
        ).also {
            player.addAnalyticsListener(it)
        }
    }

    override fun stop(player: ExoPlayer) {
        analyticsStreaming?.let {
            player.removeAnalyticsListener(it)
            it.stop()
        }
        analyticsStreaming = null
    }

    /**
     * Factory
     */
    class Factory(
        private val commandersAct: CommandersAct,
        private val coroutineContext: CoroutineContext,
    ) : MediaItemTracker.Factory<Data> {
        override fun create(): CommandersActTracker {
            return CommandersActTracker(commandersAct, coroutineContext)
        }
    }
}
