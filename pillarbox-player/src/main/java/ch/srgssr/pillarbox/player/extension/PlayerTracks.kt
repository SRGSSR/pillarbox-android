/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride

/**
 * Sets a track selection override.
 *
 * @param override The track selection override to apply.
 */
fun Player.setTrackOverride(override: TrackSelectionOverride) {
    trackSelectionParameters = trackSelectionParameters.setTrackOverride(override)
}
