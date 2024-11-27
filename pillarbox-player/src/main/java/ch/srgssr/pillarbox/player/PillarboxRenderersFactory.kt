/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.RenderersFactory

/**
 * Provides a pre-configured instance of [RenderersFactory] suitable for use within Pillarbox.
 *
 * @param context The [Context] required for initializing the [RenderersFactory].
 * @return A [RenderersFactory] ready for use within Pillarbox.
 */
@Suppress("FunctionName")
fun PillarboxRenderersFactory(context: Context): RenderersFactory {
    return DefaultRenderersFactory(context)
        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
        .setEnableDecoderFallback(true)
}
