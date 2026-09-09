/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.data

/**
 * Represents a media composition response.
 *
 * @property mediaComposition The fetched [MediaComposition].
 * @property headers The headers of the response, keyed by header name. A header name may appear multiple times in a response, hence the list of
 * values. Implementations that don't perform a network request may leave it empty.
 */
data class MediaCompositionResponse(
    val mediaComposition: MediaComposition,
    val headers: Map<String, List<String>> = emptyMap(),
) {
    val usefulHeaders: Map<String, List<String>>
        get() = headers.filterKeys { key -> MONITORED_METADATA_HEADERS.contains(key) }

    companion object {
        /**
         * The names of the response headers needed for analytics/monitoring
         */
        val MONITORED_METADATA_HEADERS = listOf(
            "akamai-grn",
            "x-location-info",
            "x-proxy-detection-info",
            "x-tracing-id",
        )
    }
}
