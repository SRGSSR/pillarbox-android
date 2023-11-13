/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition

/**
 * Media composition data source interface used by [ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource]
 */
interface MediaCompositionDataSource {
    /**
     * Get media composition by urn
     *
     * @param urn Urn to get MediaComposition.
     * @return Result
     */
    suspend fun getMediaCompositionByUrn(urn: String): Result<MediaComposition>
}
