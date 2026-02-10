/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.standard

import android.net.Uri
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData

abstract class PlayerDataMapper<CustomData> {

    abstract fun PlayerData<CustomData>.pillarboxMetadata(): PillarboxMetadata

    open fun PlayerData<CustomData>.mediaMetadata(): MediaMetadata {
        return MediaMetadata.Builder().apply {
            title?.let {
                setTitle(it)
                setDisplayTitle(it)
            }
            subtitle?.let { setSubtitle(it) }
            posterUrl?.let { setArtworkUri(Uri.parse(it)) }
        }.build()
    }

    open fun PlayerData<CustomData>.mediaItemTrackerData(mutableMediaItemTrackerData: MutableMediaItemTrackerData) = Unit
}
