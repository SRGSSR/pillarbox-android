/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.test.utils

import androidx.media3.common.AudioAttributes
import androidx.media3.common.DeviceInfo
import androidx.media3.common.ForwardingPlayer
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Metadata
import androidx.media3.common.PlaybackException
import androidx.media3.common.PlaybackParameters
import androidx.media3.common.Player
import androidx.media3.common.Player.Listener
import androidx.media3.common.Timeline
import androidx.media3.common.TrackSelectionParameters
import androidx.media3.common.Tracks
import androidx.media3.common.VideoSize
import androidx.media3.common.text.CueGroup

/**
 * Player listener that intercept Player.Listener and allow to simulate listener calls.
 */
open class PlayerListenerCommander(player: Player) : ForwardingPlayer(player), Listener {
    private val listeners = mutableListOf<Listener>()

    /**
     * Has player listener
     */
    val hasPlayerListener: Boolean
        get() = listeners.isNotEmpty()

    override fun addListener(listener: Listener) {
        listeners.add(listener)
    }

    override fun removeListener(listener: Listener) {
        listeners.remove(listener)
    }

    private fun notifyAll(run: (Player: Listener) -> Unit) {
        val copy = HashSet(listeners)
        for (listener in copy) {
            run(listener)
        }
    }

    override fun onEvents(player: Player, events: Player.Events) {
        notifyAll {
            it.onEvents(player, events)
        }
    }

    override fun onTimelineChanged(timeline: Timeline, reason: Int) {
        notifyAll {
            it.onTimelineChanged(timeline, reason)
        }
    }

    override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
        notifyAll {
            it.onMediaItemTransition(mediaItem, reason)
        }
    }

    override fun onTracksChanged(tracks: Tracks) {
        notifyAll {
            it.onTracksChanged(tracks)
        }
    }

    override fun onMediaMetadataChanged(mediaMetadata: MediaMetadata) {
        notifyAll {
            it.onMediaMetadataChanged(mediaMetadata)
        }
    }

    override fun onPlaylistMetadataChanged(mediaMetadata: MediaMetadata) {
        notifyAll {
            it.onPlaylistMetadataChanged(mediaMetadata)
        }
    }

    override fun onIsLoadingChanged(isLoading: Boolean) {
        notifyAll {
            it.onIsLoadingChanged(isLoading)
        }
    }

    override fun onAvailableCommandsChanged(availableCommands: Player.Commands) {
        notifyAll { it.onAvailableCommandsChanged(availableCommands) }
    }

    override fun onTrackSelectionParametersChanged(parameters: TrackSelectionParameters) {
        notifyAll { it.onTrackSelectionParametersChanged(parameters) }
    }

    override fun onPlaybackStateChanged(playbackState: Int) {
        notifyAll { it.onPlaybackStateChanged(playbackState) }
    }

    override fun onPlayWhenReadyChanged(playWhenReady: Boolean, reason: Int) {
        notifyAll { it.onPlayWhenReadyChanged(playWhenReady, reason) }
    }

    override fun onPlaybackSuppressionReasonChanged(playbackSuppressionReason: Int) {
        notifyAll { it.onPlaybackSuppressionReasonChanged(playbackSuppressionReason) }
    }

    override fun onIsPlayingChanged(isPlaying: Boolean) {
        notifyAll { it.onIsPlayingChanged(isPlaying) }
    }

    override fun onRepeatModeChanged(repeatMode: Int) {
        notifyAll { it.onRepeatModeChanged(repeatMode) }
    }

    override fun onShuffleModeEnabledChanged(shuffleModeEnabled: Boolean) {
        notifyAll { it.onShuffleModeEnabledChanged(shuffleModeEnabled) }
    }

    override fun onPlayerError(error: PlaybackException) {
        notifyAll { it.onPlayerError(error) }
    }

    override fun onPlayerErrorChanged(error: PlaybackException?) {
        notifyAll { it.onPlayerErrorChanged(error) }
    }

    override fun onPositionDiscontinuity(
        oldPosition: Player.PositionInfo,
        newPosition: Player.PositionInfo,
        reason: Int
    ) {
        notifyAll { it.onPositionDiscontinuity(oldPosition, newPosition, reason) }
    }

    override fun onPlaybackParametersChanged(playbackParameters: PlaybackParameters) {
        notifyAll { it.onPlaybackParametersChanged(playbackParameters) }
    }

    override fun onSeekBackIncrementChanged(seekBackIncrementMs: Long) {
        notifyAll { it.onSeekBackIncrementChanged(seekBackIncrementMs) }
    }

    override fun onSeekForwardIncrementChanged(seekForwardIncrementMs: Long) {
        notifyAll { it.onSeekForwardIncrementChanged(seekForwardIncrementMs) }
    }

    override fun onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs: Long) {
        notifyAll { it.onMaxSeekToPreviousPositionChanged(maxSeekToPreviousPositionMs) }
    }

    override fun onAudioSessionIdChanged(audioSessionId: Int) {
        notifyAll { it.onAudioSessionIdChanged(audioSessionId) }
    }

    override fun onAudioAttributesChanged(audioAttributes: AudioAttributes) {
        notifyAll { it.onAudioAttributesChanged(audioAttributes) }
    }

    override fun onVolumeChanged(volume: Float) {
        notifyAll { it.onVolumeChanged(volume) }
    }

    override fun onSkipSilenceEnabledChanged(skipSilenceEnabled: Boolean) {
        notifyAll { it.onSkipSilenceEnabledChanged(skipSilenceEnabled) }
    }

    override fun onDeviceInfoChanged(deviceInfo: DeviceInfo) {
        notifyAll { it.onDeviceInfoChanged(deviceInfo) }
    }

    override fun onDeviceVolumeChanged(volume: Int, muted: Boolean) {
        notifyAll { it.onDeviceVolumeChanged(volume, muted) }
    }

    override fun onVideoSizeChanged(videoSize: VideoSize) {
        notifyAll { it.onVideoSizeChanged(videoSize) }
    }

    override fun onSurfaceSizeChanged(width: Int, height: Int) {
        notifyAll { it.onSurfaceSizeChanged(width, height) }
    }

    override fun onRenderedFirstFrame() {
        notifyAll { it.onRenderedFirstFrame() }
    }

    override fun onCues(cueGroup: CueGroup) {
        notifyAll { it.onCues(cueGroup) }
    }

    override fun onMetadata(metadata: Metadata) {
        notifyAll { it.onMetadata(metadata) }
    }
}
