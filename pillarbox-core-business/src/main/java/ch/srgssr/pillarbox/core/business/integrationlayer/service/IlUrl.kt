/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn

/**
 * @property host The [IlHost] to use.
 * @property urn The URN of the media to request.
 * @property vector The [Vector] to use.
 * @property forceSAM Force SAM usage.
 * @property ilLocation The [IlLocation] of the request.
 */
data class IlUrl(
    val host: IlHost,
    val urn: String,
    val vector: Vector,
    val forceSAM: Boolean = false,
    val ilLocation: IlLocation? = null,
) {

    init {
        require(urn.isValidMediaUrn())
    }

    /**
     * [Uri] representation of this [IlUrl].
     */
    val uri: Uri = Uri.parse(host.baseHostUrl).buildUpon().apply {
        if (forceSAM) {
            appendEncodedPath("sam")
            appendQueryParameter(PARAM_FORCE_SAM, true.toString())
        }
        appendEncodedPath(PATH)
        appendEncodedPath(urn)
        ilLocation?.let {
            appendQueryParameter(PARAM_FORCE_LOCATION, it.toString())
        }
        appendQueryParameter(PARAM_VECTOR, vector.toString())
        appendQueryParameter(PARAM_ONLY_CHAPTERS, true.toString())
    }.build()

    internal companion object {
        private const val PARAM_ONLY_CHAPTERS = "onlyChapters"
        private const val PARAM_FORCE_SAM = "forceSAM"
        private const val PARAM_FORCE_LOCATION = "forceLocation"
        private const val PARAM_VECTOR = "vector"
        private const val PATH = "integrationlayer/2.1/mediaComposition/byUrn/"

        /**
         * Converts an [Uri] into a valid [IlUrl].
         *
         * @return An [IlUrl] or throws an [IllegalArgumentException] if the [Uri] can't be parsed.
         */
        internal fun Uri.toIlUrl(): IlUrl {
            val urn = lastPathSegment
            require(urn.isValidMediaUrn()) { "Invalid URN $urn found in $this" }
            val host = IlHost.parse(toString())
            requireNotNull(host) { "Invalid URL $this" }
            val forceSAM = getQueryParameter(PARAM_FORCE_SAM)?.toBooleanStrictOrNull() == true || pathSegments.contains("sam")
            val ilLocation = getQueryParameter(PARAM_FORCE_LOCATION)?.let { IlLocation.fromName(it) }
            val vector = getQueryParameter(PARAM_VECTOR)?.let { Vector.fromLabel(it) } ?: Vector.MOBILE

            return IlUrl(host = host, urn = checkNotNull(urn), vector = vector, ilLocation = ilLocation, forceSAM = forceSAM)
        }
    }
}
