/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer.PreloadConfiguration
import kotlin.time.Duration

/**
 * Creates a [PreloadConfiguration] instance using a [Duration].
 *
 * @param targetPreloadDuration The target duration to preload. If `null`, preloading will be disabled.
 * @return A [PreloadConfiguration] instance with the specified preload duration.
 */
fun PreloadConfiguration(targetPreloadDuration: Duration?): PreloadConfiguration {
    return PreloadConfiguration(targetPreloadDuration?.inWholeMicroseconds ?: C.TIME_UNSET)
}
