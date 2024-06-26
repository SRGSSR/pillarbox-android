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
 * Pillarbox [LoadControl] implementation that optimize content loading.
 *
 * @param bufferDurations Buffer durations to set [DefaultLoadControl.Builder.setBufferDurationsMs].
 * @param allocator The [DefaultAllocator] to use in the internal [DefaultLoadControl].
 */
class PillarboxLoadControl(
    bufferDurations: BufferDurations = DEFAULT_BUFFER_DURATIONS,
    private val allocator: DefaultAllocator = DefaultAllocator(true, C.DEFAULT_BUFFER_SEGMENT_SIZE),
) : LoadControl {

    private val defaultLoadControl: DefaultLoadControl = DefaultLoadControl.Builder()
        .setAllocator(allocator)
        .setBufferDurationsMs(
            bufferDurations.minBufferDuration.inWholeMilliseconds.toInt(),
            bufferDurations.maxBufferDuration.inWholeMilliseconds.toInt(),
            bufferDurations.bufferForPlayback.inWholeMilliseconds.toInt(),
            bufferDurations.bufferForPlaybackAfterRebuffer.inWholeMilliseconds.toInt(),
        )
        .setPrioritizeTimeOverSizeThresholds(true)
        .setBackBuffer(BACK_BUFFER_DURATION_MS, true)
        .build()

    override fun onPrepared() {
        defaultLoadControl.onPrepared()
    }

    override fun onStopped() {
        defaultLoadControl.onStopped()
    }

    override fun onReleased() {
        defaultLoadControl.onReleased()
    }

    override fun getAllocator(): Allocator {
        return allocator
    }

    override fun getBackBufferDurationUs(): Long {
        return defaultLoadControl.backBufferDurationUs
    }

    override fun retainBackBufferFromKeyframe(): Boolean {
        return defaultLoadControl.retainBackBufferFromKeyframe()
    }

    override fun shouldContinueLoading(
        playbackPositionUs: Long,
        bufferedDurationUs: Long,
        playbackSpeed: Float
    ): Boolean {
        return defaultLoadControl.shouldContinueLoading(playbackPositionUs, bufferedDurationUs, playbackSpeed)
    }

    override fun onTracksSelected(
        timeline: Timeline,
        mediaPeriodId: MediaSource.MediaPeriodId,
        renderers: Array<out Renderer>,
        trackGroups: TrackGroupArray,
        trackSelections: Array<out ExoTrackSelection>
    ) {
        defaultLoadControl.onTracksSelected(timeline, mediaPeriodId, renderers, trackGroups, trackSelections)
    }

    override fun shouldStartPlayback(
        timeline: Timeline,
        mediaPeriodId: MediaSource.MediaPeriodId,
        bufferedDurationUs: Long,
        playbackSpeed: Float,
        rebuffering: Boolean,
        targetLiveOffsetUs: Long
    ): Boolean {
        return defaultLoadControl.shouldStartPlayback(timeline, mediaPeriodId, bufferedDurationUs, playbackSpeed, rebuffering, targetLiveOffsetUs)
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

    private companion object {
        private const val BACK_BUFFER_DURATION_MS = 4_000
        private val DEFAULT_BUFFER_DURATIONS = BufferDurations(
            bufferForPlayback = 500.milliseconds,
            bufferForPlaybackAfterRebuffer = 1.seconds,
            minBufferDuration = 1.seconds,
        )
    }
}
