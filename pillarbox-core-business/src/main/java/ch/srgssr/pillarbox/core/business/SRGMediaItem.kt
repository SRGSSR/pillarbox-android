/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlLocation
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlUrl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlUrl.Companion.toIlUrl
import ch.srgssr.pillarbox.core.business.integrationlayer.service.Vector
import ch.srgssr.pillarbox.core.business.source.MimeTypeSrg
import ch.srgssr.pillarbox.player.PillarboxDsl
import ch.srgssr.pillarbox.player.source.PillarboxMediaSource

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
 *     host(IlHost.PROD)
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
    require(urn.isValidMediaUrn()) { "URN is not valid." }
    return SRGMediaItemBuilder(MediaItem.Builder().setMediaId(urn).build()).apply(block).build()
}

/**
 * Creates a new [MediaItem] by copying properties from the existing [MediaItem] and applying modifications defined in the provided block.
 * This function leverages [SRGMediaItemBuilder] for constructing the new [MediaItem].
 *
 * **Usage example**
 * ```kotlin
 * val mediaItem: MediaItem = sourceItem.buildUpon {
 *     host(IlHost.STAGE)
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
    private var host: IlHost = IlHost.PROD
    private var forceSAM: Boolean = false
    private var ilLocation: IlLocation? = null
    private var vector: Vector = Vector.MOBILE

    init {
        mediaItem.localConfiguration?.let { localConfiguration ->
            val ilUrl = localConfiguration.uri.toIlUrl()
            host = ilUrl.host
            urn = ilUrl.urn
            forceSAM = ilUrl.forceSAM
            ilLocation = ilUrl.ilLocation
            vector = ilUrl.vector
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
     * Sets the host base URL to the integration layer.
     *
     * @param host The base URL of the integration layer server.
     */
    fun host(host: IlHost) {
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
     * @param vector The vector to forward to the integration layer.
     */
    fun vector(vector: Vector) {
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
        val ilUrl = IlUrl(host = host, urn = urn, vector = vector, forceSAM = forceSAM, ilLocation = ilLocation)
        return mediaItemBuilder.setUri(ilUrl.uri)
            .setMediaId(ilUrl.urn)
            .setMimeType(MimeTypeSrg)
            .build()
    }
}
