/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.analytics.commandersact.MediaEventType
import ch.srgssr.pillarbox.analytics.commandersact.TCMediaEvent
import ch.srgssr.pillarbox.core.business.tracker.TotalPlaytimeCounter
import ch.srgssr.pillarbox.player.utils.DebugLogger
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.Timer
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Suppress("MagicNumber", "TooManyFunctions")
internal class CommandersActStreaming(
    private val commandersAct: CommandersAct,
    private val player: ExoPlayer,
    var currentData: CommandersActTracker.Data
) : AnalyticsListener {

    private enum class State {
        Idle, Playing, Paused, Seeking
    }

    private var state: State = State.Idle
    private var heartBeatTimer: Timer? = null
    private val playtimeTracker = TotalPlaytimeCounter()

    init {
        if (player.isPlaying) {
            playtimeTracker.play()
            notifyPlaying()
        }
    }

    private fun startHeartBeat() {
        stopHeartBeat()
        heartBeatTimer =
            fixedRateTimer(
                name = "pillarbox-heart-beat", false, initialDelay = HEART_BEAT_DELAY.inWholeMilliseconds,
                period = POS_PERIOD.inWholeMilliseconds
            ) {
                MainScope().launch(Dispatchers.Main) {
                    notifyPos(player.currentPosition.milliseconds)
                }
            }.also {
                if (!player.isCurrentMediaItemLive) return@also
                it.scheduleAtFixedRate(HEART_BEAT_DELAY.inWholeMilliseconds, period = UPTIME_PERIOD.inWholeMilliseconds) {
                    MainScope().launch(Dispatchers.Main) {
                        notifyUptime(player.currentPosition.milliseconds)
                    }
                }
            }
    }

    private fun stopHeartBeat() {
        heartBeatTimer?.cancel()
        heartBeatTimer = null
    }

    override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
        if (isPlaying) {
            playtimeTracker.play()
        } else {
            playtimeTracker.pause()
        }
    }

    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        if (events.containsAny(AnalyticsListener.EVENT_PLAYBACK_STATE_CHANGED, AnalyticsListener.EVENT_PLAY_WHEN_READY_CHANGED)) {
            if (player.playbackState != Player.STATE_READY) return
            if (player.playWhenReady) {
                notifyPlaying()
            } else {
                notifyPause()
            }
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
                if (abs(oldPosition.positionMs - newPosition.positionMs) > VALID_SEEK_THRESHOLD) {
                    notifySeek(oldPosition.positionMs.milliseconds)
                }
            }
        }
    }

    private fun notifyEvent(type: MediaEventType, position: Duration) {
        val totalPlayTime = playtimeTracker.getTotalPlayTime()
        DebugLogger.debug(TAG, "send : $type position = $position totalPlayTime = $totalPlayTime")
        val event = TCMediaEvent(eventType = type, assets = currentData.assets, sourceId = currentData.sourceId)
        handleTextTrackData(event)
        handleAudioTrack(event)

        if (player.isCurrentMediaItemLive) {
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

        event.mediaPosition = if (player.isCurrentMediaItemLive) totalPlayTime else position
        commandersAct.sendTcMediaEvent(event)
    }

    private fun notifyPlaying() {
        if (state == State.Playing) return
        this.state = State.Playing
        notifyEvent(MediaEventType.Play, player.currentPosition.milliseconds)
        startHeartBeat()
    }

    private fun notifyPause() {
        if (state == State.Idle) return
        this.state = State.Paused
        notifyEvent(MediaEventType.Pause, player.currentPosition.milliseconds)
        stopHeartBeat()
    }

    fun notifyStop(position: Duration, isEoF: Boolean = false) {
        stopHeartBeat()
        if (state == State.Idle) return
        this.state = State.Idle
        notifyEvent(if (isEoF) MediaEventType.Eof else MediaEventType.Stop, position)
    }

    private fun notifySeek(seekStartPosition: Duration) {
        if (state == State.Seeking || state == State.Idle) return
        state = State.Seeking
        notifyEvent(MediaEventType.Seek, seekStartPosition)
    }

    private fun notifyPos(position: Duration) {
        notifyEvent(MediaEventType.Pos, position)
    }

    private fun notifyUptime(position: Duration) {
        if (getTimeshift(position) > LIVE_EDGE_THRESHOLD) return
        notifyEvent(MediaEventType.Uptime, position)
    }

    private fun getTimeshift(position: Duration): Duration {
        return if (position == ZERO) ZERO else player.duration.milliseconds - position
    }

    /**
     * Handle text track data
     *  MEDIA_SUBTITLES_ON to true if text track selected but not forced, false otherwise
     *  MEDIA_SUBTITLE_SELECTION the language name of the currently selected track
     */
    private fun handleTextTrackData(event: TCMediaEvent) {
        // TODO handle text track analytics
        val currentTextTrack: Format? = null
        val isSubtitlesOn: Boolean = currentTextTrack?.let {
            // TODO retrieve the language
            event.subtitleSelectionLanguage = VALUE_UNKNOWN_LANGUAGE
            (it?.selectionFlags ?: 0 and C.SELECTION_FLAG_FORCED) != C.SELECTION_FLAG_FORCED
        } ?: false
        event.isSubtitlesOn = isSubtitlesOn
    }

    private fun handleAudioTrack(event: TCMediaEvent) {
        // TODO handle Audio track analytics
        val currentAudioTrack: Format? = null
        currentAudioTrack?.let { track ->
            // TODO retrieve the language
            event.audioTrackLanguage = VALUE_UNKNOWN_LANGUAGE
        }
    }

    companion object {
        private const val TAG = "CommandersActTracker"
        const val VALUE_UNKNOWN_LANGUAGE = "UND"

        internal var HEART_BEAT_DELAY = 30.seconds
        internal var UPTIME_PERIOD = 60.seconds
        internal var POS_PERIOD = 30.seconds
        private const val VALID_SEEK_THRESHOLD: Long = 1000L
        private val LIVE_EDGE_THRESHOLD = 60.seconds
    }
}
