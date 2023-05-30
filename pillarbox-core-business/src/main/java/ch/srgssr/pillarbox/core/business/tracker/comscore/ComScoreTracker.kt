/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.comscore

import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline.Window
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.player.getPlaybackSpeed
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.comscore.Analytics
import com.comscore.streaming.ContentMetadata
import com.comscore.streaming.StreamingAnalytics
import com.comscore.streaming.StreamingListener

/**
 * ComScore tracker
 *
 * This tracker will handle
 *  - Analytics.notifyUxActive and Analytics.notifiyUxInactive connecting to playing state of player.
 */
class ComScoreTracker : MediaItemTracker {
    /**
     * Data for ComScore
     *
     * @property assets labels to send to ComScore StreamingAnalytics
     */
    data class Data(val assets: Map<String, String>)

    private val component = PlayerComponent()
    private val streamingAnalytics = StreamingAnalytics()
    private val window = Window()
    private lateinit var latestData: Data
    private val debugListener =
        StreamingListener { oldState, newState, eventLabels -> DebugLogger.warning(TAG, "changes : $oldState -> $newState $eventLabels") }

    init {
        streamingAnalytics.setMediaPlayerName(MEDIA_PLAYER_NAME)
        streamingAnalytics.setMediaPlayerVersion(BuildConfig.VERSION_NAME)
    }

    override fun start(player: ExoPlayer, initialData: Any?) {
        requireNotNull(initialData)
        require(initialData is Data)
        streamingAnalytics.addListener(debugListener)
        streamingAnalytics.createPlaybackSession()
        setMetadata(initialData)
        handleStart(player)
        player.addAnalyticsListener(component)
    }

    override fun stop(player: ExoPlayer, reason: MediaItemTracker.StopReason) {
        player.removeAnalyticsListener(component)
        notifyEnd()
        streamingAnalytics.removeListener(debugListener)
    }

    override fun update(data: Any) {
        require(data is Data)
        if (latestData != data) {
            setMetadata(data)
        }
    }

    private fun setMetadata(data: Data) {
        DebugLogger.debug(TAG, "SetMetadata $data")
        val assets = ContentMetadata.Builder()
            .customLabels(data.assets)
            .build()
        streamingAnalytics.setMetadata(assets)
        latestData = data
    }

    private fun handleStart(player: ExoPlayer) {
        streamingAnalytics.notifyChangePlaybackRate(player.getPlaybackSpeed())
        when {
            player.isPlaying -> {
                player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
                notifyPlay(player.currentPosition, window)
            }
            player.playbackState == Player.STATE_BUFFERING -> notifyBufferStart()
        }
    }

    private fun notifyPause() {
        DebugLogger.debug(TAG, "notifyPause")
        streamingAnalytics.notifyPause()
        Analytics.notifyUxInactive()
    }

    private fun notifyPlay(position: Long, window: Window) {
        DebugLogger.debug(TAG, "notifyPlay: $position")
        ComScoreActiveTracker.notifyUxActive(this)
        notifyPosition(position, window)
        streamingAnalytics.notifyPlay()
    }

    private fun notifyEnd() {
        DebugLogger.debug(TAG, "notifyEnd")
        ComScoreActiveTracker.notifyUxInactive(this)
        Analytics.notifyUxInactive()
    }

    private fun notifyBufferStart() {
        DebugLogger.debug(TAG, "notifyBufferStart")
        streamingAnalytics.notifyBufferStart()
    }

    /**
     * Notify position
     *
     * @param position
     * @param window
     */
    private fun notifyPosition(position: Long, window: Window) {
        if (!window.isLive()) {
            DebugLogger.debug(TAG, "notifyPosition $position")
            streamingAnalytics.startFromPosition(position)
        } else {
            notifyLiveInformation(position, window)
        }
    }

    private fun notifySeek() {
        DebugLogger.debug(TAG, "notifySeek")
        streamingAnalytics.notifySeekStart()
    }

    private fun notifyLiveInformation(position: Long, window: Window) {
        val length = if (window.isSeekable) window.durationMs else LIVE_ONLY_WINDOW_LENGTH
        val windowOffset = if (window.isSeekable) length - position else LIVE_ONLY_WINDOW_OFFSET
        DebugLogger.debug(TAG, "notifyLiveInformation offset = $windowOffset length = $length")
        streamingAnalytics.setDvrWindowLength(length)
        streamingAnalytics.startFromDvrWindowOffset(windowOffset)
    }

    /**
     * According to the comprehension of the documentation and after validation with ComScore team,
     * [StreamingAnalytics.notifyBufferStop] isn't required.
     *
     * No! We shall call it for case seeking while in pause.
     */
    private fun notifyBufferStop() {
        DebugLogger.debug(TAG, "notifyBufferStop: ")
        streamingAnalytics.notifyBufferStop()
    }

    private inner class PlayerComponent : AnalyticsListener {
        override fun onPlaybackParametersChanged(
            eventTime: AnalyticsListener.EventTime,
            playbackParameters: PlaybackParameters
        ) {
            streamingAnalytics.notifyChangePlaybackRate(playbackParameters.speed)
        }

        override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
            when (state) {
                Player.STATE_BUFFERING -> notifyBufferStart()
                Player.STATE_READY -> notifyBufferStop()
            }
        }

        override fun onPositionDiscontinuity(
            eventTime: AnalyticsListener.EventTime,
            oldPosition: Player.PositionInfo,
            newPosition: Player.PositionInfo,
            reason: Int
        ) {
            when (reason) {
                Player.DISCONTINUITY_REASON_SEEK, Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> {
                    notifySeek()
                    eventTime.timeline.getWindow(eventTime.windowIndex, window)
                    notifyPosition(newPosition.positionMs, window)
                }
            }
        }

        override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime, reason: Int) {
            if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
                eventTime.timeline.getWindow(eventTime.windowIndex, window)
                if (window.isLive()) {
                    notifyLiveInformation(eventTime.eventPlaybackPositionMs, window)
                }
            }
        }

        override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
            val position = eventTime.eventPlaybackPositionMs
            eventTime.timeline.getWindow(eventTime.windowIndex, window)
            if (isPlaying) {
                notifyPlay(position, window)
            } else {
                notifyPause()
            }
        }
    }

    /**
     * Factory
     */
    class Factory : MediaItemTracker.Factory {
        override fun create(): MediaItemTracker {
            return ComScoreTracker()
        }
    }

    companion object {
        private const val MEDIA_PLAYER_NAME = "Pillarbox"
        private const val TAG = "ComScoreTracker"
        private const val LIVE_ONLY_WINDOW_OFFSET = 0L
        private const val LIVE_ONLY_WINDOW_LENGTH = 0L
    }
}
