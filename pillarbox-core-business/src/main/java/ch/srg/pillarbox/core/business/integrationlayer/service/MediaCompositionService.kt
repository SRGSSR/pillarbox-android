/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.integrationlayer.service

import ch.srg.pillarbox.core.business.integrationlayer.data.MediaComposition
import retrofit2.http.GET
import retrofit2.http.Path

/**
 * Retrofit media composition service from Integration layer
 */
interface MediaCompositionService {

    /**
     * Get MediaComposition by urn
     *
     * @param urn of the content
     */
    @GET("integrationlayer/2.1/mediaComposition/byUrn/{urn}")
    suspend fun getMediaCompositionByUrn(@Path("urn") urn: String): MediaComposition
}
