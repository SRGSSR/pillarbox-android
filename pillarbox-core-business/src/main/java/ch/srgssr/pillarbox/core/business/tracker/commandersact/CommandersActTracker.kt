/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import androidx.media3.common.Player
import androidx.media3.common.Player.PositionInfo
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
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
) : MediaItemTracker<CommandersActTracker.Data>, AnalyticsListener {

    /**
     * Stop reason
     */
    enum class StopReason {

        /**
         * When the player has been stopped, released or its current media item changes.
         */
        Stop,

        /**
         * When the player reaches the end of the media.
         */
        EoF
    }

    /**
     * Data for CommandersAct
     *
     * @property assets labels to send to CommandersAct
     * @property sourceId TBD
     */
    data class Data(val assets: Map<String, String>, val sourceId: String? = null)
    private var analyticsStreaming: CommandersActStreaming? = null
    private lateinit var currentData: Data
    private lateinit var player: ExoPlayer

    override fun start(player: ExoPlayer, data: Data) {
        require(analyticsStreaming == null) { "AnalyticsStreaming already start" }
        this.player = player
        commandersAct.enableRunningInBackground()
        currentData = data
        analyticsStreaming = CommandersActStreaming(
            commandersAct = commandersAct,
            player = player,
            currentData = data,
            coroutineContext = coroutineContext,
        )
        player.addAnalyticsListener(this)
        analyticsStreaming?.let {
            player.addAnalyticsListener(it)
        }
    }

    override fun stop(player: ExoPlayer) {
        player.removeAnalyticsListener(this)
        stop(StopReason.Stop, player.currentPosition)
    }

    private fun stop(reason: StopReason, positionMs: Long) {
        analyticsStreaming?.let {
            player.removeAnalyticsListener(it)
            it.notifyStop(position = positionMs.milliseconds, reason == StopReason.EoF)
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

    override fun onPlaybackStateChanged(
        eventTime: AnalyticsListener.EventTime,
        @Player.State playbackState: Int,
    ) {
        when (playbackState) {
            Player.STATE_ENDED -> stop(StopReason.EoF, player.currentPosition)
            Player.STATE_IDLE -> stop(StopReason.Stop, player.currentPosition)
            Player.STATE_READY -> {
                if (analyticsStreaming == null) {
                    start(player, currentData)
                }
            }

            else -> Unit
        }
    }

    /*
     * On position discontinuity handle stop session if required
     */
    override fun onPositionDiscontinuity(
        eventTime: AnalyticsListener.EventTime,
        oldPosition: PositionInfo,
        newPosition: PositionInfo,
        @Player.DiscontinuityReason reason: Int,
    ) {
        val oldPositionMs = oldPosition.positionMs
        when (reason) {
            Player.DISCONTINUITY_REASON_REMOVE -> stop(StopReason.Stop, oldPositionMs)
            Player.DISCONTINUITY_REASON_AUTO_TRANSITION -> {
                stop(StopReason.EoF, oldPositionMs)
                start(player, currentData)
            }

            else -> {
                if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) {
                    stop(StopReason.Stop, oldPositionMs)
                }
            }
        }
    }
}
