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

class PillarboxLoadControl(smoothSeeking: Boolean = false, defaultLoadControl: LoadControl = DefaultLoadControl()) : LoadControl {
    private var activeLoadControl: LoadControl = if (smoothSeeking) smoothSeekingLoadControl() else defaultLoadControl

    override
    fun onPrepared() {
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

    override fun shouldContinueLoading(playbackPositionUs: Long, bufferedDurationUs: Long, playbackSpeed: Float): Boolean {
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

    override fun shouldStartPlayback(bufferedDurationUs: Long, playbackSpeed: Float, rebuffering: Boolean, targetLiveOffsetUs: Long): Boolean {
        return activeLoadControl.shouldStartPlayback(bufferedDurationUs, playbackSpeed, rebuffering, targetLiveOffsetUs)
    }

    companion object {
        /**
         * Smooth seeking load control
         * TODO : Move that in a Class
         */
        fun smoothSeekingLoadControl() = DefaultLoadControl.Builder()
            .setPrioritizeTimeOverSizeThresholds(true)
            .setBufferDurationsMs(
                2_000, // DefaultLoadControl.DEFAULT_MIN_BUFFER_MS,
                2_000, // DefaultLoadControl.DEFAULT_MAX_BUFFER_MS,
                2_000,
                2_000
            )
            .setBackBuffer(2_000, true)
            .build()
    }
}
