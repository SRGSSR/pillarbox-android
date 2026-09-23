/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import android.os.Bundle
import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.MediaMetadata
import kotlinx.parcelize.Parceler
import kotlinx.parcelize.Parcelize
import kotlinx.parcelize.TypeParceler

/**
 * Represents a chapter.
 *
 * A chapter is a segment of a media item defined by a start and end time.
 * It can also contain associated metadata, such as a title or image.
 *
 * @property id The unique identifier of the chapter.
 * @property start The start position of the chapter, in milliseconds.
 * @property end The end position of the chapter, in milliseconds.
 * @property mediaMetadata The [MediaMetadata] associated with the chapter.
 */
@Parcelize
data class Chapter(
    val id: String,
    override val start: Long,
    override val end: Long,
    @TypeParceler<MediaMetadata, MediaMetadataParceler>()
    val mediaMetadata: MediaMetadata
) : TimeRange, Parcelable

internal object MediaMetadataParceler : Parceler<MediaMetadata> {
    override fun create(parcel: Parcel): MediaMetadata {
        return MediaMetadata.fromBundle(parcel.readBundle(MediaMetadata::class.java.classLoader) ?: Bundle())
    }

    override fun MediaMetadata.write(parcel: Parcel, flags: Int) {
        parcel.writeBundle(toBundle())
    }
}
