/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.Player
import androidx.media3.common.TrackSelectionOverride

/**
 * Set track override.
 *
 * @param override
 */
fun Player.setTrackOverride(override: TrackSelectionOverride) {
    trackSelectionParameters = trackSelectionParameters.setTrackOverride(override)
}
