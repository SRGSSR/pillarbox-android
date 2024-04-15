/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import android.os.Parcel
import android.os.Parcelable

/**
 * Blocked section
 *
 * @property id
 * @property start
 * @property end
 * @property reason
 * @constructor Create empty Blocked section
 */
data class BlockedInterval(
    override val id: String,
    override val start: Long,
    override val end: Long,
    val reason: String
) : TimeInterval, Parcelable {
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

    companion object CREATOR : Parcelable.Creator<BlockedInterval> {
        override fun createFromParcel(parcel: Parcel): BlockedInterval {
            return BlockedInterval(parcel)
        }

        override fun newArray(size: Int): Array<BlockedInterval?> {
            return arrayOfNulls(size)
        }
    }
}
