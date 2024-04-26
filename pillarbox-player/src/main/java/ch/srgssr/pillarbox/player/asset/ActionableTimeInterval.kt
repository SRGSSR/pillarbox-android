/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import android.os.Parcel
import android.os.Parcelable

/**
 * Actionable time interval
 *
 * @property id
 * @property start
 * @property end
 */
data class ActionableTimeInterval(
    override val id: String,
    override val start: Long,
    override val end: Long,
) : TimeInterval, Parcelable {
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
     * Creator create a [ActionableTimeInterval]
     */
    companion object CREATOR : Parcelable.Creator<ActionableTimeInterval> {
        override fun createFromParcel(source: Parcel): ActionableTimeInterval {
            return ActionableTimeInterval(source)
        }

        override fun newArray(size: Int): Array<ActionableTimeInterval?> {
            return arrayOfNulls(size)
        }
    }
}
