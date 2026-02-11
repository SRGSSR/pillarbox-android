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
    val chapters: List<Chapter>? = null,
    val timeRanges: List<TimeRange>? = null,
    val customData: CustomData? = null,
) {

    @Serializable
    data class Source(
        val url: String,
        val mimeType: String? = null,
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
    ) {
        fun isBlocked(): Boolean {
            return type == BLOCKED
        }

        fun isOpeningCredits(): Boolean {
            return type == OPENING_CREDITS
        }

        fun isClosingCredits(): Boolean {
            return type == CLOSING_CREDITS
        }

        companion object {
            const val BLOCKED = "BLOCKED"
            const val OPENING_CREDITS = "OPENING_CREDITS"
            const val CLOSING_CREDITS = "CLOSING_CREDITS"
        }
    }

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
