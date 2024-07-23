/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.content.Context
import android.os.Handler
import androidx.media3.exoplayer.DefaultRenderersFactory
import androidx.media3.exoplayer.Renderer

/**
 * Pillarbox renderer factory
 */
class PillarboxRendererFactory(context: Context) : DefaultRenderersFactory(context) {

    override fun buildMiscellaneousRenderers(context: Context, eventHandler: Handler, extensionRendererMode: Int, out: ArrayList<Renderer>) {
        out.add(PillarboxRenderer())
    }
}
