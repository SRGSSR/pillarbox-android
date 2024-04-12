/*
 * Copyright (c) SRG SSR. All rights reserved.
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
import ch.srgssr.pillarbox.player.extension.isForced
import ch.srgssr.pillarbox.player.tracks.audioTracks
import ch.srgssr.pillarbox.player.utils.DebugLogger
import kotlinx.coroutines.CoroutineName
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Deferred
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.Job
import kotlinx.coroutines.async
import kotlinx.coroutines.awaitAll
import kotlinx.coroutines.delay
import kotlinx.coroutines.isActive
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import kotlin.coroutines.CoroutineContext
import kotlin.math.abs
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

@Suppress("MagicNumber", "TooManyFunctions")
internal class CommandersActStreaming(
    private val commandersAct: CommandersAct,
    private val player: ExoPlayer,
    var currentData: CommandersActTracker.Data,
    private val coroutineContext: CoroutineContext,
) : AnalyticsListener {

    private enum class State {
        Idle, Playing, Paused, HasSeek
    }

    private var state: State = State.Idle
    private var heartBeatJob: Job? = null
    private val playtimeTracker = TotalPlaytimeCounter()

    init {
        if (player.isPlaying) {
            playtimeTracker.play()
            notifyPlaying()
        }
    }

    private fun startHeartBeat() {
        stopHeartBeat()

        heartBeatJob = CoroutineScope(coroutineContext).launch(CoroutineName("pillarbox-heart-beat")) {
            val posUpdate = periodicTask(
                period = POS_PERIOD,
                task = ::notifyPos,
            )
            val uptimeUpdate = periodicTask(
                period = UPTIME_PERIOD,
                continueLooping = { runOnMain(player::isCurrentMediaItemLive) },
                task = ::notifyUptime,
            )

            awaitAll(posUpdate, uptimeUpdate)
        }
    }

    private fun CoroutineScope.periodicTask(
        period: Duration,
        continueLooping: () -> Boolean = { true },
        task: (currentPosition: Duration) -> Unit
    ): Deferred<Unit> {
        return async {
            delay(HEART_BEAT_DELAY)

            while (isActive && continueLooping()) {
                runOnMain {
                    if (player.playWhenReady) {
                        task(player.currentPosition.milliseconds)
                    }
                }

                delay(period)
            }
        }
    }

    private fun <T> runOnMain(callback: () -> T): T {
        return runBlocking(Dispatchers.Main) {
            callback()
        }
    }

    private fun stopHeartBeat() {
        heartBeatJob?.cancel()
        heartBeatJob = null
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
            if (player.playbackState == Player.STATE_IDLE || player.playbackState == Player.STATE_ENDED) return
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
        if (!isPlaying()) return
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
        if (state != State.Playing) return
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
        return if (position == ZERO) ZERO else player.duration.milliseconds - position
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
            val selectedTextGroup = player.currentTracks.groups.first {
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
        } catch (e: NoSuchElementException) {
            event.isSubtitlesOn = false
            event.subtitleSelectionLanguage = null
        }
    }

    private fun handleAudioTrack(event: TCMediaEvent) {
        val audioTrackLanguage = player.currentTracks
            .audioTracks
            .find { it.isSelected }
            ?.format
            ?.language
            ?: C.LANGUAGE_UNDETERMINED

        event.audioTrackLanguage = audioTrackLanguage
    }

    companion object {
        private const val TAG = "CommandersActTracker"

        internal var HEART_BEAT_DELAY = 30.seconds
        internal var UPTIME_PERIOD = 60.seconds
        internal var POS_PERIOD = 30.seconds
        private const val VALID_SEEK_THRESHOLD: Long = 1000L
    }
}
