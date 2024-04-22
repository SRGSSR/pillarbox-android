/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.extension

import androidx.media3.common.MediaItem
import ch.srgssr.pillarbox.player.asset.PillarboxData

internal fun MediaItem.Builder.setPillarboxData(data: PillarboxData): MediaItem.Builder {
    setTag(data)
    return this
}

/**
 * @return `null` if there is no tag in this MediaItem, otherwise the [PillarboxData] associated with this [MediaItem].
 */
fun MediaItem?.getPillarboxDataOrNull(): PillarboxData? {
    return this?.localConfiguration?.tag as PillarboxData?
}

/**
 * A [PillarboxData], or [PillarboxData.EMPTY] if there is no data in [MediaItem.localConfiguration].
 */
val MediaItem.pillarboxData: PillarboxData
    get() = getPillarboxDataOrNull() ?: PillarboxData.EMPTY
