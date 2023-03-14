/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.AudioAttributes
import androidx.media3.common.DeviceInfo
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.Cue
import androidx.media3.common.text.CueGroup
import androidx.media3.exoplayer.DecoderCounters
import androidx.media3.exoplayer.DecoderReuseEvaluation
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.exoplayer.analytics.AnalyticsListener
import androidx.media3.exoplayer.source.LoadEventInfo
import androidx.media3.exoplayer.source.MediaLoadData
import java.io.IOException

class AnalyticsListenerCommander(mock: ExoPlayer) : ExoPlayer by mock, AnalyticsListener {
    private val listeners = mutableListOf<AnalyticsListener>()

    val hasListeners: Boolean
        get() = listeners.isNotEmpty()


    override fun addListener(listener: Player.Listener) {

    }

    override fun removeListener(listener: Player.Listener) {

    }

    override fun addAnalyticsListener(listener: AnalyticsListener) {
        listeners.add(listener)
    }

    override fun removeAnalyticsListener(listener: AnalyticsListener) {
        listeners.remove(listener)
    }

    private fun notifyAll(run: (Player: AnalyticsListener) -> Unit) {
        val list = listeners.toList()
        for (listener in list) {
            run(listener)
        }
    }

    override fun onPlaybackStateChanged(eventTime: AnalyticsListener.EventTime, state: Int) {
        notifyAll { it.onPlaybackStateChanged(eventTime, state) }
    }

    override fun onPlayWhenReadyChanged(eventTime: AnalyticsListener.EventTime, playWhenReady: Boolean, reason: Int) {
        notifyAll { it.onPlayWhenReadyChanged(eventTime, playWhenReady, reason) }
    }

    override fun onPlaybackSuppressionReasonChanged(eventTime: AnalyticsListener.EventTime, playbackSuppressionReason: Int) {
        notifyAll { it.onPlaybackSuppressionReasonChanged(eventTime, playbackSuppressionReason) }
    }

    override fun onIsPlayingChanged(eventTime: AnalyticsListener.EventTime, isPlaying: Boolean) {
        notifyAll { it.onIsPlayingChanged(eventTime, isPlaying) }
    }

    override fun onTimelineChanged(eventTime: AnalyticsListener.EventTime, reason: Int) {
        notifyAll { it.onTimelineChanged(eventTime, reason) }
    }

    override fun onMediaItemTransition(eventTime: AnalyticsListener.EventTime, mediaItem: MediaItem?, reason: Int) {
        notifyAll { it.onMediaItemTransition(eventTime, mediaItem, reason) }
    }

    override fun onPositionDiscontinuity(eventTime: AnalyticsListener.EventTime, reason: Int) {
        notifyAll { it.onPositionDiscontinuity(eventTime, reason) }
    }

    override fun onPositionDiscontinuity(
        eventTime: AnalyticsListener.EventTime,
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        notifyAll { it.onPositionDiscontinuity(eventTime, oldPosition, newPosition, reason) }
    }

    override fun onSeekStarted(eventTime: AnalyticsListener.EventTime) {
        notifyAll { it.onSeekStarted(eventTime) }
    }

    override fun onSeekProcessed(eventTime: AnalyticsListener.EventTime) {
        notifyAll { it.onSeekProcessed(eventTime) }
    }

    override fun onPlaybackParametersChanged(eventTime: AnalyticsListener.EventTime, playbackParameters: PlaybackParameters) {
        notifyAll { it.onPlaybackParametersChanged(eventTime, playbackParameters) }
    }

    override fun onSeekBackIncrementChanged(eventTime: AnalyticsListener.EventTime, seekBackIncrementMs: Long) {
        notifyAll { it.onSeekBackIncrementChanged(eventTime, seekBackIncrementMs) }
    }

    override fun onSeekForwardIncrementChanged(eventTime: AnalyticsListener.EventTime, seekForwardIncrementMs: Long) {
        notifyAll { it.onSeekForwardIncrementChanged(eventTime, seekForwardIncrementMs) }
    }

    override fun onMaxSeekToPreviousPositionChanged(eventTime: AnalyticsListener.EventTime, maxSeekToPreviousPositionMs: Long) {
        notifyAll { it.onMaxSeekToPreviousPositionChanged(eventTime, maxSeekToPreviousPositionMs) }
    }

    override fun onRepeatModeChanged(eventTime: AnalyticsListener.EventTime, repeatMode: Int) {
        notifyAll { it.onRepeatModeChanged(eventTime, repeatMode) }
    }

    override fun onShuffleModeChanged(eventTime: AnalyticsListener.EventTime, shuffleModeEnabled: Boolean) {
        notifyAll { it.onShuffleModeChanged(eventTime, shuffleModeEnabled) }
    }

