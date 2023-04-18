/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.tracker.commandersact

import android.os.SystemClock
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import ch.srgssr.pillarbox.analytics.BuildConfig
import ch.srgssr.pillarbox.analytics.commandersact.CommandersAct
import ch.srgssr.pillarbox.player.utils.DebugLogger
import com.tagcommander.lib.serverside.events.TCCustomEvent
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.MainScope
import kotlinx.coroutines.launch
import java.util.*
import kotlin.concurrent.fixedRateTimer
import kotlin.concurrent.scheduleAtFixedRate
import kotlin.math.abs
import kotlin.math.roundToLong
import kotlin.time.Duration
import kotlin.time.Duration.Companion.ZERO
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds
import kotlin.time.DurationUnit

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
    private var totalPlayTime = 0.seconds
    private var lastTotalPlayTimeUpdate = 0L
    private var hardBeatTimer: Timer? = null

    init {
        if (player.isPlaying) {
            updateTotalPlayTime(true)
            notifyPlaying()
        }
    }

    private fun startHardBeat() {
        stopHardBeat()
        val startTime = System.currentTimeMillis()
        hardBeatTimer =
            fixedRateTimer(
                name = "pillarbox-hard-beat", false, initialDelay = HARD_BEAT_DELAY.inWholeMilliseconds,
                period = POS_PERIOD
                    .inWholeMilliseconds
            ) {
                MainScope().launch(Dispatchers.Main) {
                    updateTotalPlayTime(player.isPlaying)
                    Log.d("Coucou", "Notify = ${(System.currentTimeMillis() - startTime).milliseconds} ${player.currentPosition.milliseconds}")
                    notifyPos(player.currentPosition.milliseconds)
                }
            }.also {
                it.scheduleAtFixedRate(HARD_BEAT_DELAY.inWholeMilliseconds, period = UPTIME_PERIOD.inWholeMilliseconds) {
                    MainScope().launch(Dispatchers.Main) {
                        notifyUptime(player.currentPosition.milliseconds)
                    }
                }
            }
    }

    private fun stopHardBeat() {
        hardBeatTimer?.cancel()
        hardBeatTimer = null
    }

    private fun updateTotalPlayTime(isPlaying: Boolean) {
        if (!isPlaying) {
            lastTotalPlayTimeUpdate = 0
            return
        }
        val uptime = SystemClock.uptimeMillis()
        if (lastTotalPlayTimeUpdate > 0) {
            totalPlayTime += uptime.milliseconds - lastTotalPlayTimeUpdate.milliseconds
        }
        lastTotalPlayTimeUpdate = uptime
    }

    override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
        updateTotalPlayTime(isPlaying)
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

    private fun notifyEvent(name: String, position: Duration) {
        DebugLogger.debug(TAG, "send : $name position = $position")
        val event = TCCustomEvent(name)
        event.addAll(currentData.assets)
        currentData.sourceId?.let {
            event.addAdditionalParameter(KEY_SOURCE_ID, it)
        }
        handleTextTrackData(event)
        handleAudioTrack(event)

        if (player.isCurrentMediaItemLive) {
            var timeShift = getTimeshift(position)
            if (timeShift < TIMESHIFT_EQUIVALENT_LIVE) {
                timeShift = ZERO
            }
            event.addAdditionalParameter(MEDIA_TIMESHIFT, toSeconds(timeShift).toString())
        }

        player.getCurrentBandwidth()?.let { bandwidth ->
            event.addAdditionalParameter(MEDIA_BANDWIDTH, bandwidth.toString())
        }
        event.addAdditionalParameter(MEDIA_VOLUME, (player.deviceVolume / 100).toString())
        event.addAdditionalParameter(
            MEDIA_POSITION,
            if (player.isCurrentMediaItemLive) toSeconds(totalPlayTime).toString() else toSeconds(position).toString()
        )
        event.addAdditionalParameter(MEDIA_PLAYER_VERSION, BuildConfig.VERSION_NAME)
        event.addAdditionalParameter(MEDIA_PLAYER_DISPLAY, "Pillarbox")
        commandersAct.sendTcEvent(event)
    }

    private fun notifyPlaying() {
        if (state == State.Playing) return
        this.state = State.Playing
        notifyEvent(EVENT_PLAY, player.currentPosition.milliseconds)
        startHardBeat()
    }

    private fun notifyPause() {
        if (state == State.Idle) return
        this.state = State.Paused
        notifyEvent(EVENT_PAUSE, player.currentPosition.milliseconds)
        stopHardBeat()
    }

    fun notifyStop(isEoF: Boolean = false) {
        stopHardBeat()
        if (state == State.Idle) return
        this.state = State.Idle
        notifyEvent(if (isEoF) EVENT_EOF else EVENT_STOP, player.currentPosition.milliseconds)
    }

    private fun notifySeek(seekStartPosition: Duration) {
        if (state == State.Seeking || state == State.Idle) return
        state = State.Seeking
        notifyEvent(EVENT_SEEK, seekStartPosition)
    }

    private fun notifyPos(position: Duration) {
        notifyEvent(EVENT_POS, position)
    }

    private fun notifyUptime(position: Duration) {
        if (!isCurrentlyLiveAt(position)) return
        notifyEvent(EVENT_UPTIME, position)
    }

    private fun isCurrentlyLiveAt(position: Duration): Boolean {
        return player.isCurrentMediaItemLive && getTimeshift(position) < TIMESHIFT_EQUIVALENT_LIVE
    }

    private fun getTimeshift(position: Duration): Duration {
        return if (position == ZERO) ZERO else player.duration.milliseconds - position
    }

    /**
     * Handle text track data
     *  MEDIA_SUBTITLES_ON to true if text track selected but not forced, false otherwise
     *  MEDIA_SUBTITLE_SELECTION the language name of the currently selected track
     */
    private fun handleTextTrackData(event: TCCustomEvent) {
        // TODO handle text track analytics
        val currentTextTrack: Format? = null
        val isSubtitlesOn: Boolean = currentTextTrack?.let {
            // TODO retrieve the language
            event.addAdditionalParameter(MEDIA_SUBTITLE_SELECTION, VALUE_UNKNOWN_LANGUAGE)
            (it?.selectionFlags ?: 0 and C.SELECTION_FLAG_FORCED) != C.SELECTION_FLAG_FORCED
        } ?: false
        event.addAdditionalParameter(MEDIA_SUBTITLES_ON, isSubtitlesOn.toString())
    }

    private fun handleAudioTrack(event: TCCustomEvent) {
        // TODO handle Audio track analytics
        val currentAudioTrack: Format? = null
        currentAudioTrack?.let { track ->
            // TODO retrieve the language
            event.addAdditionalParameter(MEDIA_AUDIO_TRACK, VALUE_UNKNOWN_LANGUAGE)
        }
    }

    companion object {
        private const val TAG = "CommandersActTracker"
        const val EVENT_PLAY = "play"
        const val EVENT_PAUSE = "pause"
        const val EVENT_EOF = "eof"
        const val EVENT_STOP = "stop"
        const val EVENT_SEEK = "seek"
        const val EVENT_POS = "pos"
        const val EVENT_UPTIME = "uptime"

        const val MEDIA_PLAYER_VERSION = "media_player_version"
        const val MEDIA_VOLUME = "media_volume"
        const val MEDIA_POSITION = "media_position"
        const val MEDIA_PLAYER_DISPLAY = "media_player_display"
        const val MEDIA_TIMESHIFT = "media_timeshift"
        const val MEDIA_BANDWIDTH = "media_bandwidth"
        const val MEDIA_SUBTITLES_ON = "media_subtitles_on"
        const val MEDIA_AUDIO_TRACK = "media_audio_track"
        const val MEDIA_SUBTITLE_SELECTION = "media_subtitle_selection"
        const val KEY_SOURCE_ID = "source_id"
        const val VALUE_UNKNOWN_LANGUAGE = "UND"

        internal var HARD_BEAT_DELAY = 30.seconds
        internal var UPTIME_PERIOD = 60.seconds
        internal var POS_PERIOD = 30.seconds
        private val TIMESHIFT_EQUIVALENT_LIVE = 60.seconds
        private const val VALID_SEEK_THRESHOLD: Long = 1000L

        private fun TCCustomEvent.addAll(map: Map<String, String>) {
            for (entry in map) {
                addAdditionalParameter(entry.key, entry.value)
            }
        }

        private fun toSeconds(duration: Duration): Long {
            return duration.toDouble(DurationUnit.SECONDS).roundToLong()
        }
    }
}

/**
 * Compute bit rate
 * @return null if not applicable
 */
private fun ExoPlayer.getCurrentBandwidth(): Long? {
    val videoBandwidth = if (videoFormat != null && videoFormat!!.bitrate != Format.NO_VALUE) videoFormat!!.bitrate else 0
    val audioBandwidth = if (audioFormat != null && audioFormat!!.bitrate != Format.NO_VALUE) audioFormat!!.bitrate else 0
    val bandwidth = (videoBandwidth + audioBandwidth).toLong()
    return if (bandwidth > 0) bandwidth else null
}
