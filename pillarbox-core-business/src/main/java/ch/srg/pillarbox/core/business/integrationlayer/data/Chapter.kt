/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.integrationlayer.data

import com.google.gson.annotations.SerializedName

/**
 * Chapter
 *
 * @property urn
 * @property title
 * @property lead
 * @property description
 * @property blockReason
 *@property imageUrl
 * @property listResource
 */
data class Chapter(
    val urn: String,
    val title: String,
    val lead: String? = null,
    val description: String? = null,
    val blockReason: String? = null,
    val imageUrl: String,
    @SerializedName("resourceList") val listResource: List<Resource>? = null
)
