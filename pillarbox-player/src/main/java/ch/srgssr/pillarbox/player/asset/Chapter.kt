/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.MediaMetadata

/**
 * Chapter
 *
 * @property id The id of the chapter.
 * @property start The start position, in milliseconds.
 * @property end The end position, in milliseconds.
 * @property mediaMetadata The [MediaMetadata].
 */
data class Chapter(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val mediaMetadata: MediaMetadata
) : TimeRange, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        MediaMetadata.fromBundle(parcel.readBundle(Chapter::class.java.classLoader)!!)
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeLong(start)
        parcel.writeLong(end)
        parcel.writeBundle(mediaMetadata.toBundle())
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Creator create a [Chapter]
     */
    companion object CREATOR : Parcelable.Creator<Chapter> {
        override fun createFromParcel(parcel: Parcel): Chapter {
            return Chapter(parcel)
        }

        override fun newArray(size: Int): Array<Chapter?> {
            return arrayOfNulls(size)
        }
    }
}
