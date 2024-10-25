/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.datetime.Instant
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Chapter
 *
 * @property urn
 * @property title
 * @property imageUrl
 * @property mediaType
 * @property lead
 * @property description
 * @property blockReason
 * @property fullLengthUrn
 * @property fullLengthMarkIn
 * @property fullLengthMarkOut
 * @property listSegment
 * @property listResource
 * @property comScoreAnalyticsLabels
 * @property analyticsLabels
 * @property timeIntervalList
 * @property validFrom The [Instant] when the [Chapter] becomes valid.
 * @property validTo The [Instant] until when the [Chapter] is valid.
 * @property spriteSheet The [SpriteSheet] information if available.
 * @constructor Create empty Chapter
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
     * If it is a full length chapter.
     */
    val isFullLengthChapter: Boolean = fullLengthUrn.isNullOrBlank()
}
