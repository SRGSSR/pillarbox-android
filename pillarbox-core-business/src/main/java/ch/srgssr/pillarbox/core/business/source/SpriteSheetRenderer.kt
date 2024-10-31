/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.exoplayer.BaseRenderer
import androidx.media3.exoplayer.image.ImageOutput
import kotlin.time.Duration.Companion.microseconds

class SpriteSheetRenderer(
    private var imageOutput: ImageOutput = ImageOutput.NO_OP
) : BaseRenderer(TRACK_TYPE_SPRITE_SHEET) {

    override fun getName(): String {
        return SpriteSheetRenderer::class.java.simpleName
    }

    override fun render(positionUs: Long, elapsedRealtimeUs: Long) {
        Log.d(TAG, "render ${positionUs.microseconds} ${elapsedRealtimeUs.microseconds}")
    }

    override fun isReady(): Boolean {
        Log.d(TAG, "isReady")
        return true
    }

    override fun isEnded(): Boolean {
        Log.d(TAG, "isEnded")
        return false
    }

    override fun supportsFormat(format: Format): Int {
        return if (format.sampleMimeType == MIME_TYPE_SPRITE_SHEET) C.FORMAT_HANDLED else C.FORMAT_UNSUPPORTED_TYPE
    }

    override fun handleMessage(messageType: Int, message: Any?) {
        when (messageType) {
            MSG_SET_IMAGE_OUTPUT -> {
                this.imageOutput = if (message is ImageOutput) message else ImageOutput.NO_OP
            }

            else -> super.handleMessage(messageType, message)
        }
    }

    @Suppress("UndocumentedPublicClass")
    companion object {
        const val TRACK_TYPE_SPRITE_SHEET = C.TRACK_TYPE_CUSTOM_BASE + 100
        const val MIME_TYPE_SPRITE_SHEET = MimeTypes.BASE_TYPE_APPLICATION + "/srg-sprite-sheet"

        init {
            MimeTypes.registerCustomMimeType(MIME_TYPE_SPRITE_SHEET, "srg", TRACK_TYPE_SPRITE_SHEET)
        }

        private const val TAG = "SpriteSheetRenderer"
    }
}
