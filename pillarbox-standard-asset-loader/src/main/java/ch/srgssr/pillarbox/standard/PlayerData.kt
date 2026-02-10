/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.standard

import kotlinx.serialization.Serializable

@Serializable
class PlayerData<CustomData>(
    val identifier: String? = null,
    val title: String? = null,
    val subtitle: String? = null,
    val description: String? = null,
    val posterUrl: String? = null,
    val source: Source? = null,
    val drm: Drm? = null,
    val subtitles: Subtitle? = null,
    val chapters: List<Chapter>? = null,
    val timeRanges: List<TimeRange>? = null,
    val customData: CustomData? = null,
) {

    @Serializable
    data class Source(
        val url: String,
        val type: Type,
        val mimeType: String? = null,
        val videoFragment: String? = null,
        val audioFragment: String? = null,
    ) {
        enum class Type {
            VIDEO,
            AUDIO,
        }
    }

    @Serializable
    data class Subtitle(
        val label: String,
        val kind: String,
        val language: String,
        val url: String,
    )

    @Serializable
    data class Chapter(
        val identifier: String?,
        val title: String?,
        val posterUrl: String?,
        val startTime: Long,
        val endTime: Long,
    )

    @Serializable
    data class TimeRange(
        val startTime: Long,
        val endTime: Long,
        val type: String,
    )

    @Serializable
    data class Drm(
        val keySystem: KeySystem,
        val licenseUrl: String,
        val multisession: Boolean,
    ) {
        enum class KeySystem {
            WIDEVINE,
            PLAYREADY,
            CLEAR_KEY,
        }
    }
}
