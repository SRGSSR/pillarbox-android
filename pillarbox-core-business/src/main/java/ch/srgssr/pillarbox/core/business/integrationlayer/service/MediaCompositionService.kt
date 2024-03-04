/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition

/**
 * Media composition data source interface used by [ch.srgssr.pillarbox.core.business.MediaCompositionMediaItemSource]
 */
interface MediaCompositionService {
    /**
     * Get media composition by urn
     *
     * @param uri Uri to get MediaComposition.
     * @return Result
     */
    suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition>
}
