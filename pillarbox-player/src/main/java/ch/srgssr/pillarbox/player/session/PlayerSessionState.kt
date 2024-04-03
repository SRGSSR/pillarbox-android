/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.os.Bundle
import ch.srgssr.pillarbox.player.PillarboxPlayer

/**
 * The player state that is bundled as media session extras for each connected controller.
 */
internal data class PlayerSessionState(val smoothSeekingEnabled: Boolean, val trackingEnabled: Boolean) {
    constructor(extras: Bundle) : this(
        smoothSeekingEnabled = extras.getBoolean(PillarboxSessionCommands.SMOOTH_SEEKING_ARG),
        trackingEnabled = extras.getBoolean(PillarboxSessionCommands.TRACKER_ENABLED_ARG),
    )

    constructor(player: PillarboxPlayer) : this(
        smoothSeekingEnabled = player.smoothSeekingEnabled,
        trackingEnabled = player.trackingEnabled,
    )

    fun toBundle(bundle: Bundle = Bundle.EMPTY): Bundle {
        return Bundle(bundle).apply {
            putBoolean(PillarboxSessionCommands.SMOOTH_SEEKING_ARG, smoothSeekingEnabled)
            putBoolean(PillarboxSessionCommands.TRACKER_ENABLED_ARG, trackingEnabled)
        }
    }
}
