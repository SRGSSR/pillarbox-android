/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player

import android.content.Context
import android.os.Looper
import androidx.media3.common.C
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
    }

    @AfterTest
    fun tearDown() {
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `update with new pillarbox metadata is applied without reloading`() {
        prepare(MEDIA_ITEM)
        val pillarboxMetadata = PillarboxMetadata(blockedTimeRanges = listOf(BlockedTimeRange(start = 1_000L, end = 2_000L)))

        emitUpdate(UpdatableMediaItem(pillarboxMetadata = pillarboxMetadata, mediaItem = streamItem(STREAM_URL_LIVE)))

        assertEquals(pillarboxMetadata, player.currentPillarboxMetadata)
        assertEquals(1, assetLoader.loadCount)
    }

    @Test
    fun `update with new media metadata is applied without reloading`() {
        prepare(MEDIA_ITEM)
        val mediaMetadata = MediaMetadata.Builder().setTitle("New title").build()

        emitUpdate(
            UpdatableMediaItem(
                pillarboxMetadata = PillarboxMetadata.EMPTY,
                mediaItem = streamItem(STREAM_URL_LIVE),
                mediaMetadata = mediaMetadata,
            )
        )

        assertEquals("New title", player.currentMediaItem?.mediaMetadata?.title)
        assertEquals("New title", player.mediaMetadata.title)
        assertEquals(1, assetLoader.loadCount)
    }

    @Test
    fun `update without changes does nothing`() {
        prepare(MEDIA_ITEM)
        val mediaItem = player.currentMediaItem

        emitUpdate(UpdatableMediaItem(pillarboxMetadata = PillarboxMetadata.EMPTY, mediaItem = streamItem(STREAM_URL_LIVE)))

        assertEquals(mediaItem, player.currentMediaItem)
        assertEquals(PillarboxMetadata.EMPTY, player.currentPillarboxMetadata)
        assertEquals(1, assetLoader.loadCount)
    }

    @Test
    fun `update with a new stream URL reloads the item`() {
        prepare(MEDIA_ITEM)

        assetLoader.streamUrl = STREAM_URL_VOD
        emitUpdate(UpdatableMediaItem(pillarboxMetadata = PillarboxMetadata.EMPTY, mediaItem = streamItem(STREAM_URL_VOD)))
        advance(player).untilState(Player.STATE_READY)

        assertEquals(2, assetLoader.loadCount)
        assertEquals(MEDIA_ITEM.mediaId, player.currentMediaItem?.mediaId)
        assertEquals(0, player.currentMediaItemIndex)
    }

    @Test
    fun `update with a new stream URL keeps the playlist and the current item`() {
        val otherMediaItem = MediaItem.Builder().setMediaId("other").setUri("pillarbox://other").build()
        player.setMediaItems(listOf(otherMediaItem, MEDIA_ITEM), 1, C.TIME_UNSET)
        player.prepare()
        advance(player).untilState(Player.STATE_READY)

        assetLoader.streamUrl = STREAM_URL_VOD
        emitUpdate(UpdatableMediaItem(pillarboxMetadata = PillarboxMetadata.EMPTY, mediaItem = streamItem(STREAM_URL_VOD)))
        advance(player).untilState(Player.STATE_READY)

        assertEquals(2, player.mediaItemCount)
        assertEquals(otherMediaItem.mediaId, player.getMediaItemAt(0).mediaId)
        assertEquals(1, player.currentMediaItemIndex)
        assertEquals(MEDIA_ITEM.mediaId, player.currentMediaItem?.mediaId)
    }

    @Test
    fun `update with a new stream URL resumes at the same unix time`() {
        prepare(MEDIA_ITEM)
        player.seekTo(SEEK_POSITION_MS)
        advance(player).untilPendingCommandsAreFullyHandled()
        val unixTimeMs = WINDOW_START_TIME_LIVE_MS + SEEK_POSITION_MS

        assetLoader.streamUrl = STREAM_URL_VOD
        emitUpdate(UpdatableMediaItem(pillarboxMetadata = PillarboxMetadata.EMPTY, mediaItem = streamItem(STREAM_URL_VOD)))
        advance(player).untilState(Player.STATE_READY)
        advance(player).untilPendingCommandsAreFullyHandled()

        assertEquals(2, assetLoader.loadCount)
        assertEquals(unixTimeMs - WINDOW_START_TIME_VOD_MS, player.currentPosition)
    }

    @Test
    fun `update with a new stream URL keeps the position when the unix time is not in the new stream`() {
        prepare(MEDIA_ITEM)
        player.seekTo(SEEK_POSITION_MS)
        advance(player).untilPendingCommandsAreFullyHandled()

        assetLoader.streamUrl = STREAM_URL_OTHER_DAY
        emitUpdate(UpdatableMediaItem(pillarboxMetadata = PillarboxMetadata.EMPTY, mediaItem = streamItem(STREAM_URL_OTHER_DAY)))
        advance(player).untilState(Player.STATE_READY)
        advance(player).untilPendingCommandsAreFullyHandled()

        assertEquals(2, assetLoader.loadCount)
        assertEquals(SEEK_POSITION_MS, player.currentPosition)
    }

    @Test
    fun `update with a new stream URL stays at the live edge`() {
        prepare(MEDIA_ITEM)
        assertEquals(DEFAULT_POSITION.inWholeMilliseconds, player.currentPosition)

        assetLoader.streamUrl = STREAM_URL_VOD
        emitUpdate(UpdatableMediaItem(pillarboxMetadata = PillarboxMetadata.EMPTY, mediaItem = streamItem(STREAM_URL_VOD)))
        advance(player).untilState(Player.STATE_READY)
        advance(player).untilPendingCommandsAreFullyHandled()

        assertEquals(2, assetLoader.loadCount)
        assertEquals(DEFAULT_POSITION.inWholeMilliseconds, player.currentPosition)
    }

    private fun prepare(mediaItem: MediaItem) {
        player.setMediaItem(mediaItem)
        player.prepare()
        advance(player).untilState(Player.STATE_READY)
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
     * An [AssetLoader] that plays fake streams, identified by [streamUrl], and whose updates are emitted by [updates].
     */
    private class UpdatableAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {
        val updates = MutableSharedFlow<UpdatableMediaItem>(extraBufferCapacity = 1)

        @Volatile
        var streamUrl = STREAM_URL_LIVE

        @Volatile
        var loadCount = 0
            private set

        override fun canLoadAsset(mediaItem: MediaItem): Boolean = true

        override suspend fun loadAsset(mediaItem: MediaItem): Asset {
            loadCount++
            return Asset(
                mediaSource = createFakeMediaSource(streamUrl),
                mediaMetadata = mediaItem.mediaMetadata,
                updateFlow = updates,
            )
        }

        private fun createFakeMediaSource(streamUrl: String): FakeMediaSource {
            val windowStartTimeMs = when (streamUrl) {
                STREAM_URL_LIVE -> WINDOW_START_TIME_LIVE_MS
                STREAM_URL_VOD -> WINDOW_START_TIME_VOD_MS
                else -> WINDOW_START_TIME_OTHER_DAY_MS
            }
            // FakeTimeline only provides the window start time of live windows.
            val window = FakeTimeline.TimelineWindowDefinition.Builder()
                .setMediaItem(streamItem(streamUrl))
                .setLive(true)
                .setDynamic(true)
                .setDurationUs(WINDOW_DURATION.inWholeMicroseconds)
                .setDefaultPositionUs(DEFAULT_POSITION.inWholeMicroseconds)
                .setWindowStartTimeUs(windowStartTimeMs * 1_000L)
                .build()
            return FakeMediaSource(FakeTimeline(window), VIDEO_FORMAT)
        }
    }

    private companion object {
        private const val STREAM_URL_LIVE = "https://example.com/live/master.m3u8"
        private const val STREAM_URL_VOD = "https://example.com/vod/master.m3u8"
        private const val STREAM_URL_OTHER_DAY = "https://example.com/other/master.m3u8"

        private val WINDOW_DURATION = 60.seconds
        private val DEFAULT_POSITION = 50.seconds
        private const val WINDOW_START_TIME_LIVE_MS = 1_700_000_000_000L
        private const val WINDOW_START_TIME_VOD_MS = WINDOW_START_TIME_LIVE_MS + 5_000L
        private const val WINDOW_START_TIME_OTHER_DAY_MS = WINDOW_START_TIME_LIVE_MS + 86_400_000L
        private const val SEEK_POSITION_MS = 20_000L

        private val MEDIA_ITEM = MediaItem.Builder()
            .setMediaId("media:1")
            .setUri("pillarbox://media/1")
            .setMediaMetadata(MediaMetadata.Builder().setTitle("Title").build())
            .build()

        private val VIDEO_FORMAT = Format.Builder()
            .setSampleMimeType(MimeTypes.VIDEO_H264)
            .build()

        private fun streamItem(streamUrl: String): MediaItem = MediaItem.fromUri(streamUrl)
    }
}
