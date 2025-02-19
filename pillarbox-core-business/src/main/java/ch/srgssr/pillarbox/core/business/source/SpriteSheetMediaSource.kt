/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import android.graphics.Bitmap
import androidx.media3.common.MediaItem
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.BaseMediaSource
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.SinglePeriodTimeline
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.core.business.integrationlayer.data.SpriteSheet
import kotlin.coroutines.CoroutineContext
import kotlin.time.Duration.Companion.milliseconds

/**
 * An implementation of a [BaseMediaSource] that loads a [SpriteSheet].
 *
 * @param spriteSheet The [SpriteSheet] to build thumbnails.
 * @param mediaItem The [MediaItem].
 * @param spriteSheetLoader The [SpriteSheetLoader] to use to load a [Bitmap] from a [SpriteSheet].
 * @param coroutineContext The [CoroutineContext] to use for loading the [Bitmap].
 */
internal class SpriteSheetMediaSource(
    private val spriteSheet: SpriteSheet,
    private val mediaItem: MediaItem,
    private val spriteSheetLoader: SpriteSheetLoader,
    private val coroutineContext: CoroutineContext,
) : BaseMediaSource() {

    override fun getMediaItem(): MediaItem {
        return mediaItem
    }

    override fun maybeThrowSourceInfoRefreshError() = Unit

    override fun createPeriod(id: MediaSource.MediaPeriodId, allocator: Allocator, startPositionUs: Long): MediaPeriod {
        return SpriteSheetMediaPeriod(spriteSheet, spriteSheetLoader, coroutineContext)
    }

    override fun releasePeriod(mediaPeriod: MediaPeriod) {
        (mediaPeriod as SpriteSheetMediaPeriod).releasePeriod()
    }

    override fun prepareSourceInternal(mediaTransferListener: TransferListener?) {
        val duration = (spriteSheet.rows * spriteSheet.columns * spriteSheet.interval).milliseconds
        val timeline = SinglePeriodTimeline(duration.inWholeMicroseconds, true, false, false, null, mediaItem)
        refreshSourceInfo(timeline)
    }

    override fun releaseSourceInternal() = Unit
}
