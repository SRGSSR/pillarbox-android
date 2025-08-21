/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset.timeRange

import android.os.Parcelable
import androidx.core.net.toUri
import androidx.media3.common.MediaMetadata
import kotlinx.parcelize.IgnoredOnParcel
import kotlinx.parcelize.Parcelize
import kotlinx.serialization.Serializable
import kotlinx.serialization.Transient

/**
 * Represents a chapter.
 *
 * A chapter is a segment of a media item defined by a start and end time.
 * It can also contain associated metadata, such as a title or image.
 *
 * @property id The unique identifier of the chapter.
 * @property start The start position of the chapter, in milliseconds.
 * @property end The end position of the chapter, in milliseconds.
 * @property title The title of the chapter.
 * @property description The description of the chapter.
 * @property artworkUri The artwork uri of the chapter.
 */
@Parcelize
@Serializable
data class Chapter(
    val id: String,
    override val start: Long,
    override val end: Long,
    val title: String,
    val description: String? = null,
    val artworkUri: String? = null,
) : TimeRange, Parcelable {

    /**
     * The [MediaMetadata] of the chapter build from fields.
     */
    @IgnoredOnParcel
    @Transient
    val mediaMetadata: MediaMetadata = MediaMetadata.Builder()
        .setTitle(title)
        .setDescription(description)
        .setArtworkUri(artworkUri?.toUri())
        .build()
}
