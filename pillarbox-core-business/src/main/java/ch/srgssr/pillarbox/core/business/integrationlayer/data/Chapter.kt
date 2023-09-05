/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Chapter
 *
 * @property urn
 * @property title
 * @property imageUrl
 * @property lead
 * @property description
 * @property blockReason
 * @property listSegment
 * @property listResource
 * @property comScoreAnalyticsLabels
 * @property analyticsLabels
 */
@Serializable
data class Chapter(
    val urn: String,
    val title: String,
    val imageUrl: String,
    val lead: String? = null,
    val description: String? = null,
    val blockReason: String? = null,
    @SerialName("segmentList")
    val listSegment: List<Segment>? = null,
    @SerialName("resourceList") val listResource: List<Resource>? = null,
    @SerialName("analyticsData")
    override val comScoreAnalyticsLabels: Map<String, String>? = null,
    @SerialName("analyticsMetadata")
    override val analyticsLabels: Map<String, String>? = null,
) : DataWithAnalytics
