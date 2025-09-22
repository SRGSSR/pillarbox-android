/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.core.business.cast

import android.net.Uri
import androidx.core.net.toUri
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage
import org.json.JSONObject
import com.google.android.gms.cast.MediaMetadata as CastMediaMetadata

/**
 * [MediaItemConverter] implementation to handle [SRG SSR receivers](https://github.com/SRGSSR/srgletterbox-googlecast).
 *
 * SRG SSR receivers have to handle media with urn and url with DRM configuration.
 */
class SRGMediaItemConverter : MediaItemConverter {

    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val contentId = mediaItem.mediaId
        val localConfiguration = mediaItem.localConfiguration
        checkNotNull(localConfiguration)
        return if (contentId != MediaItem.DEFAULT_MEDIA_ID && contentId.isValidMediaUrn()) {
            val mediaInfo = MediaInfo.Builder(contentId)
                .setContentType(localConfiguration.mimeType)
                .setCustomData(createCustomDataFromIlHostUri(localConfiguration.uri))
                .setMetadata(createCastMediaMetadata(CastMediaMetadata.MEDIA_TYPE_GENERIC, mediaItem.mediaMetadata))
                .build()
            MediaQueueItem.Builder(mediaInfo).build()
        } else {
            check(mediaItem.mediaId == MediaItem.DEFAULT_MEDIA_ID) {
                "mediaId must not set when playing url"
            }
            val mediaType = localConfiguration.mimeType?.let {
                if (MimeTypes.isAudio(it)) {
                    CastMediaMetadata.MEDIA_TYPE_MUSIC_TRACK
                } else {
                    CastMediaMetadata.MEDIA_TYPE_MOVIE
                }
            } ?: CastMediaMetadata.MEDIA_TYPE_GENERIC
            val contentUrl = localConfiguration.uri.toString()
            val mediaInfo = MediaInfo.Builder()
                .setContentType(localConfiguration.mimeType)
                .setContentUrl(contentUrl)
                .setCustomData(localConfiguration.drmConfiguration?.let(::createCustomData))
                .setMetadata(
                    createCastMediaMetadata(mediaType, mediaItem.mediaMetadata)
                )
                .build()
            MediaQueueItem.Builder(mediaInfo).build()
        }
    }

    override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
        val mediaInfo = mediaQueueItem.media
        checkNotNull(mediaInfo)
        val mediaMetadata = MediaMetadata.Builder().apply {
            mediaInfo.metadata?.let { metadata ->
                setTitle(metadata.getString(CastMediaMetadata.KEY_TITLE))
                setSubtitle(metadata.getString(CastMediaMetadata.KEY_SUBTITLE))
                if (metadata.hasImages()) {
                    setArtworkUri(metadata.images.first().url)
                }
            }
        }.build()
        return if (mediaInfo.contentId.isValidMediaUrn()) {
            val ilHost = mediaInfo.customData?.let(::getIlHost)
            SRGMediaItem(urn = mediaInfo.contentId) {
                mediaMetadata(mediaMetadata)
                ilHost?.let { host(it) }
            }
        } else {
            MediaItem.Builder()
                .setUri(mediaInfo.contentUrl)
                .setDrmConfiguration(getDrmConfiguration(mediaInfo))
                .setMediaMetadata(mediaMetadata)
                .setMimeType(mediaInfo.contentType)
                .build()
        }
    }

    private companion object {

        const val KEY_LICENSE_URL = "licenseUrl"
        const val KEY_PROTECTION_SYSTEM = "protectionSystem"
        const val KEY_SERVER = "server"

        /**
         * Constant from google cast web receiver cast.framework.ContentProtection.WIDEVINE
         */
        const val WIDEVINE_VALUE = "widevine"

        /**
         * Constant from google cast web receiver cast.framework.ContentProtection.PLAYREADY
         */
        const val PLAYREADY_VALUE = "playready"

        fun createCastMediaMetadata(mediaType: Int, mediaMetadata: MediaMetadata): CastMediaMetadata {
            return CastMediaMetadata(mediaType).apply {
                mediaMetadata.title?.let { putString(CastMediaMetadata.KEY_TITLE, it.toString()) }
                mediaMetadata.subtitle?.let { putString(CastMediaMetadata.KEY_SUBTITLE, it.toString()) }
                mediaMetadata.artworkUri?.let { addImage(WebImage(it)) }
            }
        }

        fun createCustomDataFromIlHostUri(uri: Uri): JSONObject {
            return JSONObject().apply {
                IlHost.parse(uri.toString())?.let {
                    put(KEY_SERVER, it.baseHostUrl.toUri().host)
                }
            }
        }

        fun createCustomData(drmConfiguration: MediaItem.DrmConfiguration): JSONObject {
            return JSONObject().apply {
                put(KEY_LICENSE_URL, drmConfiguration.licenseUri)
                val protectionSystem = when (drmConfiguration.scheme) {
                    C.WIDEVINE_UUID -> WIDEVINE_VALUE
                    C.PLAYREADY_UUID -> PLAYREADY_VALUE
                    else -> null
                }
                put(KEY_PROTECTION_SYSTEM, protectionSystem)
            }
        }

        fun getIlHost(customData: JSONObject): IlHost? {
            val host = customData.opt(KEY_SERVER)
            return host?.let { IlHost.parse("https://$it") }
        }

        fun getDrmConfiguration(mediaInfo: MediaInfo): MediaItem.DrmConfiguration? {
            return mediaInfo.customData?.let {
                val licenseUrl = it.getString(KEY_LICENSE_URL)
                val protectionSystem = it.getString(KEY_PROTECTION_SYSTEM)
                val drmUUID = when (protectionSystem) {
                    WIDEVINE_VALUE -> C.WIDEVINE_UUID
                    PLAYREADY_VALUE -> C.PLAYREADY_UUID
                    else -> null
                }
                drmUUID?.let {
                    MediaItem.DrmConfiguration.Builder(it)
                        .setLicenseUri(licenseUrl)
                        .build()
                }
            }
        }
    }
}
