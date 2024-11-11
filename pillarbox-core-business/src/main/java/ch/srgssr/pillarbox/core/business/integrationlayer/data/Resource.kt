/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable

/**
 * Represents a media resource.
 *
 * @property url The URL of the resource.
 * @property type The type of the resource.
 * @property tokenType The type of token required to access the resource.
 * @property drmList A list of DRM configurations for the resource, if applicable.
 * @property comScoreAnalyticsLabels Labels for ComScore analytics.
 * @property analyticsLabels Labels for Commanders Act analytics.
 */
@Serializable
data class Resource(
    val url: String,
    @SerialName("streaming") val type: Type,
    val tokenType: TokenType = TokenType.NONE,
    val drmList: List<Drm>? = null,
    @SerialName("analyticsData")
    override val comScoreAnalyticsLabels: Map<String, String>? = null,
    @SerialName("analyticsMetadata")
    override val analyticsLabels: Map<String, String>? = null,
) : DataWithAnalytics {

    /**
     * Represents the type of resource.
     */
    @Suppress("UndocumentedPublicProperty")
    enum class Type {
        PROGRESSIVE, M3UPLAYLIST, HLS, HDS, RTMP, DASH, UNKNOWN
    }

    /**
     * Represents the type of token.
     */
    @Suppress("UndocumentedPublicProperty")
    enum class TokenType { AKAMAI, NONE }
}
