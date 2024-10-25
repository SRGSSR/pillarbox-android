/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.Serializable

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
