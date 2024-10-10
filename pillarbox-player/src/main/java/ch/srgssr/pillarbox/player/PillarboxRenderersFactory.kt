/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.RenderersFactory

/**
 * Preconfigured [RenderersFactory] for Pillarbox.
 *
 * @param context The [Context] needed to create the [RenderersFactory].
 */
@Suppress("FunctionName")
fun PillarboxRenderersFactory(context: Context): RenderersFactory {
    return DefaultRenderersFactory(context)
        .setExtensionRendererMode(DefaultRenderersFactory.EXTENSION_RENDERER_MODE_OFF)
        .setEnableDecoderFallback(true)
}
