/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Information about the media being played.
 *
 * @property assetUrl The URL of the asset.
 * @property id The id of the media.
 * @property metadataUrl The URL of the metadata.
 * @property origin The origin of the media.
 */
@Serializable
data class QoSMedia(
    @SerialName("asset_url") val assetUrl: String,
    val id: String,
    @SerialName("metadata_url") val metadataUrl: String,
    val origin: String,
)
