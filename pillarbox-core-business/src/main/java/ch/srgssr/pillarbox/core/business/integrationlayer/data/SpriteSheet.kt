/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.Serializable

/**
 * Represents a sprite sheet containing multiple thumbnail images arranged in a grid.
 *
 * @property urn The URN of the media.
 * @property rows The number of rows in the sprite sheet.
 * @property columns The number of columns in the sprite sheet.
 * @property thumbnailHeight The height of each thumbnail image, in pixels.
 * @property thumbnailWidth The width of each thumbnail image, in pixels.
 * @property interval The interval between two thumbnail images, in milliseconds.
 * @property url The URL of the sprite sheet image.
 */
@Serializable
data class SpriteSheet(
    val urn: String,
    val rows: Int,
    val columns: Int,
    val thumbnailHeight: Int,
    val thumbnailWidth: Int,
    val interval: Long,
    val url: String
)
