/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.ui.showcases.integrations

import android.net.Uri
import androidx.media3.cast.DefaultMediaItemConverter
import androidx.media3.cast.MediaItemConverter
import androidx.media3.common.C
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.util.Assertions
import com.google.android.gms.cast.MediaInfo
import com.google.android.gms.cast.MediaQueueItem
import com.google.android.gms.common.images.WebImage
import org.json.JSONException
import org.json.JSONObject
import java.util.UUID

/**
 * Pillarbox media item converter from Exoplayer with some modifications.
 *
 * Live audio take a long time to start.
 *
 * @constructor Create empty Pillarbox media item converter
 */

class PillarboxMediaItemConverter(private val defaultConverter: DefaultMediaItemConverter = DefaultMediaItemConverter()) :
    MediaItemConverter by defaultConverter {
    private val KEY_MEDIA_ITEM = "mediaItem"
    private val KEY_PLAYER_CONFIG = "exoPlayerConfig"
    private val KEY_MEDIA_ID = "mediaId"
    private val KEY_URI = "uri"
    private val KEY_TITLE = "title"
    private val KEY_MIME_TYPE = "mimeType"
    private val KEY_DRM_CONFIGURATION = "drmConfiguration"
    private val KEY_UUID = "uuid"
    private val KEY_LICENSE_URI = "licenseUri"
    private val KEY_REQUEST_HEADERS = "requestHeaders"

    override fun toMediaQueueItem(mediaItem: MediaItem): MediaQueueItem {
        Assertions.checkNotNull(mediaItem.localConfiguration)
        // requireNotNull(mediaItem.localConfiguration!!.mimeType) { "The item must specify its mimeType" }
        val metadata = com.google.android.gms.cast.MediaMetadata(com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_GENERIC)
        // if (MimeTypes.isAudio(mediaItem.localConfiguration!!.mimeType)) com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_MUSIC_TRACK
        // else com.google.android.gms.cast.MediaMetadata.MEDIA_TYPE_MOVIE)
        if (mediaItem.mediaMetadata.title != null) {
            metadata.putString(com.google.android.gms.cast.MediaMetadata.KEY_TITLE, mediaItem.mediaMetadata.title.toString())
        }
        if (mediaItem.mediaMetadata.subtitle != null) {
            metadata.putString(com.google.android.gms.cast.MediaMetadata.KEY_SUBTITLE, mediaItem.mediaMetadata.subtitle.toString())
        }
        if (mediaItem.mediaMetadata.artist != null) {
            metadata.putString(com.google.android.gms.cast.MediaMetadata.KEY_ARTIST, mediaItem.mediaMetadata.artist.toString())
        }
        if (mediaItem.mediaMetadata.albumArtist != null) {
            metadata.putString(
                com.google.android.gms.cast.MediaMetadata.KEY_ALBUM_ARTIST, mediaItem.mediaMetadata.albumArtist.toString()
            )
        }
        if (mediaItem.mediaMetadata.albumTitle != null) {
            metadata.putString(
                com.google.android.gms.cast.MediaMetadata.KEY_ALBUM_TITLE, mediaItem.mediaMetadata.albumTitle.toString()
            )
        }
        if (mediaItem.mediaMetadata.artworkUri != null) {
            metadata.addImage(WebImage(mediaItem.mediaMetadata.artworkUri!!))
        }
        if (mediaItem.mediaMetadata.composer != null) {
            metadata.putString(com.google.android.gms.cast.MediaMetadata.KEY_COMPOSER, mediaItem.mediaMetadata.composer.toString())
        }
        if (mediaItem.mediaMetadata.discNumber != null) {
            metadata.putInt(com.google.android.gms.cast.MediaMetadata.KEY_DISC_NUMBER, mediaItem.mediaMetadata.discNumber!!)
        }
        if (mediaItem.mediaMetadata.trackNumber != null) {
            metadata.putInt(com.google.android.gms.cast.MediaMetadata.KEY_TRACK_NUMBER, mediaItem.mediaMetadata.trackNumber!!)
        }
        val contentUrl = mediaItem.localConfiguration!!.uri.toString()
        val contentId = if (mediaItem.mediaId == MediaItem.DEFAULT_MEDIA_ID) contentUrl else mediaItem.mediaId
        val mediaInfo = MediaInfo.Builder(contentId)
            .setStreamType(MediaInfo.STREAM_TYPE_NONE)
            // .setContentType(mediaItem.localConfiguration!!.mimeType!!)
            .setContentUrl(contentUrl)
            .setMetadata(metadata)
            .setCustomData(getCustomData(mediaItem)!!)
            .build()
        return MediaQueueItem.Builder(mediaInfo).build()
    }

    // Deserialization from DefaultMediaItemConverter.
    private fun getMediaItem(
        customData: JSONObject,
        mediaMetadata: MediaMetadata
    ): MediaItem {
        return try {
            val mediaItemJson = customData.getJSONObject(KEY_MEDIA_ITEM)
            val builder = MediaItem.Builder()
                .setUri(Uri.parse(mediaItemJson.getString(KEY_URI)))
                .setMediaId(mediaItemJson.getString(KEY_MEDIA_ID))
                .setMediaMetadata(mediaMetadata)
            if (mediaItemJson.has(KEY_MIME_TYPE)) {
                builder.setMimeType(mediaItemJson.getString(KEY_MIME_TYPE))
            }
            if (mediaItemJson.has(KEY_DRM_CONFIGURATION)) {
                populateDrmConfiguration(mediaItemJson.getJSONObject(KEY_DRM_CONFIGURATION), builder)
            }
            builder.build()
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
    }

    @Throws(JSONException::class)
    private fun populateDrmConfiguration(json: JSONObject, mediaItem: MediaItem.Builder) {
        val drmConfiguration = MediaItem.DrmConfiguration.Builder(UUID.fromString(json.getString(KEY_UUID)))
            .setLicenseUri(json.getString(KEY_LICENSE_URI))
        val requestHeadersJson = json.getJSONObject(KEY_REQUEST_HEADERS)
        val requestHeaders = HashMap<String, String>()
        val iterator = requestHeadersJson.keys()
        while (iterator.hasNext()) {
            val key = iterator.next()
            requestHeaders[key] = requestHeadersJson.getString(key)
        }
        drmConfiguration.setLicenseRequestHeaders(requestHeaders)
        mediaItem.setDrmConfiguration(drmConfiguration.build())
    }

    // Serialization.

    // Serialization.
    private fun getCustomData(mediaItem: MediaItem): JSONObject? {
        val json = JSONObject()
        try {
            json.put(KEY_MEDIA_ITEM, getMediaItemJson(mediaItem))
            val playerConfigJson = getPlayerConfigJson(mediaItem)
            if (playerConfigJson != null) {
                json.put(KEY_PLAYER_CONFIG, playerConfigJson)
            }
        } catch (e: JSONException) {
            throw RuntimeException(e)
        }
        return json
    }

    @Throws(JSONException::class)
    private fun getMediaItemJson(mediaItem: MediaItem): JSONObject? {
        Assertions.checkNotNull(mediaItem.localConfiguration)
        val json = JSONObject()
        json.put(KEY_MEDIA_ID, mediaItem.mediaId)
        json.put(KEY_TITLE, mediaItem.mediaMetadata.title)
        json.put(KEY_URI, mediaItem.localConfiguration!!.uri.toString())
        json.put(KEY_MIME_TYPE, mediaItem.localConfiguration!!.mimeType)
        if (mediaItem.localConfiguration!!.drmConfiguration != null) {
            json.put(
                KEY_DRM_CONFIGURATION,
                getDrmConfigurationJson(mediaItem.localConfiguration!!.drmConfiguration!!)
            )
        }
        return json
    }

    @Throws(JSONException::class)
    private fun getDrmConfigurationJson(drmConfiguration: MediaItem.DrmConfiguration): JSONObject {
        val json = JSONObject()
        json.put(KEY_UUID, drmConfiguration.scheme)
        json.put(KEY_LICENSE_URI, drmConfiguration.licenseUri)
        json.put(KEY_REQUEST_HEADERS, JSONObject(drmConfiguration.licenseRequestHeaders.toMap()))
        return json
    }

    @Throws(JSONException::class)
    private fun getPlayerConfigJson(mediaItem: MediaItem): JSONObject? {
        if (mediaItem.localConfiguration == null ||
            mediaItem.localConfiguration!!.drmConfiguration == null
        ) {
            return null
        }
        val drmConfiguration = mediaItem.localConfiguration!!.drmConfiguration
        val drmScheme: String
        drmScheme = if (C.WIDEVINE_UUID == drmConfiguration!!.scheme) {
            "widevine"
        } else if (C.PLAYREADY_UUID == drmConfiguration.scheme) {
            "playready"
        } else {
            return null
        }
        val playerConfigJson = JSONObject()
        playerConfigJson.put("withCredentials", false)
        playerConfigJson.put("protectionSystem", drmScheme)
        if (drmConfiguration.licenseUri != null) {
            playerConfigJson.put("licenseUrl", drmConfiguration.licenseUri)
        }
        if (!drmConfiguration.licenseRequestHeaders.isEmpty()) {
            playerConfigJson.put("headers", JSONObject(drmConfiguration.licenseRequestHeaders.toMap()))
        }
        return playerConfigJson
    }
}
