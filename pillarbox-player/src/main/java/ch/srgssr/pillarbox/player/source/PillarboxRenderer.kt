/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.BaseRenderer
import androidx.media3.exoplayer.RendererCapabilities
import java.util.Arrays
import kotlin.time.Duration.Companion.milliseconds

/**
 * Track Type
 */
const val PILLARBOX_TRACK_TYPE = C.TRACK_TYPE_CUSTOM_BASE + 1

/**
 * Pillarbox Track Mime Type
 */
const val PILLARBOX_TRACK_MIME_TYPE = "${MimeTypes.BASE_TYPE_APPLICATION}/pillarbox"

/**
 * Pillarbox renderer
 * */
class PillarboxRenderer : BaseRenderer(PILLARBOX_TRACK_TYPE) {

    init {
        MimeTypes.registerCustomMimeType(PILLARBOX_TRACK_MIME_TYPE, "pillarbox", PILLARBOX_TRACK_TYPE)
    }

    override fun getName(): String {
        return "PillarboxRenderer"
    }

    override fun render(positionUs: Long, elapsedRealtimeUs: Long) {
        Log.d(name, "render@${positionUs.milliseconds} $stream ${Arrays.toString(streamFormats)}")
    }

    override fun isReady(): Boolean {
        return true
    }

    override fun isEnded(): Boolean {
        return false
    }

    override fun supportsFormat(format: Format): Int {
        return if (MimeTypes.getTrackType(format.sampleMimeType) == trackType) RendererCapabilities.create(C.FORMAT_HANDLED) else
            RendererCapabilities.create(C.FORMAT_UNSUPPORTED_TYPE)
    }
}
