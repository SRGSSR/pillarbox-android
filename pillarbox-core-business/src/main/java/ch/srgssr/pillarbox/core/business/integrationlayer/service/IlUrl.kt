/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.integrationlayer.service

import android.net.Uri
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn

/**
 * @property host the [IlHost] to use.
 * @property urn the URN of the media to request.
 * @property vector The [Vector] to use.
 * @property forceSAM Force SAM usage.
 * @property ilLocation the [IlLocation] of the request.
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
     * Uri
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

    @Suppress("UndocumentedPublicClass")
    companion object {
        private const val PARAM_ONLY_CHAPTERS = "onlyChapters"
        private const val PARAM_FORCE_SAM = "forceSAM"
        private const val PARAM_FORCE_LOCATION = "forceLocation"
        private const val PARAM_VECTOR = "vector"
        private const val PATH = "integrationlayer/2.1/mediaComposition/byUrn/"

        /**
         * Convert an [Uri] into a valid [IlUrl].
         *
         * @return a [IlUrl] or throw an [IllegalArgumentException] if the Uri can't be parse.
         */
        fun Uri.toIlUrl(): IlUrl {
            val urn = lastPathSegment
            val host = IlHost.parse(toString())
            require(urn.isValidMediaUrn()) { "Invalid urn $urn found in $this" }
            requireNotNull(host) { "Invalid url $this" }
            val forceSAM = getQueryParameter(PARAM_FORCE_SAM)?.toBooleanStrictOrNull() == true || pathSegments.contains("sam")
            val ilLocation = getQueryParameter(PARAM_FORCE_LOCATION)?.let { IlLocation.fromName(it) }
            val vector = getQueryParameter(PARAM_VECTOR)?.let { Vector.fromLabel(it) } ?: Vector.MOBILE

            return IlUrl(host = host, urn = checkNotNull(urn), vector, ilLocation = ilLocation, forceSAM = forceSAM)
        }
    }
}