    override fun onIsLoadingChanged(eventTime: AnalyticsListener.EventTime, isLoading: Boolean) {
        notifyAll { it.onIsLoadingChanged(eventTime, isLoading) }
    }

    override fun onLoadingChanged(eventTime: AnalyticsListener.EventTime, isLoading: Boolean) {
        notifyAll { it.onLoadingChanged(eventTime, isLoading) }
    }

    override fun onAvailableCommandsChanged(eventTime: AnalyticsListener.EventTime, availableCommands: Player.Commands) {
        notifyAll { it.onAvailableCommandsChanged(eventTime, availableCommands) }
    }

    override fun onPlayerError(eventTime: AnalyticsListener.EventTime, error: PlaybackException) {
        notifyAll {
            it.onPlayerError(eventTime, error)
        }
    }

    override fun onPlayerErrorChanged(eventTime: AnalyticsListener.EventTime, error: PlaybackException?) {
        notifyAll { it.onPlayerErrorChanged(eventTime, error) }
    }

    override fun onTracksChanged(eventTime: AnalyticsListener.EventTime, tracks: Tracks) {
        notifyAll { it.onTracksChanged(eventTime, tracks) }
    }

    override fun onTrackSelectionParametersChanged(
        eventTime: AnalyticsListener.EventTime,
        trackSelectionParameters: TrackSelectionParameters
    ) {
        notifyAll { it.onTrackSelectionParametersChanged(eventTime, trackSelectionParameters) }
    }

    override fun onMediaMetadataChanged(eventTime: AnalyticsListener.EventTime, mediaMetadata: MediaMetadata) {
        notifyAll { it.onMediaMetadataChanged(eventTime, mediaMetadata) }
    }

    override fun onPlaylistMetadataChanged(eventTime: AnalyticsListener.EventTime, playlistMetadata: MediaMetadata) {
        notifyAll { it.onPlaylistMetadataChanged(eventTime, playlistMetadata) }
    }

    override fun onLoadStarted(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        notifyAll { it.onLoadStarted(eventTime, loadEventInfo, mediaLoadData) }
    }

    override fun onLoadCompleted(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        notifyAll { it.onLoadCompleted(eventTime, loadEventInfo, mediaLoadData) }
    }

    override fun onLoadCanceled(eventTime: AnalyticsListener.EventTime, loadEventInfo: LoadEventInfo, mediaLoadData: MediaLoadData) {
        notifyAll { it.onLoadCanceled(eventTime, loadEventInfo, mediaLoadData) }
    }

    override fun onLoadError(
        eventTime: AnalyticsListener.EventTime,
        loadEventInfo: LoadEventInfo,
        mediaLoadData: MediaLoadData,
        error: IOException,
        wasCanceled: Boolean
    ) {
        notifyAll { it.onLoadError(eventTime, loadEventInfo, mediaLoadData, error, wasCanceled) }
    }

    override fun onDownstreamFormatChanged(eventTime: AnalyticsListener.EventTime, mediaLoadData: MediaLoadData) {
        notifyAll { it.onDownstreamFormatChanged(eventTime, mediaLoadData) }
    }

    override fun onUpstreamDiscarded(eventTime: AnalyticsListener.EventTime, mediaLoadData: MediaLoadData) {
        notifyAll { it.onUpstreamDiscarded(eventTime, mediaLoadData) }
    }

    override fun onBandwidthEstimate(
        eventTime: AnalyticsListener.EventTime,
        totalLoadTimeMs: Int,
        totalBytesLoaded: Long,
        bitrateEstimate: Long
    ) {
        notifyAll { it.onBandwidthEstimate(eventTime, totalLoadTimeMs, totalBytesLoaded, bitrateEstimate) }
    }

    override fun onMetadata(eventTime: AnalyticsListener.EventTime, metadata: Metadata) {
        notifyAll { it.onMetadata(eventTime, metadata) }
    }

    override fun onCues(eventTime: AnalyticsListener.EventTime, cues: MutableList<Cue>) {
        notifyAll { it.onCues(eventTime, cues) }
    }

    override fun onCues(eventTime: AnalyticsListener.EventTime, cueGroup: CueGroup) {
        notifyAll { it.onCues(eventTime, cueGroup) }
    }

    override fun onDecoderEnabled(eventTime: AnalyticsListener.EventTime, trackType: Int, decoderCounters: DecoderCounters) {
        notifyAll { it.onDecoderEnabled(eventTime, trackType, decoderCounters) }
    }

    override fun onDecoderInitialized(
        eventTime: AnalyticsListener.EventTime,
        trackType: Int,
        decoderName: String,
        initializationDurationMs: Long
    ) {
        notifyAll { it.onDecoderInitialized(eventTime, trackType, decoderName, initializationDurationMs) }
    }

