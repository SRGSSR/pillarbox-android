/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.Timeline
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.Allocator

/**
 * Proxy [LoadControl] that will use a custom [LoadControl] if `smoothSeeking` is `true`, or the provided `defaultLoadControl` otherwise.
 *
 * @param smoothSeeking `true` to use a custom [LoadControl] adapted for smooth seeking, `false` to use the provided [LoadControl].
 * @param defaultLoadControl The [LoadControl] to use if `smoothSeeking` is `false`.
 */
class PillarboxLoadControl(
    smoothSeeking: Boolean = false,
    defaultLoadControl: LoadControl = DefaultLoadControl()
) : LoadControl {
    private val activeLoadControl: LoadControl = if (smoothSeeking) SmoothLoadControl() else defaultLoadControl

    override fun onPrepared() {
        activeLoadControl.onPrepared()
    }

    override fun onStopped() {
        activeLoadControl.onStopped()
    }

    override fun onReleased() {
        activeLoadControl.onReleased()
    }

    override fun getAllocator(): Allocator {
        return activeLoadControl.allocator
    }

    override fun getBackBufferDurationUs(): Long {
        return activeLoadControl.backBufferDurationUs
    }

    override fun retainBackBufferFromKeyframe(): Boolean {
        return activeLoadControl.retainBackBufferFromKeyframe()
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
        activeLoadControl.onTracksSelected(timeline, mediaPeriodId, renderers, trackGroups, trackSelections)
    }

    @Deprecated("Deprecated in Java")
    override fun onTracksSelected(
        renderers: Array<out Renderer>,
        trackGroups: TrackGroupArray,
        trackSelections: Array<out ExoTrackSelection>
    ) {
        activeLoadControl.onTracksSelected(renderers, trackGroups, trackSelections)
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

    private companion object SmoothLoadControl {
        private const val MIN_BUFFER_MS = 2_000
        private const val MAX_BUFFER_MS = 2_000
        private const val BUFFER_FOR_PLAYBACK_MS = 2_000
        private const val BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS = 2_000
        private const val BACK_BUFFER_DURATION_MS = 2_000

        operator fun invoke(): LoadControl {
            return DefaultLoadControl.Builder()
                .setPrioritizeTimeOverSizeThresholds(true)
                .setBufferDurationsMs(
                    MIN_BUFFER_MS,
                    MAX_BUFFER_MS,
                    BUFFER_FOR_PLAYBACK_MS,
                    BUFFER_FOR_PLAYBACK_AFTER_REBUFFER_MS
                )
                .setBackBuffer(BACK_BUFFER_DURATION_MS, true)
                .build()
        }
    }
}
