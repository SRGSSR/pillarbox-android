/*
 * Copyright (c) 2022. SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.demo.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata

/**
 * Demo browser
 *
 * - root
 *  - Playlist 1
 *      - Playable items
 *  - Playlist 2
 *      - Playable items
 *
 * @property dataProvider
 * @constructor Create empty Demo browser
 */
class DemoBrowser(private val dataProvider: DemoPlaylistProvider) {

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
                    .setFolderType(MediaMetadata.FOLDER_TYPE_MIXED)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }

    init {
        val rootList = mapMediaIdToChildren[DEMO_BROWSABLE_ROOT] ?: mutableListOf()
        val listPlaylist = dataProvider.loadDemoItemFromAssets("streams.json")
        for (playlist in listPlaylist) {
            rootList += playlist.toMediaItem()
            for (playlistItem in playlist.items) {
                val item = playlistItem.toMediaItem()
                mapMediaIdMediaItem[item.mediaId] = item
            }

            mapMediaIdToChildren[playlist.title] = playlist.items.map {
                val mediaItem = it.toMediaItem()
                val metadata = mediaItem.mediaMetadata.buildUpon()
                    .setIsPlayable(true)
                    .setFolderType(MediaMetadata.FOLDER_TYPE_NONE)
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

    companion object {
        private const val DEMO_BROWSABLE_ROOT = "DEMO_BROWSABLE_ROOT"
    }
}
