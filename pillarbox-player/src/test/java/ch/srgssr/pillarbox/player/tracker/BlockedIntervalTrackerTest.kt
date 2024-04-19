/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.exoplayer.source.DefaultMediaSourceFactory
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.SeekIncrement
import ch.srgssr.pillarbox.player.asset.Asset
import ch.srgssr.pillarbox.player.asset.AssetLoader
import ch.srgssr.pillarbox.player.asset.BlockedInterval
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import io.mockk.clearAllMocks
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyOrder
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test
import kotlin.test.assertEquals

@RunWith(AndroidJUnit4::class)
class BlockedIntervalTrackerTest {

    private lateinit var player: PillarboxExoPlayer
    private lateinit var fakeClock: FakeClock
    private lateinit var listener: PillarboxPlayer.Listener

    @BeforeTest
    fun createPlayer() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        listener = spyk(object : PillarboxPlayer.Listener {})
        fakeClock = FakeClock(true)
        player = PillarboxExoPlayer(
            context = context,
            seekIncrement = SeekIncrement(),
            loadControl = DefaultLoadControl(),
            clock = fakeClock,
            mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
                addAssetLoader(BlockedAssetLoader(context))
            },
        )
        player.addListener(listener)
        player.prepare()
        player.play()
    }

    @AfterTest
    fun releasePlayer() {
        player.removeListener(listener)
        player.release()
        clearAllMocks()
    }

    @Test
    fun `test block interval while playing`() {
        val expectedBlockedIntervals = listOf(BlockedAssetLoader.START_SEGMENT, BlockedAssetLoader.SEGMENT)
        player.addMediaItem(BlockedAssetLoader.MEDIA_START_BLOCKED_SEGMENT)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        val receivedBlockedIntervals = mutableListOf<BlockedInterval>()
        verifyOrder {
            listener.onBlockIntervalReached(capture(receivedBlockedIntervals))
            listener.onBlockIntervalReached(capture(receivedBlockedIntervals))
        }
        assertEquals(expectedBlockedIntervals, receivedBlockedIntervals.reversed())
    }

    @Test
    fun `test block interval when player seek`() {
        player.pause()
        val expectedBlockedIntervals = listOf(BlockedAssetLoader.SEGMENT)
        player.setMediaItem(BlockedAssetLoader.MEDIA_ONE_SEGMENT, BlockedAssetLoader.SEGMENT.start - 10)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(BlockedAssetLoader.SEGMENT.start)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val receivedBlockedIntervals = mutableListOf<BlockedInterval>()
        verify {
            listener.onBlockIntervalReached(capture(receivedBlockedIntervals))
        }
        assertEquals(expectedBlockedIntervals, receivedBlockedIntervals.reversed())
    }
}

private class BlockedAssetLoader(context: Context) : AssetLoader(DefaultMediaSourceFactory(context)) {

    override fun canLoadAsset(mediaItem: MediaItem): Boolean {
        return mediaItem.localConfiguration != null
    }

    override suspend fun loadAsset(mediaItem: MediaItem): Asset {
        val itemBuilder = mediaItem.buildUpon()
        val blockedIntervals = when (mediaItem.mediaId) {
            MEDIA_ONE_SEGMENT.mediaId -> {
                listOf(SEGMENT)
            }

            MEDIA_START_BLOCKED_SEGMENT.mediaId -> {
                listOf(START_SEGMENT, SEGMENT)
            }

            else -> {
                emptyList()
            }
        }
        return Asset(
            mediaSource = mediaSourceFactory.createMediaSource(itemBuilder.build()),
            mediaMetadata = mediaItem.mediaMetadata,
            blockedIntervals = blockedIntervals,
        )
    }

    companion object {
        val MEDIA_ONE_SEGMENT = createMediaItem("media:one_blocked_segment")
        val MEDIA_START_BLOCKED_SEGMENT = createMediaItem("media:start_blocked_segment")

        private const val URL = "https://rts-vod-amd.akamaized.net/ww/13317145/f1d49f18-f302-37ce-866c-1c1c9b76a824/master.m3u8"

        const val NEAR_END_POSITION_MS = 15_000L // the video has 17 sec duration
        val START_SEGMENT = BlockedInterval("id:1", start = 0, end = 5, reason = "reason")
        val SEGMENT = BlockedInterval("id:2", start = 10, end = 13, reason = "reason")

        private fun createMediaItem(mediaId: String) = MediaItem.Builder()
            .setUri(URL)
            .setMediaId(mediaId)
            .build()
    }
}
