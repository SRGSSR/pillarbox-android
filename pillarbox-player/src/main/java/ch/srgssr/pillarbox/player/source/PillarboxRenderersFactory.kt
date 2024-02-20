/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.content.Context
import android.os.Looper
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer
import androidx.media3.exoplayer.metadata.MetadataOutput

/**
 * Pillarbox renderers factory
 *
 * Add a special MetadataRenderer to publish chapters/blocked segment as Metadata.
 *
 * @param context a Context.
 */
class PillarboxRenderersFactory(context: Context) : DefaultRenderersFactory(context) {
    init {
        setExtensionRendererMode(EXTENSION_RENDERER_MODE_OFF)
        setEnableDecoderFallback(true)
    }

    override fun buildMetadataRenderers(
        context: Context,
        output: MetadataOutput,
        outputLooper: Looper,
        extensionRendererMode: Int,
        out: ArrayList<Renderer>
    ) {
        super.buildMetadataRenderers(context, output, outputLooper, extensionRendererMode, out)
        out.add(PillarboxMetadataRenderer(output, outputLooper))
    }
}
