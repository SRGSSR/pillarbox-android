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
import androidx.media3.common.MimeTypes
import ch.srgssr.pillarbox.core.business.SRGMediaItem
import ch.srgssr.pillarbox.core.business.integrationlayer.data.isValidMediaUrn
import ch.srgssr.pillarbox.core.business.integrationlayer.service.IlHost
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaMetadata
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage
import org.json.JSONObject

/**
 * [MediaItemConverter] implementation to handle SRG SSR receivers.
 */
class SRGMediaItemConverter : MediaItemConverter {

    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        val contentId = mediaItem.mediaId
        val localConfiguration = mediaItem.localConfiguration
        checkNotNull(localConfiguration)
        return if (contentId.isValidMediaUrn()) {
            val mediaInfo = MediaInfo.Builder(contentId)
                .setContentType(localConfiguration.mimeType)
                .setContentUrl(localConfiguration.uri.toString())
                .setCustomData(toCustomData(localConfiguration.uri))
                .setMetadata(
                    MediaMetadata(MediaMetadata.MEDIA_TYPE_GENERIC).apply {
                        fillMediaMetadata(mediaItem)
                    }
                )
                .build()
            MediaQueueItem.Builder(mediaInfo).build()
        } else {
            val mediaType = localConfiguration.mimeType?.let {
                if (MimeTypes.isAudio(it)) {
                    MediaMetadata.MEDIA_TYPE_MUSIC_TRACK
                } else {
                    MediaMetadata.MEDIA_TYPE_MOVIE
                }
            } ?: MediaMetadata.MEDIA_TYPE_GENERIC
            val mediaInfo = MediaInfo.Builder(contentId)
                .setStreamType(MediaInfo.STREAM_TYPE_BUFFERED)
                .setContentType(localConfiguration.mimeType)
                .setContentUrl(localConfiguration.uri.toString())
                .setCustomData(localConfiguration.drmConfiguration?.let(::toCustomData))
                .setMetadata(
                    MediaMetadata(mediaType).apply {
                        fillMediaMetadata(mediaItem)
                    }
                )
                .build()
            MediaQueueItem.Builder(mediaInfo).build()
        }
    }

    override fun toMediaItem(mediaQueueItem: MediaQueueItem): MediaItem {
        val mediaInfo = mediaQueueItem.media
        checkNotNull(mediaInfo)
        val mediaMetadata = androidx.media3.common.MediaMetadata.Builder().apply {
            mediaInfo.metadata?.let { metadata ->
                setTitle(metadata.getString(MediaMetadata.KEY_TITLE))
                setSubtitle(metadata.getString(MediaMetadata.KEY_SUBTITLE))
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
                .setMediaId(mediaInfo.contentId)
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
         * cast.framework.ContentProtection.WIDEVINE
         */
        const val WIDEVINE_VALUE = "widevine"

        /**
         * cast.framework.ContentProtection.PLAYREADY
         */
        const val PLAYREADY_VALUE = "playready"

        fun MediaMetadata.fillMediaMetadata(mediaItem: MediaItem) {
            mediaItem.mediaMetadata.title?.let { putString(MediaMetadata.KEY_TITLE, it.toString()) }
            mediaItem.mediaMetadata.subtitle?.let { putString(MediaMetadata.KEY_SUBTITLE, it.toString()) }
            mediaItem.mediaMetadata.artworkUri?.let { addImage(WebImage(it)) }
        }

        fun toCustomData(uri: Uri): JSONObject {
            return JSONObject().apply {
                IlHost.parse(uri.toString())?.let {
                    put(KEY_SERVER, it.baseHostUrl.toUri().host)
                }
            }
        }

        fun toCustomData(drmConfiguration: MediaItem.DrmConfiguration): JSONObject {
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
