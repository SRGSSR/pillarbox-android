/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.Serializable

/**
 * Data class representing a sprite sheet for thumbnails.
 *
 * A sprite sheet is a single image containing multiple thumbnails arranged in a grid.
 * This class stores metadata about the sprite sheet, such as its dimensions,
 * individual thumbnail dimensions, and the URL of the sprite sheet image.
 *
 * @property urn Unique identifier for the sprite sheet.
 * @property rows Number of rows in the sprite sheet.
 * @property columns Number of columns in the sprite sheet.
 * @property thumbnailHeight Height of each thumbnail image.
 * @property thumbnailWidth Width of each thumbnail image.
 * @property interval Currently unused. This field might be a remnant from a previous implementation and can potentially be removed.
 * @property url URL of the sprite sheet image.
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
