/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import android.os.Parcel
import android.os.Parcelable

/**
 * Blocked time range
 *
 * @property id The id of the chapter.
 * @property start The start position, in milliseconds.
 * @property end The end position, in milliseconds.
 * @property reason The block reason.
 */
data class BlockedTimeRange(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val reason: String
) : TimeRange, Parcelable {
    constructor(parcel: Parcel) : this(
        parcel.readString()!!,
        parcel.readLong(),
        parcel.readLong(),
        parcel.readString()!!
    )

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(id)
        parcel.writeLong(start)
        parcel.writeLong(end)
        parcel.writeString(reason)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Creator create a [BlockedTimeRange]
     */
    companion object CREATOR : Parcelable.Creator<BlockedTimeRange> {
        override fun createFromParcel(parcel: Parcel): BlockedTimeRange {
            return BlockedTimeRange(parcel)
        }

        override fun newArray(size: Int): Array<BlockedTimeRange?> {
            return arrayOfNulls(size)
        }
    }
}
