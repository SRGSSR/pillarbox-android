/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.Allocator
import androidx.media3.exoplayer.upstream.DefaultAllocator
import kotlin.time.Duration
import kotlin.time.Duration.Companion.milliseconds
import kotlin.time.Duration.Companion.seconds

/**
 * Experimental [LoadControl] implementation that optimize content loading for smooth seeking.
 *
 * @param bufferDurations Buffer duration when [smoothSeeking] is not enabled.
 * @property smoothSeeking If enabled, use an optimized [LoadControl].
 * @param allocator The [DefaultAllocator] to use in the internal [DefaultLoadControl].
 */
class TrickPlayLoadControl(
    bufferDurations: BufferDurations = BufferDurations(),
    var smoothSeeking: Boolean = false,
    private val allocator: DefaultAllocator = DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),
) : LoadControl {

    private val fastSeekLoadControl: DefaultLoadControl = DefaultLoadControl.Builder()
        .setAllocator(allocator)
        .setDurations(FAST_SEEK_DURATIONS)
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()
    private val defaultLoadControl: DefaultLoadControl = DefaultLoadControl.Builder()
        .setAllocator(allocator)
        .setDurations(bufferDurations)
        .setPrioritizeTimeOverSizeThresholds(true)
        .build()
    private val activeLoadControl: LoadControl
        get() {
            return if (smoothSeeking) fastSeekLoadControl else defaultLoadControl
        }

    override fun onPrepared() {
        fastSeekLoadControl.onPrepared()
        defaultLoadControl.onPrepared()
    }

    override fun onStopped() {
        fastSeekLoadControl.onStopped()
        defaultLoadControl.onStopped()
    }

    override fun onReleased() {
        fastSeekLoadControl.onReleased()
        defaultLoadControl.onReleased()
    }

    override fun getAllocator(): Allocator {
        return allocator
    }

    override fun getBackBufferDurationUs(): Long {
        return BACK_BUFFER_DURATION_MS
    }

    override fun retainBackBufferFromKeyframe(): Boolean {
        return true
    }

    override fun shouldContinueLoading(
        playbackPositionUs: Long,
        bufferedDurationUs: Long,
        playbackSpeed: Float
    ): Boolean {
        return activeLoadControl.shouldContinueLoading(playbackPositionUs, bufferedDurationUs, playbackSpeed)
    }

    override fun onTracksSelected(
        timeline: Timeline,
        mediaPeriodId: MediaSource.MediaPeriodId,
        renderers: Array<out Renderer>,
        trackGroups: TrackGroupArray,
        trackSelections: Array<out ExoTrackSelection>
    ) {
        fastSeekLoadControl.onTracksSelected(timeline, mediaPeriodId, renderers, trackGroups, trackSelections)
        defaultLoadControl.onTracksSelected(timeline, mediaPeriodId, renderers, trackGroups, trackSelections)
    }

    @Deprecated("Deprecated in Java")
    override fun onTracksSelected(
        renderers: Array<out Renderer>,
        trackGroups: TrackGroupArray,
        trackSelections: Array<out ExoTrackSelection>
    ) {
        fastSeekLoadControl.onTracksSelected(renderers, trackGroups, trackSelections)
        defaultLoadControl.onTracksSelected(renderers, trackGroups, trackSelections)
    }

    override fun shouldStartPlayback(
        timeline: Timeline,
        mediaPeriodId: MediaSource.MediaPeriodId,
        bufferedDurationUs: Long,
        playbackSpeed: Float,
        rebuffering: Boolean,
        targetLiveOffsetUs: Long
    ): Boolean {
        return activeLoadControl.shouldStartPlayback(timeline, mediaPeriodId, bufferedDurationUs, playbackSpeed, rebuffering, targetLiveOffsetUs)
    }

    @Deprecated("Deprecated in Java")
    override fun shouldStartPlayback(
        bufferedDurationUs: Long,
        playbackSpeed: Float,
        rebuffering: Boolean,
        targetLiveOffsetUs: Long
    ): Boolean {
        return activeLoadControl.shouldStartPlayback(bufferedDurationUs, playbackSpeed, rebuffering, targetLiveOffsetUs)
    }

    /**
     * Buffer durations to use for [DefaultLoadControl.Builder.setBufferDurationsMs].
     *
     * @property minBufferDuration The minimum duration of media that the player will attempt to ensure is buffered at all times.
     * @property maxBufferDuration The maximum duration of media that the player will attempt to buffer.
     * @property bufferForPlayback The duration of media that must be buffered for playback to start or resume following a user action such as a seek.
     * @property bufferForPlaybackAfterRebuffer The default duration of media that must be buffered for playback to resume after a rebuffer.
     * A rebuffer is defined to be caused by buffer depletion rather than a user action.
     * @constructor Create empty Buffer durations
     */
    data class BufferDurations(
        val minBufferDuration: Duration = DefaultLoadControl.DEFAULT_MIN_BUFFER_MS.milliseconds,
        val maxBufferDuration: Duration = DefaultLoadControl.DEFAULT_MAX_BUFFER_MS.milliseconds,
        val bufferForPlayback: Duration = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_MS.milliseconds,
        val bufferForPlaybackAfterRebuffer: Duration = DefaultLoadControl.DEFAULT_BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS.milliseconds,
    )

    private companion object SmoothLoadControl {
        private const val BACK_BUFFER_DURATION_MS = 6_000L
        private val FAST_SEEK_DURATIONS = BufferDurations(
            minBufferDuration = 2.seconds,
            maxBufferDuration = 2.seconds,
            bufferForPlayback = 2.seconds,
            bufferForPlaybackAfterRebuffer = 2.seconds,
        )

        private fun DefaultLoadControl.Builder.setDurations(durations: BufferDurations): DefaultLoadControl.Builder {
            return setBufferDurationsMs(
                durations.minBufferDuration.inWholeMilliseconds.toInt(),
                durations.maxBufferDuration.inWholeMilliseconds.toInt(),
                durations.bufferForPlayback.inWholeMilliseconds.toInt(),
                durations.bufferForPlaybackAfterRebuffer.inWholeMilliseconds.toInt(),
            )
        }
    }
}
