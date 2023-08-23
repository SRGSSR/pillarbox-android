/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import retrofit2.http.GET
import retrofit2.http.Path
import retrofit2.http.Query

/**
 * Retrofit media composition service from Integration layer
 */
interface MediaCompositionService {

    /**
     * Get MediaComposition by urn
     *
     * @param urn Urn of the content.
     * @param onlyChapters Only chapters, no segments are delivered.
     * @param vector Distribution vector.
     */
    @GET("integrationlayer/2.1/mediaComposition/byUrn/{urn}")
    suspend fun getMediaCompositionByUrn(
        @Path("urn") urn: String,
        @Query("onlyChapters") onlyChapters: Boolean = true,
        @Query("vector") vector: String = Vector.TV
    ): MediaComposition
}
