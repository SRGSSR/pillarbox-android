/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import android.os.Parcel
import android.os.Parcelable
import androidx.media3.common.Metadata

class PillarboxMetadata(private val pillarboxData: String = "Pillarbox est arrivée") : Metadata.Entry {
    constructor(parcel: Parcel) : this(
        parcel.readString() ?: "Unknown",
    ) {
    }

    override fun writeToParcel(parcel: Parcel, flags: Int) {
        parcel.writeString(pillarboxData)
    }

    override fun describeContents(): Int {
        return 0
    }

    companion object CREATOR : Parcelable.Creator<PillarboxMetadata> {
        override fun createFromParcel(parcel: Parcel): PillarboxMetadata {
            return PillarboxMetadata(parcel)
        }

        override fun newArray(size: Int): Array<PillarboxMetadata?> {
            return arrayOfNulls(size)
        }
    }
}
