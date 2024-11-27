/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter

/**
 * Provides a pre-configured instance of [BandwidthMeter] suitable for use within Pillarbox.
 *
 * @param context The [Context] required for initializing the [BandwidthMeter].
 * @return A [BandwidthMeter] ready for use within Pillarbox.
 */
@Suppress("FunctionName")
fun PillarboxBandwidthMeter(context: Context): BandwidthMeter {
    return DefaultBandwidthMeter.getSingletonInstance(context)
}
