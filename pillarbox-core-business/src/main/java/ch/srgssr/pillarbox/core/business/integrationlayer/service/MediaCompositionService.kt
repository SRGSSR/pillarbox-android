/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition

/**
 * Media composition service
 */
fun interface MediaCompositionService {
    /**
     * Fetch media composition
     *
     * @param uri The uri of the [MediaComposition] to fetch.
     * @return Result
     */
    suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition>
}
