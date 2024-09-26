/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.common.Player.PositionInfo
import androidx.media3.common.Timeline.Window
import androidx.media3.common.Tracks
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.analytics.AnalyticsListener.EventTime
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import ch.srgssr.pillarbox.player.analytics.TotalPlaytimeCounter
import ch.srgssr.pillarbox.player.extension.hasAccessibilityRoles
import ch.srgssr.pillarbox.player.extension.isForced
import ch.srgssr.pillarbox.player.runOnApplicationLooper
import ch.srgssr.pillarbox.player.tracks.audioTracks
import ch.srgssr.pillarbox.player.utils.DebugLogger
import ch.srgssr.pillarbox.player.utils.Heartbeat
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

internal class CommandersActStreaming(
    private val commandersAct: CommandersAct,
    private val player: ExoPlayer,
    var currentData: CommandersActTracker.Data,
    coroutineContext: CoroutineContext,
) : AnalyticsListener {

    private enum class State {
        Idle, Playing, Paused, HasSeek
    }

    private val positionHeartbeat = Heartbeat(
        startDelay = HEART_BEAT_DELAY,
        period = POS_PERIOD,
        coroutineContext = coroutineContext,
        task = {
            player.runOnApplicationLooper {
                if (player.playWhenReady) {
                    notifyPos(player.currentPosition.milliseconds)
                }
            }
        },
    )

    private val uptimeHeartbeat = Heartbeat(
        startDelay = HEART_BEAT_DELAY,
        period = UPTIME_PERIOD,
        coroutineContext = coroutineContext,
        task = {
            player.runOnApplicationLooper {
                if (player.playWhenReady && player.isCurrentMediaItemLive) {
                    notifyUptime(player.currentPosition.milliseconds)
                }
            }
        },
    )

    private var state: State = State.Idle
    private val playtimeTracker = TotalPlaytimeCounter()
    private var oldPosition: PositionInfo? = null
    private var reachEoF = false
    private var currentTracks = player.currentTracks
    private val window = Window()

    init {
        player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
        if (player.isPlaying) {
            playtimeTracker.play()
            notifyPlaying()
        }
    }

    private fun startHeartBeat() {
        stopHeartBeat()

        positionHeartbeat.start()
        uptimeHeartbeat.start()
    }

    private fun stopHeartBeat() {
        positionHeartbeat.stop()
        uptimeHeartbeat.stop()
    }

    private fun notifyEvent(type: MediaEventType, position: Duration) {
        val totalPlayTime = playtimeTracker.getTotalPlayTime()
        DebugLogger.debug(TAG, "send : $type position = $position totalPlayTime = $totalPlayTime ${window.isLive}")
        val event = TCMediaEvent(eventType = type, assets = currentData.assets, sourceId = currentData.sourceId)
        handleTextTrackData(event)
        handleAudioTrack(event)

        if (window.isLive) {
            event.timeShift = getTimeshift(position)
        }
        val maxVolume = player.deviceInfo.maxVolume
        val minVolume = player.deviceInfo.minVolume
        val volumeRange = maxVolume - minVolume
        if (volumeRange == 0 || player.volume == 0f) {
            event.deviceVolume = player.volume
        } else {
            val deviceVolume = player.deviceVolume - minVolume
            event.deviceVolume = deviceVolume / volumeRange.toFloat()
        }

        event.mediaPosition = if (window.isLive) totalPlayTime else position
        commandersAct.sendTcMediaEvent(event)
    }

    private fun notifyPlaying() {
        if (state == State.Playing) return
        this.state = State.Playing
        notifyEvent(MediaEventType.Play, player.currentPosition.milliseconds)
        startHeartBeat()
    }

    private fun notifyPause() {
        if (state != State.Playing) return
        this.state = State.Paused
        notifyEvent(MediaEventType.Pause, player.currentPosition.milliseconds)
        stopHeartBeat()
    }

    fun stop() {
        val position = oldPosition?.positionMs ?: player.currentPosition
        notifyStop(position.milliseconds, reachEoF)
        oldPosition = null
        reachEoF = false
    }

    fun notifyStop(position: Duration, isEoF: Boolean = false) {
        stopHeartBeat()
        if (state == State.Idle) return
        this.state = State.Idle
        notifyEvent(if (isEoF) MediaEventType.Eof else MediaEventType.Stop, position)
    }

    private fun notifySeek(seekStartPosition: Duration) {
        if (state != State.Playing) return
        state = State.HasSeek
        notifyEvent(MediaEventType.Seek, seekStartPosition)
    }

    private fun notifyPos(position: Duration) {
        notifyEvent(MediaEventType.Pos, position)
    }

    private fun notifyUptime(position: Duration) {
        notifyEvent(MediaEventType.Uptime, position)
    }

    private fun getTimeshift(position: Duration): Duration {
        return if (position == ZERO) ZERO else window.durationMs.milliseconds - position
    }

    private fun isPlaying(): Boolean {
        return player.playWhenReady && (player.playbackState == Player.STATE_READY || player.playbackState == Player.STATE_BUFFERING)
    }

    /**
     * Handle text track data
     *  MEDIA_SUBTITLES_ON to true if text track selected but not forced, false otherwise
     *  MEDIA_SUBTITLE_SELECTION the language name of the currently selected track
     */
    @Suppress("SwallowedException")
    private fun handleTextTrackData(event: TCMediaEvent) {
        try {
            val selectedTextGroup = currentTracks.groups.first {
                it.type == C.TRACK_TYPE_TEXT && it.isSelected
            }
            val selectedFormat: Format = selectedTextGroup.getTrackFormat(0)
            if (selectedFormat.isForced()) {
                event.isSubtitlesOn = false
                event.subtitleSelectionLanguage = null
            } else {
                event.subtitleSelectionLanguage = selectedFormat.language ?: C.LANGUAGE_UNDETERMINED
                event.isSubtitlesOn = true
            }
        } catch (_: NoSuchElementException) {
            event.isSubtitlesOn = false
            event.subtitleSelectionLanguage = null
        }
    }

    private fun handleAudioTrack(event: TCMediaEvent) {
        val currentAudioTrack = currentTracks.audioTracks.find { it.isSelected }
        val audioTrackLanguage = currentAudioTrack
            ?.format
            ?.language
            ?: C.LANGUAGE_UNDETERMINED

        event.audioTrackLanguage = audioTrackLanguage

        event.audioTrackHasAudioDescription = currentAudioTrack?.format?.hasAccessibilityRoles() ?: false
    }

    override fun onIsPlayingChanged(eventTime: EventTime, isPlaying: Boolean) {
        if (isPlaying) {
            playtimeTracker.play()
        } else {
            playtimeTracker.pause()
        }
    }

    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        if (events.containsAny(AnalyticsListener.EVENT_PLAYBACK_STATE_CHANGED, AnalyticsListener.EVENT_PLAY_WHEN_READY_CHANGED)) {
            if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) return
            if (player.playWhenReady) {
                notifyPlaying()
            } else {
                notifyPause()
            }
        }
    }

    override fun onPlaybackStateChanged(
        eventTime: EventTime,
        @Player.State playbackState: Int,
    ) {
        when (playbackState) {
            Player.STATE_ENDED, Player.STATE_IDLE -> {
                reachEoF = playbackState == Player.STATE_ENDED
                oldPosition = null
                stop()
            }

            Player.STATE_READY -> {
                if (player.playWhenReady) {
                    notifyPlaying()
                }
            }

            else -> Unit
        }
    }

    override fun onPositionDiscontinuity(
        eventTime: EventTime,
        oldPosition: PositionInfo,
        newPosition: PositionInfo,
        reason: Int
    ) {
        when (reason) {
            Player.DISCONTINUITY_REASON_SEEK, Player.DISCONTINUITY_REASON_SEEK_ADJUSTMENT -> {
                if (oldPosition.mediaItemIndex != newPosition.mediaItemIndex) {
                    this.oldPosition = oldPosition
                } else if (isPlaying() && abs(oldPosition.positionMs - newPosition.positionMs) > VALID_SEEK_THRESHOLD) {
                    this.oldPosition = null
                    notifySeek(oldPosition.positionMs.milliseconds)
                }
            }

            Player.DISCONTINUITY_REASON_AUTO_TRANSITION, Player.DISCONTINUITY_REASON_REMOVE -> {
                this.oldPosition = oldPosition
            }
        }
    }

    override fun onMediaItemTransition(eventTime: EventTime, mediaItem: MediaItem?, reason: Int) {
        reachEoF = reason <= Player.MEDIA_ITEM_TRANSITION_REASON_AUTO
        when (reason) {
            Player.MEDIA_ITEM_TRANSITION_REASON_REPEAT -> {
                stop()
                notifyPlaying()
            }
        }
    }

    override fun onTimelineChanged(eventTime: EventTime, reason: Int) {
        if (reason == Player.TIMELINE_CHANGE_REASON_SOURCE_UPDATE) {
            player.currentTimeline.getWindow(player.currentMediaItemIndex, window)
        }
    }

    override fun onTracksChanged(eventTime: EventTime, tracks: Tracks) {
        currentTracks = tracks
    }

    companion object {
        private const val TAG = "CommandersActTracker"

        internal var HEART_BEAT_DELAY = 30.seconds
        internal var UPTIME_PERIOD = 60.seconds
        internal var POS_PERIOD = 30.seconds
        private const val VALID_SEEK_THRESHOLD: Long = 1000L
    }
}
