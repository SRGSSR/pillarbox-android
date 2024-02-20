/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.annotation.SuppressLint
import android.os.Handler.Callback
import android.os.Looper
import android.os.Message
import android.util.Log
import androidx.media3.common.C
import androidx.media3.common.Format
import androidx.media3.common.Metadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.util.Util
import androidx.media3.exoplayer.BaseRenderer
import androidx.media3.exoplayer.RendererCapabilities
import androidx.media3.exoplayer.metadata.MetadataOutput
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.extractor.metadata.id3.ChapterFrame
import androidx.media3.extractor.metadata.id3.Id3Frame
import androidx.media3.extractor.metadata.id3.TextInformationFrame
import ch.srgssr.pillarbox.player.data.Chapter
import kotlin.time.Duration.Companion.microseconds
import kotlin.time.Duration.Companion.milliseconds

/**
 * Pillarbox metadata renderer inspired by [MetadataRenderer].
 *
 * Currently simulated only hard coded chapters.
 * The method Player.onMetadata is spammed until the current player time is not in a chapter.
 *
 * @param output
 * @param outputLooper
 */
@SuppressLint("LongLogTag")
class PillarboxMetadataRenderer(
    private val output: MetadataOutput,
    outputLooper: Looper
) : BaseRenderer(TRACK_TYPE), Callback {
    private val outputHandler = outputLooper.let { Util.createHandler(it, this) }
    private val chapters = listOf(
        Chapter("urn:chapter_1", "Chapter1", 0, 5_000),
        Chapter("urn:chapter_2", "Chapter2", 20_000, 30_000),
    )
    private var outputStreamOffsetUs: Long = C.TIME_UNSET
    private var pendingMetadata: Metadata? = null

    override fun getName(): String {
        return TAG
    }

    override fun onStreamChanged(formats: Array<out Format>, startPositionUs: Long, offsetUs: Long, mediaPeriodId: MediaSource.MediaPeriodId) {
        outputStreamOffsetUs = offsetUs
    }

    override fun render(positionUs: Long, elapsedRealtimeUs: Long) {
        val presentationTimeUs = positionUs - outputStreamOffsetUs
        Log.d(TAG, "render position = ${presentationTimeUs.microseconds} elapsedRealTimeUs = ${elapsedRealtimeUs.microseconds}")
        val chapter = chapters.firstOrNull {
            presentationTimeUs.microseconds >= it.startPresentationTimeMs.milliseconds && presentationTimeUs.microseconds <= it.endPresentationTimeMs
                .milliseconds
        }

        chapter?.let {
            val frames: Array<Id3Frame> = arrayOf(TextInformationFrame("title", "description", listOf(chapter.title)))
            val chapterFrame = ChapterFrame(it.id, it.startPresentationTimeMs.toInt(), it.endPresentationTimeMs.toInt(), 0, 0, frames)
            invokeRenderer(Metadata(presentationTimeUs, chapterFrame))
        }
    }

    override fun isReady(): Boolean {
        return true
    }

    override fun isEnded(): Boolean {
        return false
    }

    override fun supportsFormat(format: Format): Int {
        if (format.sampleMimeType == MIME_TYPE) {
            return RendererCapabilities.create(C.FORMAT_HANDLED)
        }
        return RendererCapabilities.create(C.FORMAT_UNSUPPORTED_TYPE)
    }

    private fun invokeRenderer(metadata: Metadata) {
        outputHandler.obtainMessage(MSG_INVOKE_RENDERER, metadata).sendToTarget()
    }

    private fun invokeRendererInternal(metadata: Metadata) {
        output.onMetadata(metadata)
    }

    override fun handleMessage(msg: Message): Boolean {
        return when (msg.what) {
            MSG_INVOKE_RENDERER -> {
                invokeRendererInternal(msg.obj as Metadata)
                true
            }

            else -> {
                // Should not happens!
                true
            }
        }
    }

    companion object {
        private const val TAG = "PillarboxMetadataRenderer"
        private const val TRACK_TYPE = C.TRACK_TYPE_NONE
        const val MIME_TYPE = MimeTypes.BASE_TYPE_APPLICATION + "/chapters"
        private const val MSG_INVOKE_RENDERER = 0
    }
}
