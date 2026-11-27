/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.source

import androidx.media3.common.Format
import androidx.media3.common.MimeTypes
import androidx.media3.common.TrackGroup
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.UpdatableMediaItem
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource.Companion.TRACK_TYPE_PILLARBOX_TRACKERS
import kotlinx.coroutines.flow.Flow

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

    /**
     * This MIME type is used to identify assets containing a [Flow] of updates, built from
     * [Asset.updateFlow][ch.srgssr.pillarbox.player.asset.Asset.updateFlow].
     */
    const val PILLARBOX_ASSET_UPDATE_MIME_TYPE = "${MimeTypes.BASE_TYPE_APPLICATION}/pillarbox-asset-update"

    /**
     * This track type is used to identify tracks containing a [Flow] of updates, built from
     * [Asset.updateFlow][ch.srgssr.pillarbox.player.asset.Asset.updateFlow].
     */
    const val TRACK_TYPE_PILLARBOX_UPDATE = TRACK_TYPE_PILLARBOX_METADATA + 1

    init {
        MimeTypes.registerCustomMimeType(PILLARBOX_ASSET_METADATA_MIME_TYPE, "pillarbox", TRACK_TYPE_PILLARBOX_METADATA)
        MimeTypes.registerCustomMimeType(PILLARBOX_ASSET_UPDATE_MIME_TYPE, "pillarbox", TRACK_TYPE_PILLARBOX_UPDATE)
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
            .setContainerMimeType(PILLARBOX_ASSET_METADATA_MIME_TYPE)
            .setCustomData(metadata)
            .build()
    }

    /**
     * Creates a [TrackGroup] for a [Flow] of [UpdatableMediaItem].
     */
    internal fun createUpdateTrackGroup(updateFlow: Flow<UpdatableMediaItem>): TrackGroup {
        return TrackGroup(
            Format.Builder()
                .setContainerMimeType(PILLARBOX_ASSET_UPDATE_MIME_TYPE)
                .setCustomData(updateFlow)
                .build()
        )
    }
}
