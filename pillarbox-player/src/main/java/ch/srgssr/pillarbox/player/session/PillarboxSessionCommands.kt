/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.session

import android.os.Bundle
import androidx.media3.session.SessionCommand

internal object PillarboxSessionCommands {
    const val SMOOTH_SEEKING_ENABLED = "pillarbox.smooth.seeking.enabled"
    const val SMOOTH_SEEKING_DISABLED = "pillarbox.smooth.seeking.disabled"
    const val SMOOTH_SEEKING_CHANGED = "pillarbox.smooth.seeking.changed"
    const val SMOOTH_SEEKING_GET = "pillarbox.smooth.seeking.get"

    val COMMAND_SEEK_ENABLED = SessionCommand(SMOOTH_SEEKING_ENABLED, Bundle.EMPTY)
    val COMMAND_SEEK_DISABLED = SessionCommand(SMOOTH_SEEKING_DISABLED, Bundle.EMPTY)
    val COMMAND_SEEK_GET = SessionCommand(SMOOTH_SEEKING_GET, Bundle.EMPTY)

    fun seekChangedCommand(smoothSeekingEnabled: Boolean) =
        SessionCommand(SMOOTH_SEEKING_CHANGED, Bundle().apply { putBoolean("smoothSeekingEnabled", smoothSeekingEnabled) })
}
