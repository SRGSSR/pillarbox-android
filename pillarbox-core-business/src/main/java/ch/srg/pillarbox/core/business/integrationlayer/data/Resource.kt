/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.integrationlayer.data

import com.google.gson.annotations.SerializedName

/**
 * Resource
 *
 * @property url
 * @property type
 * @property tokenType
 * @constructor Create empty Resource
 */
data class Resource(
    val url: String,
    @SerializedName("streaming") val type: Type,
    val tokenType: TokenType = TokenType.NONE
) {

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
