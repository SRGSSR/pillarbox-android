/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srg.pillarbox.core.business.integrationlayer.service

import ch.srg.pillarbox.core.business.integrationlayer.data.MediaComposition

/**
 * Media composition data source interface used by [ch.srg.pillarbox.core.business.UrnMediaItemSource]
 */
interface MediaCompositionDataSource {
    /**
     * Get media composition by urn
     *
     * @param urn
     * @return RemoteResult.Success or RemoteResult.Error
     */
    suspend fun getMediaCompositionByUrn(urn: String): RemoteResult<MediaComposition>
}
