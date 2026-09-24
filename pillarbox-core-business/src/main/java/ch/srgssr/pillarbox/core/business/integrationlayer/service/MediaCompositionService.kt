/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaCompositionResponse

/**
 * Represents a service responsible for fetching [MediaComposition]s.
 */
interface MediaCompositionService {
    /**
     * Fetches the [MediaCompositionResponse] located at the provided [uri], along with the headers of the response it was fetched from.
     *
     * ```kotlin
     * val mediaCompositionResponseResult = mediaCompositionService.fetchMediaComposition(uri)
     * val mediaCompositionResponse = mediaCompositionResponseResult.getOrNull()
     * if (mediaCompositionResponse == null) {
     *     val throwable = mediaCompositionResponseResult.exceptionOrNull()
     *     // Handle error
     * } else {
     *     // Do something with mediaCompositionResponse.mediaComposition and mediaCompositionResponse.headers
     * }
     * ```
     *
     * @param uri The URI identifying the desired [MediaComposition].
     * @return A [Result] containing either the successfully fetched [MediaCompositionResponse] or an error indicating the reason for failure.
     */
    suspend fun fetchMediaComposition(uri: Uri): Result<MediaCompositionResponse>
}
