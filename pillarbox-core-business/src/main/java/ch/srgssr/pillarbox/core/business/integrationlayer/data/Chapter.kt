/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a [Chapter] within a media.
 *
 * @property urn Unique identifier for the chapter.
 * @property title Title of the chapter.
 * @property imageUrl URL of an image representing the chapter.
 * @property mediaType The type of media this chapter represents.
 * @property lead A short introductory text for the chapter.
 * @property description A detailed description of the chapter.
 * @property blockReason Reason for blocking the chapter, if applicable.
 * @property fullLengthUrn URN of the full-length media this chapter is a part of.
 * @property fullLengthMarkIn Start time of the chapter within the full-length media (in milliseconds).
 * @property fullLengthMarkOut End time of the chapter within the full-length media (in milliseconds).
 * @property listSegment List of segments within this chapter.
 * @property listResource List of resources associated with this chapter.
 * @property comScoreAnalyticsLabels Labels for ComScore analytics.
 * @property analyticsLabels Labels for Commanders Act analytics.
 * @property timeIntervalList List of time intervals relevant to the chapter.
 * @property validFrom The [Instant] when the [Chapter] becomes valid.
 * @property validTo The [Instant] until when the [Chapter] is valid.
 * @property spriteSheet The [SpriteSheet] information if available.
 */
@Serializable
data class Chapter(
    val urn: String,
    val title: String,
    val imageUrl: String,
    val mediaType: MediaType,
    val lead: String? = null,
    val description: String? = null,
    val blockReason: BlockReason? = null,
    val fullLengthUrn: String? = null,
    val fullLengthMarkIn: Long? = null,
    val fullLengthMarkOut: Long? = null,
    @SerialName("segmentList")
    val listSegment: List<Segment>? = null,
    @SerialName("resourceList") val listResource: List<Resource>? = null,
    @SerialName("analyticsData")
    override val comScoreAnalyticsLabels: Map<String, String>? = null,
    @SerialName("analyticsMetadata")
    override val analyticsLabels: Map<String, String>? = null,
    val timeIntervalList: List<TimeInterval>? = null,
    val validFrom: Instant? = null,
    val validTo: Instant? = null,
    val spriteSheet: SpriteSheet? = null,
) : DataWithAnalytics {
    /**
     * Indicates whether this represents a full-length chapter.
     */
    val isFullLengthChapter: Boolean = fullLengthUrn.isNullOrBlank()
}
