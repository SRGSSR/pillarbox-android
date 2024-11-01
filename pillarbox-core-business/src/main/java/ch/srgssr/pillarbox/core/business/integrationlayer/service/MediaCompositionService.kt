/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.MediaComposition

/**
 * Represents a service responsible for fetching [MediaComposition]s.
 */
fun interface MediaCompositionService {
    /**
     * Fetches a [MediaComposition] located at the provided [uri].
     *
     * ```kotlin
     * val mediaCompositionResult = mediaCompositionService.fetchMediaComposition(uri)
     * val mediaComposition = mediaCompositionResult.getOrNull()
     * if (mediaComposition == null) {
     *     val throwable = mediaCompositionResult.exceptionOrNull()
     *     // Handle error
     * } else {
     *     // Do something with the media composition
     * }
     * ```
     *
     * @param uri The URI identifying the desired [MediaComposition].
     * @return A [Result] containing either the successfully fetched [MediaComposition] or an error indicating the reason for failure.
     */
    suspend fun fetchMediaComposition(uri: Uri): Result<MediaComposition>
}
