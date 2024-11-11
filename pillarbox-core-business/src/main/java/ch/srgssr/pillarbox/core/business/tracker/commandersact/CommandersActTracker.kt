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
 * A [MediaItemTracker] implementation for Commanders Act analytics.
 *
 * @param commandersAct The [CommandersAct] instance to use for tracking.
 * @param coroutineContext The [CoroutineContext] to use for asynchronous operations.
 */
class CommandersActTracker(
    private val commandersAct: CommandersAct,
    private val coroutineContext: CoroutineContext,
) : MediaItemTracker<CommandersActTracker.Data> {

    /**
     * Represents data to be sent to Commanders Act.
     *
     * @property assets A map of labels to be sent to Commanders Act.
     * @property sourceId
     */
    data class Data(val assets: Map<String, String>, val sourceId: String? = null)

    private var analyticsStreaming: CommandersActStreaming? = null

    override fun start(player: ExoPlayer, data: Data) {
        commandersAct.enableRunningInBackground()
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
     * A factory class responsible for creating instances of [CommandersActTracker].
     *
     * @param commandersAct The [CommandersAct] instance to use for tracking.
     * @param coroutineContext The [CoroutineContext] to use for asynchronous operations.
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
