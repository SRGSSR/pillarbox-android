/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.shared.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesApple
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesBitmovin
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesGoogle
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesSRG
import ch.srgssr.pillarbox.demo.shared.data.samples.SamplesUnifiedStreaming

/**
 * Demo browser
 *
 * - root
 *  - Playlist 1
 *      - Playable items
 *  - Playlist 2
 *      - Playable items
 *
 * @constructor Create empty Demo browser
 */
class DemoBrowser {

    /**
     * Every Android Auto navigable [MediaItem] accessed by id.
     */
    private val mapMediaIdMediaItem = mutableMapOf<String, MediaItem>()
    private val mapMediaIdToChildren = mutableMapOf<String, MutableList<MediaItem>>()

    /**
     * Root browsable item
     */
    val rootMediaItem: MediaItem by lazy {
        MediaItem.Builder()
            .setMediaId(DEMO_BROWSABLE_ROOT)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_MIXED)
                    .setIsBrowsable(true)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }

    init {
        val rootList = mapMediaIdToChildren[DEMO_BROWSABLE_ROOT] ?: mutableListOf()
        mapMediaIdMediaItem[DEMO_BROWSABLE_ROOT] = rootMediaItem
        val listPlaylist = listOf(
            SamplesSRG.StreamUrls,
            SamplesSRG.StreamUrns,
            Playlist(
                title = "Mixed content",
                items = listOf(
                    SamplesSRG.OnDemandHLS,
                    SamplesSRG.OnDemandHorizontalVideo,
                    SamplesSRG.Unknown,
                    SamplesSRG.ShortOnDemandVideoHLS
                )
            ),
            SamplesApple.All,
            SamplesGoogle.All,
            SamplesUnifiedStreaming.HLS,
            SamplesUnifiedStreaming.DASH,
            SamplesBitmovin.All
        )
        for (playlist in listPlaylist) {
            val playlistRootItem = playlist.toMediaItem()
            rootList += playlistRootItem
            mapMediaIdMediaItem[playlistRootItem.mediaId] = playlistRootItem
            for (playlistItem in playlist.items) {
                val item = playlistItem.toMediaItem()
                mapMediaIdMediaItem[item.mediaId] = item
            }

            mapMediaIdToChildren[playlist.title] = playlist.items.map {
                val mediaItem = it.toMediaItem()
                val metadata = mediaItem.mediaMetadata.buildUpon()
                    .setIsPlayable(true)
                    .setIsBrowsable(false)
                    .build()
                it.toMediaItem().buildUpon()
                    .setMediaMetadata(metadata)
                    .build()
            }.toMutableList()
        }

        mapMediaIdToChildren[DEMO_BROWSABLE_ROOT] = rootList
    }

    /**
     * Get children from [parentId]
     *
     * @param parentId
     * @return
     */
    fun getChildren(parentId: String): MutableList<MediaItem>? = mapMediaIdToChildren[parentId]

    /**
     * Get media item from [mediaId]
     *
     * @param mediaId
     */
    fun getMediaItemFromId(mediaId: String) = mapMediaIdMediaItem[mediaId]

    private companion object {
        private const val DEMO_BROWSABLE_ROOT = "DEMO_BROWSABLE_ROOT"
    }
}
