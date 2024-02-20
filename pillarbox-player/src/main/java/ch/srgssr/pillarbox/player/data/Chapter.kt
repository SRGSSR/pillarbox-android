/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.data

/**
 * Chapter
 *
 * @property id
 * @property title
 * @property startPresentationTimeMs
 * @property endPresentationTimeMs
 * @property imageUrl
 */
data class Chapter(
    val id: String,
    val title: String,
    val startPresentationTimeMs: Long,
    val endPresentationTimeMs: Long,
    val imageUrl: String? = null
)
