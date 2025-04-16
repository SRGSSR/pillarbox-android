/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
@file:Suppress("MaximumLineLength", "MaxLineLength", "StringLiteralDuplication")

package ch.srgssr.pillarbox.demo.shared.data

import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import java.io.Serializable

/**
 * Playlist
 *
 * @property title
 * @property items
 * @property languageTag
 */
@Suppress("UndocumentedPublicProperty")
data class Playlist(val title: String, val items: List<DemoItem>, val languageTag: String? = null) : Serializable {
    /**
     * To media item
     *
     * @return not playable MediaItem
     */
    fun toMediaItem(): MediaItem {
        return MediaItem.Builder()
            .setMediaId(title)
            .setMediaMetadata(
                MediaMetadata.Builder()
                    .setTitle(title)
                    .setIsBrowsable(true)
                    .setMediaType(MediaMetadata.MEDIA_TYPE_FOLDER_PLAYLISTS)
                    .setIsPlayable(false)
                    .build()
            )
            .build()
    }

    companion object {
        private const val serialVersionUID: Long = 1

        val DashIfStream = Playlist(
            title = "DashIf streams",
            items = listOf(
                DemoItem.DashIfMultiPeriodVodExample,
                DemoItem.DashIfMultiPeriodDifferentContentVodExample,
                DemoItem.DashIfClearMultiPeriodStatic,
                DemoItem.DashIfClearMultiPeriodLive,
                DemoItem.DashIfMultiDrmMultiPeriod,
            )
        )
    }
}
