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
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlLocation
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector
import ch.srgssr.pillarbox.core.business.source.MimeTypeSrg
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource
import java.net.URL

/**
 * Creates a [MediaItem] suited for SRG SSR content identified by a URN.
 *
 * **Basic usage**
 *
 * ```kotlin
 * val mediaItem: MediaItem = SRGMediaItem("urn:rts:audio:3262363")
 * ```
 *
 * **Custom configuration**
 *
 * ```kotlin
 * val mediaItem: MediaItem = SRGMediaItem("urn:rts:audio:3262363") {
 *     host(IlHost.Default)
 *     vector(Vector.TV)
 * }
 * ```
 *
 * **Modify an existing `MediaItem`**
 *
 * ```kotlin
 * val updatedMediaItem: MediaItem = mediaItem.buildUpon {
 *     urn("urn:rts:video:1234")
 * }
 * ```
 *
 * @param urn The URN identifying the SRG SSR content.
 * @param block An optional lambda to further configure the [MediaItem].
 *
 * @return A [MediaItem] configured for the specified SRG SSR content.
 */
@PillarboxDsl
@Suppress("FunctionName")
fun SRGMediaItem(urn: String, block: SRGMediaItemBuilder.() -> Unit = {}): MediaItem {
    return SRGMediaItemBuilder(MediaItem.Builder().setMediaId(urn).build()).apply(block).build()
}

/**
 * Creates a new [MediaItem] by copying properties from the existing [MediaItem] and applying modifications defined in the provided block.
 * This function leverages [SRGMediaItemBuilder] for constructing the new [MediaItem].
 *
 * **Usage example**
 * ```kotlin
 * val mediaItem: MediaItem = sourceItem.buildUpon {
 *     host(IlHost.Stage)
 * }
 * ```
 *
 * @param block A lambda with a receiver of type [SRGMediaItemBuilder] that allows configuring the new [MediaItem].
 * @return A new [MediaItem] instance with the applied modifications.
 */
fun MediaItem.buildUpon(block: SRGMediaItemBuilder.() -> Unit): MediaItem {
    return SRGMediaItemBuilder(this).apply(block).build()
}

/**
 * Creates a [MediaItem] suited for SRG SSR content identified by a URN. The created [MediaItem] can be parsed by [PillarboxMediaSource].
 */
@PillarboxDsl
class SRGMediaItemBuilder internal constructor(mediaItem: MediaItem) {
    private val mediaItemBuilder = mediaItem.buildUpon()
    private var urn: String = mediaItem.mediaId
    private var host: URL = IlHost.DEFAULT
    private var forceSAM: Boolean = false
    private var ilLocation: IlLocation? = null
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
                this.ilLocation = uri.getQueryParameter(PARAM_FORCE_LOCATION)?.let { IlLocation.fromName(it) }
                uri.getQueryParameter(PARAM_VECTOR)?.let { vector = it }
            }
        }
    }

    /**
     * Sets the media metadata using an existing [MediaMetadata] instance.
     *
     * @param mediaMetadata The [MediaMetadata] to set on the [MediaItem].
     */
    fun mediaMetadata(mediaMetadata: MediaMetadata) {
        this.mediaItemBuilder.setMediaMetadata(mediaMetadata)
    }

    /**
     * Sets the media metadata by customizing the [MediaMetadata.Builder] receiver in [block].
     *
     * @param block A lambda that receives a [MediaMetadata.Builder] to configure the [MediaMetadata].
     */
    fun mediaMetadata(block: MediaMetadata.Builder.() -> Unit) {
        mediaMetadata(MediaMetadata.Builder().apply(block).build())
    }

    /**
     * Sets the URN to be played.
     *
     * @param urn The URN to be played. It must be a valid URN string.
     */
    fun urn(urn: String) {
        this.urn = urn
    }

    /**
     * Sets the host URL to the integration layer.
     *
     * @param host The URL of the integration layer server.
     */
    fun host(host: URL) {
        this.host = host
    }

    /**
     * Forces the use of the SAM backend.
     *
     * @param forceSAM `true` to force the use of the SAM backend, `false` otherwise.
     */
    fun forceSAM(forceSAM: Boolean) {
        this.forceSAM = forceSAM
    }

    /**
     * Sets the location for IL backend calls.
     *
     * @param ilLocation The location to set. Passing `null` defaults to automatic detection.
     */
    fun ilLocation(ilLocation: IlLocation?) {
        this.ilLocation = ilLocation
    }

    /**
     * Sets the vector.
     *
     * @param vector The vector to forward to the integration layer. Should be either [Vector.MOBILE] or [Vector.TV].
     */
    fun vector(vector: String) {
        this.vector = vector
    }

    /**
     * Builds a [MediaItem] based on the provided parameters.
     *
     * It ensures the URN is valid and sets the necessary properties on the [MediaItem].
     *
     * @throws IllegalArgumentException If the URN is not valid.
     * @return A new [MediaItem] ready for playback.
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
            ilLocation?.let {
                appendQueryParameter(PARAM_FORCE_LOCATION, it.toString())
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
