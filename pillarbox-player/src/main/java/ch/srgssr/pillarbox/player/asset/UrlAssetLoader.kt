/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.asset

import androidx.media3.common.MediaItem
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import ch.srgssr.pillarbox.player.asset.UrlAssetLoader.TrackerDataProvider
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData

/**
 * AssetLoader to load Asset from an stream url.
 *
 * @param defaultMediaSourceFactory The [DefaultMediaSourceFactory] to create a MediaSource for the player.
 */
class UrlAssetLoader(
    defaultMediaSourceFactory: DefaultMediaSourceFactory,
) : AssetLoader(defaultMediaSourceFactory) {
    /**
     * The [TrackerDataProvider] to customize tracker data.
     */
    var trackerDataProvider: TrackerDataProvider = DEFAULT_TRACKER_DATA_LOADER

    /**
     * Tracker data loader
     *
     * @constructor Create empty Tracker data loader
     */
    fun interface TrackerDataProvider {
        /**
         * Provide Tracker Data to the [MediaItem].
         *
         * @param mediaItem The input [MediaItem] of the [UrlAssetLoader.loadAsset].
         * @param trackerDataBuilder The [MediaItemTrackerData.Builder] to add tracker data.
         */
        suspend fun provide(mediaItem: MediaItem, trackerDataBuilder: MediaItemTrackerData.Builder)
    }

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfiguration != null
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val mediaSource = mediaSourceFactory.createMediaSource(mediaItem)
        val trackerData = MediaItemTrackerData.Builder().apply {
            trackerDataProvider.provide(mediaItem, this)
        }.build()
        return Asset(
            mediaSource = mediaSource,
            mediaMetadata = mediaItem.mediaMetadata,
            trackersData = trackerData,
        )
    }

    companion object {
        private val DEFAULT_TRACKER_DATA_LOADER = TrackerDataProvider { _, _ -> }
    }
}
