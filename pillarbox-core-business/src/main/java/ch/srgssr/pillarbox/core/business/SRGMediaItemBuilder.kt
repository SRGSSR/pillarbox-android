/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector
import ch.srgssr.pillarbox.core.business.source.MimeTypeSrg
import java.net.URL

/**
 * Create a [MediaItem] that can be parsed by [PillarboxMediaSource][ch.srgssr.pillarbox.player.source.PillarboxMediaSource].
 *
 * @param mediaItem Build a new [SRGMediaItemBuilder] from an existing [MediaItem].
 */
class SRGMediaItemBuilder(mediaItem: MediaItem) {
    private val mediaItemBuilder = mediaItem.buildUpon()
    private var urn: String = mediaItem.mediaId
    private var host: URL = IlHost.DEFAULT
    private var forceSAM: Boolean = false
    private var forceLocation: String? = null
    private var vector: String = Vector.MOBILE

    init {
        urn = mediaItem.mediaId
        mediaItem.localConfiguration?.let { localConfiguration ->
            val uri = localConfiguration.uri
            val urn = uri.lastPathSegment
            if (uri.toString().contains(PATH) && urn.isValidMediaUrn()) {
                uri.host?.let { hostname -> host = URL(Uri.Builder().scheme(host.protocol).authority(hostname).build().toString()) }
                this.urn = urn!!
                this.forceSAM = uri.getQueryParameter(PARAM_FORCE_SAM)?.toBooleanStrictOrNull() ?: false
                this.forceLocation = uri.getQueryParameter(PARAM_FORCE_LOCATION)
                uri.getQueryParameter(PARAM_VECTOR)?.let { vector = it }
            }
        }
    }

    /**
     * @param urn The SRG SSR unique identifier of a media.
     */
    constructor(urn: String) : this(MediaItem.Builder().setMediaId(urn).build())

    /**
     * Set media metadata
     *
     * @param mediaMetadata The [MediaMetadata] to set to [MediaItem].
     * @return this for convenience
     */
    fun setMediaMetadata(mediaMetadata: MediaMetadata): SRGMediaItemBuilder {
        this.mediaItemBuilder.setMediaMetadata(mediaMetadata)
        return this
    }

    /**
     * Set urn
     *
     * @param urn The urn that has to be a validated urn.
     * @return this for convenience
     */
    fun setUrn(urn: String): SRGMediaItemBuilder {
        this.urn = urn
        return this
    }

    /**
     * Set integration host
     *
     * @param host The host name to the integration layer server.
     * @return this for convenience
     */
    fun setHost(host: URL): SRGMediaItemBuilder {
        this.host = host
        return this
    }

    /**
     * Set force SAM
     *
     * @param forceSAM `true` to force the use of the SAM backend, `false` otherwise.
     * @return this for convenience
     */
    fun setForceSAM(forceSAM: Boolean): SRGMediaItemBuilder {
        this.forceSAM = forceSAM
        return this
    }

    /**
     * Set force location
     *
     * @param forceLocation The location to use on the IL/SAM backend calls. Can be `null`, `CH`,  or `WW`.
     * @return this for convenience
     */
    fun setForceLocation(forceLocation: String?): SRGMediaItemBuilder {
        this.forceLocation = forceLocation
        return this
    }

    /**
     * Set vector
     *
     * @param vector The vector to forward to the integration layer.
     * Should be [Vector.TV] or [Vector.MOBILE].
     * @return this for convenience
     */
    fun setVector(vector: String): SRGMediaItemBuilder {
        this.vector = vector
        return this
    }

    /**
     * Build
     *
     * @return create a new [MediaItem].
     */
    fun build(): MediaItem {
        require(urn.isValidMediaUrn()) { "Not a valid Urn!" }
        mediaItemBuilder.setMediaId(urn)
        mediaItemBuilder.setMimeType(MimeTypeSrg)
        val uri = Uri.Builder().apply {
            scheme(host.protocol)
            authority(host.host)
            if (forceSAM) {
                appendEncodedPath("sam")
            }
            appendEncodedPath(PATH)
            appendEncodedPath(urn)
            if (forceSAM) {
                appendQueryParameter(PARAM_FORCE_SAM, true.toString())
            }
            if (!forceLocation.isNullOrBlank()) {
                appendQueryParameter(PARAM_FORCE_LOCATION, forceLocation)
            }
            if (vector.isNotBlank()) {
                appendQueryParameter(PARAM_VECTOR, vector)
            }
            appendQueryParameter(PARAM_ONLY_CHAPTERS, true.toString())
        }.build()
        mediaItemBuilder.setUri(uri)
        return mediaItemBuilder.build()
    }

    private companion object {
        private const val PATH = "integrationlayer/2.1/mediaComposition/byUrn/"
        private const val PARAM_ONLY_CHAPTERS = "onlyChapters"
        private const val PARAM_FORCE_SAM = "forceSAM"
        private const val PARAM_FORCE_LOCATION = "forceLocation"
        private const val PARAM_VECTOR = "vector"
    }
}
