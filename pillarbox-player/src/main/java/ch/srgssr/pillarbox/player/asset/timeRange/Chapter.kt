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
 * Chapter
 *
 * @property id The id of the chapter.
 * @property start The start position, in milliseconds.
 * @property end The end position, in milliseconds.
 * @property mediaMetadata The [MediaMetadata].
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
