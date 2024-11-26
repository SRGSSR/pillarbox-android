/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import androidx.media3.common.C
import androidx.media3.exoplayer.ExoPlayer.PreloadConfiguration
import kotlin.time.Duration

/**
 * @param targetPreloadDuration The target duration to preload or `null` to disable preloading.
 * @return [PreloadConfiguration]
 */
fun PreloadConfiguration(targetPreloadDuration: Duration?): PreloadConfiguration {
    return PreloadConfiguration(targetPreloadDuration?.inWholeMicroseconds ?: C.TIME_UNSET)
}
