/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.qos.models

/**
 * Information about the media being played.
 *
 * @property assetUrl The URL of the asset.
 * @property id The id of the media.
 * @property metadataUrl The URL of the metadata.
 * @property origin The origin of the media.
 */
data class QoSMedia(
    val assetUrl: String,
    val id: String,
    val metadataUrl: String,
    val origin: String,
)
