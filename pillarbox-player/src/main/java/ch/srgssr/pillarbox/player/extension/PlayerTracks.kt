/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride

/**
 * Disable text track.
 *
 * @deprecated Use the `disableTextTrack()` extension from the `tracks` package instead.
 */
@Deprecated(
    message = "Use the `disableTextTrack()` extension from the `tracks` package instead",
    replaceWith = ReplaceWith(
        expression = "disableTextTrack()",
        imports = ["ch.srgssr.pillarbox.player.tracks.disableTextTrack"],
    ),
)
fun Player.disableTextTrack() {
    trackSelectionParameters = trackSelectionParameters.disableTextTrack()
}

/**
 * Set default text track.
 *
 * @param context
 *
 * @deprecated Use the `setAutoTextTrack(Context)` extension instead.
 */
@Deprecated(
    message = "Use the `setAutoTextTrack(Context)` extension instead",
    replaceWith = ReplaceWith(
        expression = "setAutoTextTrack(context)",
        imports = ["ch.srgssr.pillarbox.player.tracks.setAutoTextTrack"],
    ),
)
fun Player.setDefaultTextTrack(context: Context) {
    trackSelectionParameters = trackSelectionParameters.defaultTextTrack(context)
}

/**
 * Disable audio track.
 *
 * @deprecated Use the `disableAudioTrack()` extension from the `tracks` package instead.
 */
@Deprecated(
    message = "Use the `disableAudioTrack()` extension from the `tracks` package instead",
    replaceWith = ReplaceWith(
        expression = "disableAudioTrack()",
        imports = ["ch.srgssr.pillarbox.player.tracks.disableAudioTrack"],
    ),
)
fun Player.disableAudioTrack() {
    trackSelectionParameters = trackSelectionParameters.disableAudioTrack()
}

/**
 * Set default audio track.
 *
 * @param context
 *
 * @deprecated Use the `setAutoAudioTrack(Context)` extension instead.
 */
@Deprecated(
    message = "Use the `setAutoAudioTrack(Context)` extension instead",
    replaceWith = ReplaceWith(
        expression = "setAutoAudioTrack(context)",
        imports = ["ch.srgssr.pillarbox.player.tracks.setAutoAudioTrack"],
    ),
)
fun Player.setDefaultAudioTrack(context: Context) {
    trackSelectionParameters = trackSelectionParameters.defaultAudioTrack(context)
}

/**
 * Set track override.
 *
 * @param override
 */
fun Player.setTrackOverride(override: TrackSelectionOverride) {
    trackSelectionParameters = trackSelectionParameters.setTrackOverride(override)
}
