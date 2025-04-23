/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data

import androidx.core.net.toUri
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaItem.DrmConfiguration
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlLocation
import java.io.Serializable

/**
 * Generic media item that can represent either a content playable by URL or by URN.
 *
 * @property uri The URI of the media.
 * @property title The title of the media
 * @property description The optional description of the media.
 * @property imageUri The optional image URI of the media.
 * @property languageTag The IETF BCP47 language tag of the title and description.
 */
sealed class DemoItem(
    open val uri: String,
    open val title: String?,
    open val description: String?,
    open val imageUri: String?,
    open val languageTag: String? = null,
) : Serializable {
    /**
     * Represents a media item playable by URL.
     *
     * @property uri The URI of the media.
     * @property title The title of the media
     * @property description The optional description of the media.
     * @property imageUri The optional image URI of the media.
     * @property languageTag The IETF BCP47 language tag of the title and description.
     * @property licenseUri The DRM license uri for the media.
     * @property multiSession Whether to use multi-session or not.
     * @property licenseRequestHeaders optional headers to be sent with the license request.
     */
    data class URL(
        override val uri: String,
        override val title: String? = null,
        override val description: String? = null,
        override val imageUri: String? = null,
        override val languageTag: String? = null,
        val licenseUri: String? = null,
        val multiSession: Boolean = true,
        val licenseRequestHeaders: Map<String, String> = emptyMap(),
    ) : DemoItem(uri, title, description, imageUri, languageTag) {

        override fun toMediaItem(): MediaItem {
            return MediaItem.Builder()
                .setUri(uri)
                .setMediaId(uri)
                .setMediaMetadata(
                    MediaMetadata.Builder()
                        .setTitle(title)
                        .setDescription(description)
                        .setArtworkUri(imageUri?.toUri())
                        .build()
                )
                .setDrmConfiguration(
                    licenseUri?.let {
                        DrmConfiguration.Builder(C.WIDEVINE_UUID).apply {
                            setLicenseUri(it)
                            setMultiSession(multiSession)
                            setLicenseRequestHeaders(licenseRequestHeaders)
                        }.build()
                    }
                )
                .build()
        }
    }

    /**
     * Represents a media item playable by URN.
     *
     * @property urn The URN of the media.
     * @property title The title of the media
     * @property description The optional description of the media.
     * @property imageUri The optional image URI of the media.
     * @property languageTag The IETF BCP47 language tag of the title and description.
     * @property host The host from which to load the media.
     * @property forceSAM Whether to use SAM instead of the IL.
     * @property ilLocation The optional location from which to load the media.
     */
    data class URN(
        val urn: String,
        override val title: String? = null,
        override val description: String? = null,
        override val imageUri: String? = null,
        override val languageTag: String? = null,
        val host: IlHost = IlHost.PROD,
        val forceSAM: Boolean = false,
        val ilLocation: IlLocation? = null,
    ) : DemoItem(urn, title, description, imageUri, languageTag) {
        override fun toMediaItem(): MediaItem {
            return SRGMediaItem(urn) {
                host(host)
                forceSAM(forceSAM)
                ilLocation(ilLocation)
                mediaMetadata {
                    setTitle(title)
                    setDescription(description)
                    setArtworkUri(imageUri?.toUri())
                }
            }
        }
    }

    /**
     * Converts this [DemoItem] into a [MediaItem].
     */
    abstract fun toMediaItem(): MediaItem

    companion object {
        @Suppress("ConstPropertyName")
        private const val serialVersionUID: Long = 1
    }
}
