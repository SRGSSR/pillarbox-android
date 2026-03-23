/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.standard

import kotlinx.serialization.Serializable

/**
 * Data representing a playable item and its associated metadata.
 *
 * This class encapsulates all the information required to load and display a media resource,
 * including its source URL, DRM configuration, descriptive metadata, and navigational
 * elements like chapters or blocked time ranges.
 *
 * @param CustomData The type of custom data associated with this player data.
 * @property identifier The unique identifier for this media.
 * @property title The title of the media.
 * @property subtitle The subtitle of the media.
 * @property description A description of the media.
 * @property posterUrl The URL of the poster image.
 * @property source The source information for the media (URL and MIME type).
 * @property drm The DRM configuration for the media.
 * @property chapters The list of chapters for the media.
 * @property timeRanges The list of time ranges (e.g., blocked segments, credits) for the media.
 * @property customData Custom data associated with this media.
 */
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

    /**
     * Represents a media source.
     *
     * @property url The URL of the media.
     * @property mimeType The MIME type of the media, if known.
     */
    @Serializable
    data class Source(
        val url: String,
        val mimeType: String? = null,
    )

    /**
     * Represents a chapter within a media item.
     *
     * Chapters are used to divide a media into distinct sections, each with its own title
     * and time range. They can be used for navigation, such as seeking to the beginning of a chapter.
     *
     * @property identifier A unique identifier for the chapter.
     * @property title The title of the chapter, displayed to the user.
     * @property posterUrl An optional URL for an image to represent the chapter.
     * @property startTime The start time of the chapter, in milliseconds.
     * @property endTime The end time of the chapter, in milliseconds.
     */
    @Serializable
    data class Chapter(
        val identifier: String?,
        val title: String,
        val posterUrl: String?,
        val startTime: Long,
        val endTime: Long,
    )

    /**
     * Represents a specific time range within the media, such as a blocked segment or credits.
     *
     * These ranges can be used to implement features like skipping intros/credits or
     * preventing seeking into certain parts of the content.
     *
     * @property startTime The start time of the range in milliseconds.
     * @property endTime The end time of the range in milliseconds.
     * @property type The type of the time range, indicating its purpose (e.g., [BLOCKED], [OPENING_CREDITS]).
     */
    @Serializable
    data class TimeRange(
        val startTime: Long,
        val endTime: Long,
        val type: String,
    ) {
        /**
         * Checks if the time range is of type [BLOCKED].
         */
        fun isBlocked(): Boolean {
            return type == BLOCKED
        }

        /**
         * Checks if the time range is of type [OPENING_CREDITS].
         */
        fun isOpeningCredits(): Boolean {
            return type == OPENING_CREDITS
        }

        /**
         * Checks if the time range is of type [CLOSING_CREDITS].
         */
        fun isClosingCredits(): Boolean {
            return type == CLOSING_CREDITS
        }

        @Suppress("UndocumentedPublicProperty")
        companion object {
            const val BLOCKED = "BLOCKED"
            const val OPENING_CREDITS = "OPENING_CREDITS"
            const val CLOSING_CREDITS = "CLOSING_CREDITS"
        }
    }

    /**
     * Represents DRM configuration for a media item.
     * @property keySystem The key system used for DRM (e.g., WIDEVINE, PLAYREADY, CLEAR_KEY).
     * @property licenseUrl The license URL for the DRM system.
     * @property multisession Whether the DRM system supports multisession playback.
     */
    @Serializable
    data class Drm(
        val keySystem: KeySystem,
        val licenseUrl: String,
        val multisession: Boolean,
    ) {
        /**
         * Represents the key system used for DRM.
         */
        @Suppress("UndocumentedPublicProperty")
        enum class KeySystem {
            WIDEVINE,
            PLAYREADY,
            CLEAR_KEY,
        }
    }
}
