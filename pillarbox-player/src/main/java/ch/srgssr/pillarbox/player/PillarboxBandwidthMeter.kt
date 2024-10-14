/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.exoplayer.upstream.BandwidthMeter
import androidx.media3.exoplayer.upstream.DefaultBandwidthMeter

/**
 * Preconfigured [BandwidthMeter] for Pillarbox.
 *
 * @param context The [Context] needed to create the [BandwidthMeter].
 */
@Suppress("FunctionName")
fun PillarboxBandwidthMeter(context: Context): BandwidthMeter {
    return DefaultBandwidthMeter.getSingletonInstance(context)
}
