/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

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
) : MediaItemTracker {
    /**
     * Data for CommandersAct
     *
     * @property assets labels to send to CommandersAct
     * @property sourceId TBD
     */
    data class Data(val assets: Map<String, String>, val sourceId: String? = null)

    private var analyticsStreaming: CommandersActStreaming? = null
    private var currentData: Data? = null

    override fun start(player: ExoPlayer, initialData: Any?) {
        requireNotNull(initialData)
        require(initialData is Data)
        commandersAct.enableRunningInBackground()
        currentData = initialData
        analyticsStreaming = CommandersActStreaming(
            commandersAct = commandersAct,
            player = player,
            currentData = initialData,
            coroutineContext = coroutineContext,
        )
        analyticsStreaming?.let {
            player.addAnalyticsListener(it)
        }
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason, positionMs: Long) {
        analyticsStreaming?.let {
            player.removeAnalyticsListener(it)
            it.notifyStop(position = positionMs.milliseconds, reason == MediaItemTracker.StopReason.EoF)
        }
        analyticsStreaming = null
        currentData = null
    }

    /**
     * Factory
     */
    class Factory(
        private val commandersAct: CommandersAct,
        private val coroutineContext: CoroutineContext,
    ) : MediaItemTracker.Factory {
        override fun create(): MediaItemTracker {
            return CommandersActTracker(commandersAct, coroutineContext)
        }
    }
}
