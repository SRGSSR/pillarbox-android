/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.cast

import androidx.media3.common.AudioAttributes
import androidx.media3.common.DeviceInfo
import androidx.media3.common.FlagSet
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup

/**
 * Cast forwarding listener
 *
 * Because kotlin delegation with interface with default method are not handled correctly we have to implement all listener method.
 *
 * https://github.com/androidx/media/issues/2120
 *
 * @param player The player to forward to [onEvents].
 * @param listener The [Player.Listener] to forward to.
 */
internal class CastForwardingListener(
    private val player: Player,
    private val listener: Player.Listener
) : Player.Listener by listener {

    override fun onTracksChanged(tracks: Tracks) {
        // Do not forward this event.
    }

    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
        // Do not forward this event.
    }

    override fun onEvents(player: Player, events: Player.Events) {
        // Filter Events triggered by CastPlayer
        if (events.containsAny(Player.EVENT_AVAILABLE_COMMANDS_CHANGED, Player.EVENT_TRACKS_CHANGED)) {
            return
        }
        val flagSet = FlagSet.Builder()
            .apply {
                for (index in 0 until events.size()) {
                    val event = events.get(index)
                    addIf(event, event != Player.EVENT_TRACKS_CHANGED && event != Player.EVENT_AVAILABLE_COMMANDS_CHANGED)
                }
            }
            .build()
        listener.onEvents(this.player, Player.Events(flagSet))
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        listener.onTimelineChanged(timeline, reason)
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        listener.onMediaItemTransition(mediaItem, reason)
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        listener.onMediaMetadataChanged(mediaMetadata)
    }

    override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
        listener.onPlaylistMetadataChanged(mediaMetadata)
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        listener.onIsLoadingChanged(isLoading)
    }

    override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
        listener.onTrackSelectionParametersChanged(parameters)
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        listener.onPlaybackStateChanged(playbackState)
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        listener.onPlayWhenReadyChanged(playWhenReady, reason)
    }

    override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
        listener.onPlaybackSuppressionReasonChanged(playbackSuppressionReason)
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        listener.onIsPlayingChanged(isPlaying)
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        listener.onRepeatModeChanged(repeatMode)
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        listener.onShuffleModeEnabledChanged(shuffleModeEnabled)
    }

    override fun onPlayerError(error: PlaybackException) {
        listener.onPlayerError(error)
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        listener.onPlayerErrorChanged(error)
    }

    override fun onPositionDiscontinuity(oldPosition: Player.PositionInfo, newPosition: Player.PositionInfo, reason: Int) {
        listener.onPositionDiscontinuity(oldPosition, newPosition, reason)
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        listener.onPlaybackParametersChanged(playbackParameters)
    }

    override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
        listener.onSeekBackIncrementChanged(seekBackIncrementMs)
    }

    override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
        listener.onSeekForwardIncrementChanged(seekForwardIncrementMs)
    }

    override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
        listener.onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs)
    }

    override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
        listener.onAudioAttributesChanged(audioAttributes)
    }

    override fun onVolumeChanged(volume: Float) {
        listener.onVolumeChanged(volume)
    }

    override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
        listener.onSkipSilenceEnabledChanged(skipSilenceEnabled)
    }

    override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
        listener.onDeviceInfoChanged(deviceInfo)
    }

    override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
        listener.onDeviceVolumeChanged(volume, muted)
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        listener.onVideoSizeChanged(videoSize)
    }

    override fun onSurfaceSizeChanged(width: Int, height: Int) {
        listener.onSurfaceSizeChanged(width, height)
    }

    override fun onRenderedFirstFrame() {
        listener.onRenderedFirstFrame()
    }

    override fun onCues(cueGroup: CueGroup) {
        listener.onCues(cueGroup)
    }

    override fun onMetadata(metadata: Metadata) {
        listener.onMetadata(metadata)
    }

    override fun equals(other: Any?): Boolean {
        if (this === other) return true
        if (javaClass != other?.javaClass) return false

        other as CastForwardingListener

        if (player != other.player) return false
        if (listener != other.listener) return false

        return true
    }

    override fun hashCode(): Int {
        var result = player.hashCode()
        result = 31 * result + listener.hashCode()
        return result
    }
}
