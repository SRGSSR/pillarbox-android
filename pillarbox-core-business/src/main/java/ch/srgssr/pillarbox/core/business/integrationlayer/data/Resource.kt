/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import com.squareup.moshi.Json
import com.squareup.moshi.JsonClass

/**
 * Resource
 *
 * @property url
 * @property type
 * @property tokenType
 * @property drmList
 * @property comScoreAnalyticsLabels
 * @property analyticsLabels
 */
@JsonClass(generateAdapter = true)
data class Resource(
    val url: String,
    @Json(name = "streaming") val type: Type,
    val tokenType: TokenType = TokenType.NONE,
    val drmList: List<Drm>? = null,
    @Json(name = "analyticsData")
    override val comScoreAnalyticsLabels: Map<String, String>? = null,
    @Json(name = "analyticsMetadata")
    override val analyticsLabels: Map<String, String>? = null,
) : DataWithAnalytics {

    /**
     * Type of the Resource
     */
    enum class Type {
        PROGRESSIVE, M3UPLAYLIST, HLS, HDS, RTMP, DASH, UNKNOWN
    }

    /**
     * Token type
     */
    enum class TokenType { AKAMAI, NONE }
}
