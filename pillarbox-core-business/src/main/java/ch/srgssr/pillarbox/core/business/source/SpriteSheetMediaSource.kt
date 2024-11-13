/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.source

import androidx.media3.common.MediaItem
import androidx.media3.datasource.TransferListener
import androidx.media3.exoplayer.source.BaseMediaSource
import androidx.media3.exoplayer.source.MediaPeriod
import androidx.media3.exoplayer.source.MediaSource
import androidx.media3.exoplayer.source.SinglePeriodTimeline
import androidx.media3.exoplayer.upstream.Allocator
import ch.srgssr.pillarbox.core.business.integrationlayer.data.SpriteSheet
import kotlin.time.Duration.Companion.milliseconds

/**
 * Sprite sheet media source
 *
 * @param spriteSheet The [SpriteSheet] to build thumbnails.
 */
class SpriteSheetMediaSource(
    private val spriteSheet: SpriteSheet
) : BaseMediaSource() {

    private val mediaItem: MediaItem = MediaItem.Builder()
        .setUri(spriteSheet.url)
        .setTag(spriteSheet)
        .build()

    override fun getMediaItem(): MediaItem {
        return mediaItem
    }

    override fun canUpdateMediaItem(mediaItem: MediaItem): Boolean {
        return this.mediaItem.localConfiguration == mediaItem.localConfiguration
    }

    override fun maybeThrowSourceInfoRefreshError() = Unit

    override fun createPeriod(id: MediaSource.MediaPeriodId, allocator: Allocator, startPositionUs: Long): MediaPeriod {
        return SpriteSheetMediaPeriod(spriteSheet)
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
