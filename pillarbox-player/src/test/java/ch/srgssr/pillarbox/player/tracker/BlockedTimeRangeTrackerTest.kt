/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.content.Context
import android.os.Looper
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.PillarboxMetadata
import ch.srgssr.pillarbox.player.asset.timeRange.BlockedTimeRange
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class BlockedTimeRangeTrackerTest {

    private lateinit var player: PillarboxExoPlayer
    private lateinit var listener: PillarboxPlayer.Listener

    @BeforeTest
    fun createPlayer() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        listener = spyk(object : PillarboxPlayer.Listener {})
        player = PillarboxExoPlayer {
            +BlockedAssetLoader(context)
        }
        player.addListener(listener)
        player.prepare()
        player.play()
    }

    @AfterTest
    fun releasePlayer() {
        player.removeListener(listener)
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
        clearAllMocks()
    }

    @Test
    fun `test block time range while playing`() {
        val expectedBlockedIntervals = listOf(BlockedAssetLoader.START_SEGMENT, BlockedAssetLoader.SEGMENT)
        player.addMediaItem(BlockedAssetLoader.MEDIA_START_BLOCKED_SEGMENT)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val receivedBlockedTimeRanges = mutableListOf<BlockedTimeRange>()
        verifyOrder {
            listener.onBlockedTimeRangeReached(capture(receivedBlockedTimeRanges))
            listener.onBlockedTimeRangeReached(capture(receivedBlockedTimeRanges))
        }
        assertEquals(expectedBlockedIntervals, receivedBlockedTimeRanges.reversed())
    }

    @Test
    fun `test block time range when player seek`() {
        player.pause()
        val expectedBlockedIntervals = listOf(BlockedAssetLoader.SEGMENT)
        player.setMediaItem(BlockedAssetLoader.MEDIA_ONE_SEGMENT, BlockedAssetLoader.SEGMENT.start - 10)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(BlockedAssetLoader.SEGMENT.start)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val receivedBlockedTimeRanges = mutableListOf<BlockedTimeRange>()
        verify {
            listener.onBlockedTimeRangeReached(capture(receivedBlockedTimeRanges))
        }
        assertEquals(expectedBlockedIntervals, receivedBlockedTimeRanges.reversed())
    }
}

private class BlockedAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfiguration != null
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val itemBuilder = mediaItem.buildUpon()
        val listBlockedTimeRanges = when (mediaItem.mediaId) {
            MEDIA_ONE_SEGMENT.mediaId -> listOf(SEGMENT)
            MEDIA_START_BLOCKED_SEGMENT.mediaId -> listOf(START_SEGMENT, SEGMENT)
            else -> emptyList()
        }
        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(itemBuilder.build()),
            mediaMetadata = mediaItem.mediaMetadata,
            pillarboxMetadata = PillarboxMetadata(blockedTimeRanges = listBlockedTimeRanges),
        )
    }

    companion object {
        val MEDIA_ONE_SEGMENT = createMediaItem("media:one_blocked_segment")
        val MEDIA_START_BLOCKED_SEGMENT = createMediaItem("media:start_blocked_segment")

        private const val URL = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"

        val START_SEGMENT = BlockedTimeRange(id = "id:1", start = 0, end = 5, reason = "reason")
        val SEGMENT = BlockedTimeRange(id = "id:2", start = 10, end = 13, reason = "reason")

        private fun createMediaItem(mediaId: String) = MediaItem.Builder()
            .setUri(URL)
            .setMediaId(mediaId)
            .build()
    }
}
