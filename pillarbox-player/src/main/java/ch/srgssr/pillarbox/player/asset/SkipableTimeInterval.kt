/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import android.os.Parcel
import android.os.Parcelable

/**
 * Skipable time interval
 *
 * @property id The id of the time interval.
 * @property start The start time, in milliseconds, of the time interval.
 * @property end The end time, in milliseconds, of the time interval.
 */
data class SkipableTimeInterval(
    override val id: String,
    override val start: Long,
    override val end: Long,
) : TimeInterval, Parcelable {
    /**
     * The type of time interval.
     */
    val type: SkipableTimeIntervalType?
        get() = runCatching {
            enumValueOf<SkipableTimeIntervalType>(id)
        }.getOrNull()

    constructor(parcel: Parcel) : this(
        id = parcel.readString()!!,
        start = parcel.readLong(),
        end = parcel.readLong(),
    )

    override fun writeToParcel(dest: Parcel, flags: Int) {
        dest.writeString(id)
        dest.writeLong(start)
        dest.writeLong(end)
    }

    override fun describeContents(): Int {
        return 0
    }

    /**
     * Creator create a [SkipableTimeInterval]
     */
    companion object CREATOR : Parcelable.Creator<SkipableTimeInterval> {
        override fun createFromParcel(source: Parcel): SkipableTimeInterval {
            return SkipableTimeInterval(source)
        }

        override fun newArray(size: Int): Array<SkipableTimeInterval?> {
            return arrayOfNulls(size)
        }
    }
}
