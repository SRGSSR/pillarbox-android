/*
 * Copyright (c) 2023. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.exoplayer.ExoPlayer
import ch.srgssr.pillarbox.player.SeekIncrement

/**
 * Set seek increments
 *
 * @param seekIncrement The seek increments to set.
 * @return this
 */
fun ExoPlayer.Builder.setSeekIncrements(seekIncrement: SeekIncrement): ExoPlayer.Builder {
    setSeekForwardIncrementMs(seekIncrement.forward.inWholeMilliseconds)
    setSeekBackIncrementMs(seekIncrement.backward.inWholeMilliseconds)
    return this
}
