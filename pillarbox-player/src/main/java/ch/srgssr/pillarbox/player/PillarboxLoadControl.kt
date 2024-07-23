/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.common.C.TrackType
import androidx.media3.common.Timeline
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.LoadControl
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.analytics.PlayerId
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.TrackGroupArray
import androidx.media3.exoplayer.trackselection.ExoTrackSelection
import androidx.media3.exoplayer.upstream.Allocator
import androidx.media3.exoplayer.upstream.DefaultAllocator
import kotlin.math.max
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

    /*
      int minBufferMs,
      int maxBufferMs,
      int bufferForPlaybackMs,
      int bufferForPlaybackAfterRebufferMs,
      int targetBufferBytes,
      boolean prioritizeTimeOverSizeThresholds,
      int backBufferDurationMs,
      boolean retainBackBufferFromKeyfram
     */
    private class CustomDefaultLoadController(
        allocator: DefaultAllocator,
        bufferDurations: BufferDurations
    ) : DefaultLoadControl(
        allocator,
        bufferDurations.minBufferDuration.inWholeMilliseconds.toInt(),
        bufferDurations.maxBufferDuration.inWholeMilliseconds.toInt(),
        bufferDurations.bufferForPlayback.inWholeMilliseconds.toInt(),
        bufferDurations.bufferForPlaybackAfterRebuffer.inWholeMilliseconds.toInt(),
        DEFAULT_TARGET_BUFFER_BYTES,
        true,
        BACK_BUFFER_DURATION_MS,
        true
    ) {

        override fun calculateTargetBufferBytes(renderers: Array<out Renderer>, trackSelectionArray: Array<out ExoTrackSelection>): Int {
            var targetBufferSize = 0
            for (i in renderers.indices) {
                if (trackSelectionArray[i] != null) {
                    targetBufferSize += getDefaultBufferSize(renderers[i].trackType)
                }
            }
            return max(DEFAULT_MIN_BUFFER_SIZE.toDouble(), targetBufferSize.toDouble()).toInt()
        }

        private fun getDefaultBufferSize(trackType: @TrackType Int): Int {
            return when (trackType) {
                C.TRACK_TYPE_DEFAULT -> DEFAULT_MUXED_BUFFER_SIZE
                C.TRACK_TYPE_AUDIO -> DEFAULT_AUDIO_BUFFER_SIZE
                C.TRACK_TYPE_VIDEO -> DEFAULT_VIDEO_BUFFER_SIZE
                C.TRACK_TYPE_TEXT -> DEFAULT_TEXT_BUFFER_SIZE
                C.TRACK_TYPE_METADATA -> DEFAULT_METADATA_BUFFER_SIZE
                C.TRACK_TYPE_CAMERA_MOTION -> DEFAULT_CAMERA_MOTION_BUFFER_SIZE
                C.TRACK_TYPE_IMAGE -> DEFAULT_IMAGE_BUFFER_SIZE
                C.TRACK_TYPE_NONE -> 0
                C.TRACK_TYPE_CUSTOM_BASE + 1 -> DEFAULT_METADATA_BUFFER_SIZE
                C.TRACK_TYPE_UNKNOWN -> throw IllegalArgumentException()
                else -> throw IllegalArgumentException()
            }
        }
    }

    private val defaultLoadControl: LoadControl = CustomDefaultLoadController(allocator, bufferDurations)

    override fun onPrepared(playerId: PlayerId) {
        defaultLoadControl.onPrepared(playerId)
    }

    override fun onStopped(playerId: PlayerId) {
        defaultLoadControl.onStopped(playerId)
    }

    override fun onReleased(playerId: PlayerId) {
        defaultLoadControl.onReleased(playerId)
    }

    override fun getAllocator(): Allocator {
        return allocator
    }

    override fun getBackBufferDurationUs(playerId: PlayerId): Long {
        return defaultLoadControl.getBackBufferDurationUs(playerId)
    }

    override fun retainBackBufferFromKeyframe(playerId: PlayerId): Boolean {
        return defaultLoadControl.retainBackBufferFromKeyframe(playerId)
    }

    override fun shouldContinueLoading(parameters: LoadControl.Parameters): Boolean {
        return defaultLoadControl.shouldContinueLoading(parameters)
    }

    override fun onTracksSelected(
        playerId: PlayerId,
        timeline: Timeline,
        mediaPeriodId: MediaSource.MediaPeriodId,
        renderers: Array<out Renderer>,
        trackGroups: TrackGroupArray,
        trackSelections: Array<out ExoTrackSelection>
    ) {
        defaultLoadControl.onTracksSelected(playerId, timeline, mediaPeriodId, renderers, trackGroups, trackSelections)
    }

    override fun shouldStartPlayback(parameters: LoadControl.Parameters): Boolean {
        return defaultLoadControl.shouldStartPlayback(parameters)
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
            minBufferDuration = 1.seconds
        )
    }
}