    override fun onDecoderInputFormatChanged(eventTime: AnalyticsListener.EventTime, trackType: Int, format: Format) {
        notifyAll { it.onDecoderInputFormatChanged(eventTime, trackType, format) }
    }

    override fun onDecoderDisabled(eventTime: AnalyticsListener.EventTime, trackType: Int, decoderCounters: DecoderCounters) {
        notifyAll { it.onDecoderDisabled(eventTime, trackType, decoderCounters) }
    }

    override fun onAudioEnabled(eventTime: AnalyticsListener.EventTime, decoderCounters: DecoderCounters) {
        notifyAll { it.onAudioEnabled(eventTime, decoderCounters) }
    }

    override fun onAudioDecoderInitialized(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        notifyAll { it.onAudioDecoderInitialized(eventTime, decoderName, initializedTimestampMs, initializationDurationMs) }
    }

    override fun onAudioDecoderInitialized(eventTime: AnalyticsListener.EventTime, decoderName: String, initializationDurationMs: Long) {
        notifyAll { it.onAudioDecoderInitialized(eventTime, decoderName, initializationDurationMs) }
    }

    override fun onAudioInputFormatChanged(eventTime: AnalyticsListener.EventTime, format: Format) {
        notifyAll { it.onAudioInputFormatChanged(eventTime, format) }
    }

    override fun onAudioInputFormatChanged(
        eventTime: AnalyticsListener.EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?
    ) {
        notifyAll { it.onAudioInputFormatChanged(eventTime, format, decoderReuseEvaluation) }
    }

    override fun onAudioPositionAdvancing(eventTime: AnalyticsListener.EventTime, playoutStartSystemTimeMs: Long) {
        notifyAll { it.onAudioPositionAdvancing(eventTime, playoutStartSystemTimeMs) }
    }

    override fun onAudioUnderrun(eventTime: AnalyticsListener.EventTime, bufferSize: Int, bufferSizeMs: Long, elapsedSinceLastFeedMs: Long) {
        notifyAll { it.onAudioUnderrun(eventTime, bufferSize, bufferSizeMs, elapsedSinceLastFeedMs) }
    }

    override fun onAudioDecoderReleased(eventTime: AnalyticsListener.EventTime, decoderName: String) {
        notifyAll { it.onAudioDecoderReleased(eventTime, decoderName) }
    }

    override fun onAudioDisabled(eventTime: AnalyticsListener.EventTime, decoderCounters: DecoderCounters) {
        notifyAll { it.onAudioDisabled(eventTime, decoderCounters) }
    }

    override fun onAudioSessionIdChanged(eventTime: AnalyticsListener.EventTime, audioSessionId: Int) {
        notifyAll { it.onAudioSessionIdChanged(eventTime, audioSessionId) }
    }

    override fun onAudioAttributesChanged(eventTime: AnalyticsListener.EventTime, audioAttributes: AudioAttributes) {
        notifyAll { it.onAudioAttributesChanged(eventTime, audioAttributes) }
    }

    override fun onSkipSilenceEnabledChanged(eventTime: AnalyticsListener.EventTime, skipSilenceEnabled: Boolean) {
        notifyAll { it.onSkipSilenceEnabledChanged(eventTime, skipSilenceEnabled) }
    }

    override fun onAudioSinkError(eventTime: AnalyticsListener.EventTime, audioSinkError: Exception) {
        notifyAll { it.onAudioSinkError(eventTime, audioSinkError) }
    }

    override fun onAudioCodecError(eventTime: AnalyticsListener.EventTime, audioCodecError: Exception) {
        notifyAll { it.onAudioCodecError(eventTime, audioCodecError) }
    }

    override fun onVolumeChanged(eventTime: AnalyticsListener.EventTime, volume: Float) {
        notifyAll { it.onVolumeChanged(eventTime, volume) }
    }

    override fun onDeviceInfoChanged(eventTime: AnalyticsListener.EventTime, deviceInfo: DeviceInfo) {
        notifyAll { it.onDeviceInfoChanged(eventTime, deviceInfo) }
    }

    override fun onDeviceVolumeChanged(eventTime: AnalyticsListener.EventTime, volume: Int, muted: Boolean) {
        notifyAll { it.onDeviceVolumeChanged(eventTime, volume, muted) }
    }

    override fun onVideoEnabled(eventTime: AnalyticsListener.EventTime, decoderCounters: DecoderCounters) {
        notifyAll { it.onVideoEnabled(eventTime, decoderCounters) }
    }

