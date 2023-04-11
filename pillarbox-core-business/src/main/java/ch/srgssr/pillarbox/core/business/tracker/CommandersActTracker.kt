/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker

import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.tagcommander.lib.serverside.events.TCCustomEvent

/**
 * Commanders act tracker
 *
 * @property commandersAct CommandersAct to send stream events
 */
class CommandersActTracker(private val commandersAct: CommandersAct) : MediaItemTracker {
    /**
     * Data for CommandersAct
     *
     * @property assets labels to send to CommandersAct
     * @property sourceId TBD
     */
    data class Data(val assets: Map<String, String>, val sourceId: String? = null)

    private val playerComponent = PlayerComponent()
    private var _currentData: Data? = null
    private val currentData: Data
        get() = _currentData!!

    override fun start(player: ExoPlayer, initialData: Any?) {
        requireNotNull(initialData)
        require(initialData is Data)
        _currentData = initialData
        player.addAnalyticsListener(playerComponent)
        if (player.isPlaying) {
            onStart()
        }
    }

    // stop is called
    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason) {
        player.removeAnalyticsListener(playerComponent)
        when (reason) {
            MediaItemTracker.StopReason.EoF -> onEoF()
            else -> onStop()
        }
    }

    private fun notifyEvent(name: String, mediaPositionMs: Long) {
        val event = TCCustomEvent(name)
        event.addAll(currentData.assets)

        DebugLogger.debug(TAG, "send : $name p = $mediaPositionMs")
    }

    private fun onStart() {
        notifyEvent("start", 0L)
    }

    private fun onStop() {
        notifyEvent("stop", 0L)
    }

    private fun onEoF() {
        notifyEvent("eof", 0L)
    }

    private inner class PlayerComponent : AnalyticsListener

    /**
     * Factory
     */
    class Factory(private val commandersAct: CommandersAct) : MediaItemTracker.Factory {
        override fun create(): MediaItemTracker {
            return CommandersActTracker(commandersAct)
        }
    }

    companion object {
        private const val TAG = "CommandersActTracker"

        private fun TCCustomEvent.addAll(map: Map<String, String>) {
            for (entry in map) {
                addAdditionalParameter(entry.key, entry.value)
            }
        }
    }
}
