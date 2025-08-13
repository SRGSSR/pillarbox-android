/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource.Companion.TRACK_TYPE_PILLARBOX_TRACKERS

/**
 * Helper for creating [TrackGroup] for [PillarboxMetadata].
 */
object PillarboxMetadataTrackGroup {
    /**
     * This MIME type is used to identify assets containing [PillarboxMetadata].
     */
    const val PILLARBOX_ASSET_METADATA_MIME_TYPE = "${MimeTypes.BASE_TYPE_APPLICATION}/pillarbox-asset-metadata"

    /**
     * This track type is used to identify tracks containing [PillarboxMetadata].
     */
    const val TRACK_TYPE_PILLARBOX_METADATA = TRACK_TYPE_PILLARBOX_TRACKERS + 1

    init {
        MimeTypes.registerCustomMimeType(PILLARBOX_ASSET_METADATA_MIME_TYPES, "pillarbox", TRACK_TYPE_PILLARBOX_METADATA)
    }

    /**
     * Creates a [TrackGroup] for [PillarboxMetadata].
     */
    fun createTrackGroup(metadata: PillarboxMetadata): TrackGroup {
        return TrackGroup(createFormat(metadata))
    }

    /**
     * Creates a [Format] for [PillarboxMetadata].
     */
    fun createFormat(metadata: PillarboxMetadata): Format {
        return Format.Builder()
            .setContainerMimeType(PILLARBOX_ASSET_METADATA_MIME_TYPES)
            .setCustomData(metadata)
            .build()
    }
}
