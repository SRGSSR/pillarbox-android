/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import android.os.Looper
import androidx.media3.common.Format
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.MimeTypes
import androidx.media3.common.Player
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.test.utils.FakeMediaSource
import androidx.media3.test.utils.FakeTimeline
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper.advance
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.UpdatableMediaItem
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import kotlinx.coroutines.flow.MutableSharedFlow
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals
import kotlin.test.assertTrue
import kotlin.time.Duration.Companion.seconds

/**
 * Tests the updates of the item being played, emitted by [Asset.updateFlow].
 */
@RunWith(AndroidJUnit4::class)
class PillarboxExoPlayerUpdateTest {
    private lateinit var assetLoader: UpdatableAssetLoader
    private lateinit var player: PillarboxExoPlayer

    @BeforeTest
    fun setup() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        assetLoader = UpdatableAssetLoader(context)
        player = PillarboxExoPlayer {
            disableMonitoring()
            addAssetLoader(assetLoader)
        }
        player.setMediaItem(MEDIA_ITEM)
        player.prepare()
        advance(player).untilState(Player.STATE_READY)
    }

    @AfterTest
    fun tearDown() {
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `update with new pillarbox metadata is applied without reloading`() {
        val pillarboxMetadata = PillarboxMetadata(blockedTimeRanges = listOf(BlockedTimeRange(start = 1_000L, end = 2_000L)))

        emitUpdate(UpdatableMediaItem(pillarboxMetadata = pillarboxMetadata))

        assertEquals(pillarboxMetadata, player.currentPillarboxMetadata)
        assertEquals(1, assetLoader.loadCount)
    }

    @Test
    fun `update with new media metadata is applied without reloading`() {
        val mediaMetadata = MediaMetadata.Builder().setTitle("New title").build()

        emitUpdate(UpdatableMediaItem(pillarboxMetadata = PillarboxMetadata.EMPTY, mediaMetadata = mediaMetadata))

        assertEquals("New title", player.currentMediaItem?.mediaMetadata?.title)
        assertEquals("New title", player.mediaMetadata.title)
        assertEquals(1, assetLoader.loadCount)
    }

    @Test
    fun `update with empty media metadata keeps the current media metadata`() {
        emitUpdate(UpdatableMediaItem(pillarboxMetadata = PillarboxMetadata.EMPTY, mediaMetadata = MediaMetadata.EMPTY))

        assertEquals(MEDIA_ITEM.mediaMetadata.title, player.currentMediaItem?.mediaMetadata?.title)
        assertEquals(1, assetLoader.loadCount)
    }

    @Test
    fun `update without changes does nothing`() {
        val mediaItem = player.currentMediaItem

        emitUpdate(UpdatableMediaItem(pillarboxMetadata = PillarboxMetadata.EMPTY, mediaMetadata = MEDIA_ITEM.mediaMetadata))

        assertEquals(mediaItem, player.currentMediaItem)
        assertEquals(PillarboxMetadata.EMPTY, player.currentPillarboxMetadata)
        assertEquals(1, assetLoader.loadCount)
    }

    @Test
    fun `updates keep the stream and the position`() {
        player.seekTo(SEEK_POSITION_MS)
        advance(player).untilPendingCommandsAreFullyHandled()
        val pillarboxMetadata = PillarboxMetadata(blockedTimeRanges = listOf(BlockedTimeRange(start = 1_000L, end = 2_000L)))
        val mediaMetadata = MediaMetadata.Builder().setTitle("New title").build()

        emitUpdate(UpdatableMediaItem(pillarboxMetadata = pillarboxMetadata))
        emitUpdate(UpdatableMediaItem(pillarboxMetadata = pillarboxMetadata, mediaMetadata = mediaMetadata))

        assertEquals(1, assetLoader.loadCount)
        assertEquals(MEDIA_ITEM.mediaId, player.currentMediaItem?.mediaId)
        assertEquals(SEEK_POSITION_MS, player.currentPosition)
    }

    /**
     * Emits [update] once the player collects the update flow, then waits until the player handled it.
     */
    private fun emitUpdate(update: UpdatableMediaItem) {
        val mainLooper = shadowOf(Looper.getMainLooper())
        mainLooper.idle()
        assertTrue(assetLoader.updates.subscriptionCount.value > 0, "The player doesn't collect the update flow")
        assertTrue(assetLoader.updates.tryEmit(update))
        mainLooper.idle()
        advance(player).untilPendingCommandsAreFullyHandled()
    }

    /**
     * An [AssetLoader] that plays a fake stream, and whose updates are emitted by [updates].
     */
    private class UpdatableAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
        val updates = MutableSharedFlow<UpdatableMediaItem>(extraBufferCapacity = 1)

        @Volatile
        var loadCount = 0
            private set

        override fun canLoadAsset(mediaItem: MediaItem): Boolean = true

        override suspend fun loadAsset(mediaItem: MediaItem): Asset {
            loadCount++
            val window = FakeTimeline.TimelineWindowDefinition.Builder()
                .setMediaItem(MediaItem.fromUri(STREAM_URL))
                .setDurationUs(WINDOW_DURATION.inWholeMicroseconds)
                .build()
            return Asset(
                mediaSource = FakeMediaSource(FakeTimeline(window), VIDEO_FORMAT),
                mediaMetadata = mediaItem.mediaMetadata,
                updateFlow = updates,
            )
        }
    }

    private companion object {
        private const val STREAM_URL = "https://example.com/live/master.m3u8"
        private const val SEEK_POSITION_MS = 20_000L
        private val WINDOW_DURATION = 60.seconds

        private val MEDIA_ITEM = MediaItem.Builder()
            .setMediaId("media:1")
            .setUri("pillarbox://media/1")
            .setMediaMetadata(MediaMetadata.Builder().setTitle("Title").build())
            .build()

        private val VIDEO_FORMAT = Format.Builder()
            .setSampleMimeType(MimeTypes.VIDEO_H264)
            .build()
    }
}
