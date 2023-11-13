/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride

/**
 * Disable text track
 *
 */
fun Player.disableTextTrack() {
    trackSelectionParameters = trackSelectionParameters.disableTextTrack()
}

/**
 * Set default text track
 *
 * @param context
 */
fun Player.setDefaultTextTrack(context: Context) {
    trackSelectionParameters = trackSelectionParameters.defaultTextTrack(context)
}

/**
 * Disable audio track
 *
 */
fun Player.disableAudioTrack() {
    trackSelectionParameters = trackSelectionParameters.disableAudioTrack()
}

/**
 * Set default audio track
 *
 * @param context
 */
fun Player.setDefaultAudioTrack(context: Context) {
    trackSelectionParameters = trackSelectionParameters.defaultAudioTrack(context)
}

/**
 * Set track override
 *
 * @param override
 */
fun Player.setTrackOverride(override: TrackSelectionOverride) {
    trackSelectionParameters = trackSelectionParameters.setTrackOverride(override)
}
