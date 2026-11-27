/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.offline

import android.content.Context
import androidx.core.net.toUri
import androidx.media3.common.MediaItem
import androidx.media3.datasource.DefaultDataSource
import androidx.media3.datasource.cache.Cache
import androidx.media3.exoplayer.offline.DownloadRequest
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.tracker.FactoryData
import ch.srgssr.pillarbox.player.tracker.MediaItemTracker
import ch.srgssr.pillarbox.player.tracker.MediaItemTrackerData
import ch.srgssr.pillarbox.player.tracker.MutableMediaItemTrackerData
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.runner.RunWith
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue

@RunWith(AndroidJUnit4::class)
class DownloadedMediaAssetLoaderTest {
    private lateinit var context: Context

    @BeforeTest
    fun setUp() {
        context = ApplicationProvider.getApplicationContext()
    }

    @Test
    fun `trackers data are decoded by the matching asset loader`() = runTest {
        val assetLoader = TestAssetLoader(context)
        val downloadedMediaAssetLoader = createDownloadedMediaAssetLoader(trackersData = TRACKERS_DATA) { assetLoader }

        val asset = downloadedMediaAssetLoader.loadAsset(MEDIA_ITEM)

        assertEquals(TRACKERS_DATA, asset.trackersData[TestTracker::class.java]?.data)
    }

    @Test
    fun `no trackers data without stored data`() = runTest {
        val assetLoader = TestAssetLoader(context)
        val downloadedMediaAssetLoader = createDownloadedMediaAssetLoader(trackersData = null) { assetLoader }

        val asset = downloadedMediaAssetLoader.loadAsset(MEDIA_ITEM)

        assertTrue(asset.trackersData.isEmpty())
    }

    @Test
    fun `no trackers data without matching asset loader`() = runTest {
        val downloadedMediaAssetLoader = createDownloadedMediaAssetLoader(trackersData = TRACKERS_DATA) { null }

        val asset = downloadedMediaAssetLoader.loadAsset(MEDIA_ITEM)

        assertTrue(asset.trackersData.isEmpty())
    }

    private fun createDownloadedMediaAssetLoader(
        trackersData: String?,
        findAssetLoader: (MediaItem) -> AssetLoader?,
    ): DownloadedMediaAssetLoader {
        val provider = object : DownloadedMediaProvider {
            override val cache: Cache = mockk(relaxed = true)

            override fun getDownloadRequest(mediaItem: MediaItem): DownloadRequest {
                return DownloadRequest.Builder(mediaItem.mediaId, "https://example.com/media.mp4".toUri()).build()
            }

            override fun getOfflineTrackersData(downloadRequest: DownloadRequest): ByteArray? {
                return trackersData?.encodeToByteArray()
            }
        }

        return DownloadedMediaAssetLoader(provider, DefaultDataSource.Factory(context), findAssetLoader)
    }

    private class TestTracker : MediaItemTracker<String> {
        override fun start(player: PillarboxExoPlayer, data: String) = Unit

        override fun stop(player: PillarboxExoPlayer) = Unit

        class Factory : MediaItemTracker.Factory<String> {
            override fun create(): MediaItemTracker<String> = TestTracker()
        }
    }

    private class TestAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
        override fun canLoadAsset(mediaItem: MediaItem): Boolean = true

        override suspend fun loadAsset(mediaItem: MediaItem): Asset = error("Not used")

        override fun decodeOfflineTrackersData(data: ByteArray): MediaItemTrackerData {
            return MutableMediaItemTrackerData().apply {
                this[TestTracker::class.java] = FactoryData(TestTracker.Factory(), data.decodeToString())
            }.toMediaItemTrackerData()
        }
    }

    private companion object {
        private const val TRACKERS_DATA = "trackers"
        private val MEDIA_ITEM = MediaItem.Builder()
            .setMediaId("urn:rts:video:123")
            .setUri("https://example.com/media.mp4")
            .build()
    }
}