    override fun onVideoDecoderInitialized(
        eventTime: AnalyticsListener.EventTime,
        decoderName: String,
        initializedTimestampMs: Long,
        initializationDurationMs: Long
    ) {
        notifyAll { it.onVideoDecoderInitialized(eventTime, decoderName, initializedTimestampMs, initializationDurationMs) }
    }

    override fun onVideoDecoderInitialized(eventTime: AnalyticsListener.EventTime, decoderName: String, initializationDurationMs: Long) {
        notifyAll { it.onVideoDecoderInitialized(eventTime, decoderName, initializationDurationMs) }
    }

    override fun onVideoInputFormatChanged(eventTime: AnalyticsListener.EventTime, format: Format) {
        notifyAll { it.onVideoInputFormatChanged(eventTime, format) }
    }

    override fun onVideoInputFormatChanged(
        eventTime: AnalyticsListener.EventTime,
        format: Format,
        decoderReuseEvaluation: DecoderReuseEvaluation?
    ) {
        notifyAll { it.onVideoInputFormatChanged(eventTime, format, decoderReuseEvaluation) }
    }

    override fun onDroppedVideoFrames(eventTime: AnalyticsListener.EventTime, droppedFrames: Int, elapsedMs: Long) {
        notifyAll { it.onDroppedVideoFrames(eventTime, droppedFrames, elapsedMs) }
    }

    override fun onVideoDecoderReleased(eventTime: AnalyticsListener.EventTime, decoderName: String) {
        notifyAll { it.onVideoDecoderReleased(eventTime, decoderName) }
    }

    override fun onVideoDisabled(eventTime: AnalyticsListener.EventTime, decoderCounters: DecoderCounters) {
        notifyAll { it.onVideoDisabled(eventTime, decoderCounters) }
    }

    override fun onVideoFrameProcessingOffset(eventTime: AnalyticsListener.EventTime, totalProcessingOffsetUs: Long, frameCount: Int) {
        notifyAll { it.onVideoFrameProcessingOffset(eventTime, totalProcessingOffsetUs, frameCount) }
    }

    override fun onVideoCodecError(eventTime: AnalyticsListener.EventTime, videoCodecError: Exception) {
        notifyAll { it.onVideoCodecError(eventTime, videoCodecError) }
    }

    override fun onRenderedFirstFrame(eventTime: AnalyticsListener.EventTime, output: Any, renderTimeMs: Long) {
        notifyAll { it.onRenderedFirstFrame(eventTime, output, renderTimeMs) }
    }

    override fun onVideoSizeChanged(eventTime: AnalyticsListener.EventTime, videoSize: VideoSize) {
        notifyAll { it.onVideoSizeChanged(eventTime, videoSize) }
    }

    override fun onVideoSizeChanged(
        eventTime: AnalyticsListener.EventTime,
        width: Int,
        height: Int,
        unappliedRotationDegrees: Int,
        pixelWidthHeightRatio: Float
    ) {
        notifyAll { it.onVideoSizeChanged(eventTime, width, height, unappliedRotationDegrees, pixelWidthHeightRatio) }
    }

    override fun onSurfaceSizeChanged(eventTime: AnalyticsListener.EventTime, width: Int, height: Int) {
        notifyAll { it.onSurfaceSizeChanged(eventTime, width, height) }
    }

    override fun onDrmSessionAcquired(eventTime: AnalyticsListener.EventTime) {
        notifyAll { it.onDrmSessionAcquired(eventTime) }
    }

    override fun onDrmSessionAcquired(eventTime: AnalyticsListener.EventTime, state: Int) {
        notifyAll { it.onDrmSessionAcquired(eventTime, state) }
    }

    override fun onDrmKeysLoaded(eventTime: AnalyticsListener.EventTime) {
        notifyAll { it.onDrmKeysLoaded(eventTime) }
    }

    override fun onDrmSessionManagerError(eventTime: AnalyticsListener.EventTime, error: Exception) {
        notifyAll { it.onDrmSessionManagerError(eventTime, error) }
    }

    override fun onDrmKeysRestored(eventTime: AnalyticsListener.EventTime) {
        notifyAll { it.onDrmKeysRestored(eventTime) }
    }

    override fun onDrmKeysRemoved(eventTime: AnalyticsListener.EventTime) {
        notifyAll { it.onDrmKeysRemoved(eventTime) }
    }

    override fun onDrmSessionReleased(eventTime: AnalyticsListener.EventTime) {
        notifyAll { it.onDrmSessionReleased(eventTime) }
    }

    override fun onPlayerReleased(eventTime: AnalyticsListener.EventTime) {
        notifyAll { it.onPlayerReleased(eventTime) }
    }

    override fun onEvents(player: Player, events: AnalyticsListener.Events) {
        notifyAll { it.onEvents(player, events) }
    }
}
