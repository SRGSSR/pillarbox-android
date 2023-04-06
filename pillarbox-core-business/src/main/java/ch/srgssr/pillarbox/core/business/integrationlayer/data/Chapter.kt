/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Chapter
 *
 * @property urn
 * @property title
 * @property imageUrl
 * @property lead
 * @property description
 * @property blockReason
 * @property listResource
 * @property comScoreAnalyticsLabels
 * @property analyticsLabels
 */
@JsonClass(generateAdapter = true)
data class Chapter(
    val urn: String,
    val title: String,
    val imageUrl: String,
    val lead: String? = null,
    val description: String? = null,
    val blockReason: String? = null,
    @Json(name = "resourceList") val listResource: List<Resource>? = null,
    @Json(name = "analyticsData")
    override val comScoreAnalyticsLabels: Map<String, String>? = null,
    @Json(name = "analyticsMetadata")
    override val analyticsLabels: Map<String, String>? = null,
) : DataWithAnalytics
