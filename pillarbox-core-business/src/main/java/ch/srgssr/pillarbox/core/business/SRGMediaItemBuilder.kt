/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import android.net.Uri
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector
import ch.srgssr.pillarbox.core.business.source.MimeTypeSrg

/**
 * Create a [MediaItem] that can be parsed by SRGMediaSource
 *
 * @param mediaItem Build a new [SRGMediaItemBuilder] from an existing [MediaItem].
 */
class SRGMediaItemBuilder(mediaItem: MediaItem) {
    private val mediaItemBuilder = mediaItem.buildUpon()
    private var urn: String = mediaItem.mediaId
    private var host: String = "il.srgssr.ch"
    private var vector: String = Vector.MOBILE

    init {
        urn = mediaItem.mediaId
        mediaItem.localConfiguration?.uri?.let { uri ->
            val urn = uri.lastPathSegment
            if (uri.toString().contains(PATH) && urn.isValidMediaUrn()) {
                uri.host?.let { host = it }
                this.urn = urn!!
                uri.getQueryParameter("vector")?.let { vector = it }
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
     * @param mediaMetadata The [MediaMetadata] to use set to [MediaItem].
     * @return this for convenience
     */
    fun setMediaMetadata(mediaMetadata: MediaMetadata): SRGMediaItemBuilder {
        this.mediaItemBuilder.setMediaMetadata(mediaMetadata)
        return this
    }

    /**
     * Set urn
     *
     * @param urn The urn that have to be a validated urn.
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
    fun setHost(host: String): SRGMediaItemBuilder {
        this.host = host
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
            scheme("https")
            appendEncodedPath(host)
            appendEncodedPath(PATH)
            appendEncodedPath(urn)
            if (vector.isNotBlank()) {
                appendQueryParameter(PARAM_VECTOR, vector)
            }
            appendQueryParameter(PARAM_ONLY_CHAPTERS, true.toString())
        }.build()
        mediaItemBuilder.setUri(uri)
        return mediaItemBuilder.build()
    }

    companion object {
        private const val PATH = "integrationlayer/2.1/mediaComposition/byUrn/"
        private const val PARAM_ONLY_CHAPTERS = "onlyChapters"
        private const val PARAM_VECTOR = "vector"
    }
}

/**
 * Create [MediaItem] for Pillarbox from a urn.
 */
@Deprecated("Replaced by SRGMediaItemBuilder")
object MediaItemUrn {
    /**
     * Invoke
     *
     * @param urn The media urn to play.
     * @param title The optional title to display..
     * @param subtitle The optional subtitle to display.
     * @param artworkUri The artworkUri image uri.
     * @return MediaItem.
     */
    operator fun invoke(
        urn: String,
        title: String? = null,
        subtitle: String? = null,
        artworkUri: Uri? = null
    ): MediaItem = SRGMediaItemBuilder(urn)
        .setMediaMetadata(
            MediaMetadata.Builder().apply {
                setTitle(title)
                setSubtitle(subtitle)
                setArtworkUri(artworkUri)
            }.build()
        )
        .build()
}
