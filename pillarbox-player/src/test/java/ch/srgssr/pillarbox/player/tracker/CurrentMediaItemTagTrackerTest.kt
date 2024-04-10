/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.content.Context
import androidx.media3.common.Player
import androidx.media3.exoplayer.ExoPlayer
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.asset.PillarboxData
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import io.mockk.confirmVerified
import io.mockk.mockk
import io.mockk.verifyOrder
import org.junit.runner.RunWith
import kotlin.test.AfterTest
import kotlin.test.BeforeTest
import kotlin.test.Test

@RunWith(AndroidJUnit4::class)
class CurrentMediaItemTagTrackerTest {
    private lateinit var clock: FakeClock
    private lateinit var context: Context
    private lateinit var player: ExoPlayer
    private lateinit var tagTracker: CurrentMediaItemTagTracker

    @BeforeTest
    fun setUp() {
        clock = FakeClock(true)
        context = ApplicationProvider.getApplicationContext()

        player = PillarboxExoPlayer(
            context = context,
            mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
                addAssetLoader(FakeAssetLoader(context))
            },
            mediaItemTrackerProvider = FakeTrackerProvider(FakeMediaItemTracker()),
            clock = clock,
        )

        tagTracker = CurrentMediaItemTagTracker(player)
    }

    @AfterTest
    fun tearDown() {
        player.release()
    }

    @Test
    fun `player with no media item`() {
        val callback = mockk<CurrentMediaItemTagTracker.Callback>()

        player.prepare()
        player.play()

        tagTracker.addCallback(callback)

        verifyOrder {
            callback.hashCode()
        }
        confirmVerified(callback)
    }

    @Test
    fun `player with tag-less media item`() {
        val callback = mockk<CurrentMediaItemTagTracker.Callback>(relaxed = true)
        val mediaItem = FakeAssetLoader.MEDIA_NO_TRACKING_DATA
        val expectedPillarboxData = PillarboxData(
            MediaItemTrackerData.Builder()
                .build()
        )

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        tagTracker.addCallback(callback)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem = player.currentMediaItem!!

        verifyOrder {
            callback.hashCode()
            callback.onTagChanged(mediaItem, null)
            callback.onTagChanged(currentMediaItem, expectedPillarboxData)
        }
        confirmVerified(callback)
    }

    @Test
    fun `player with tagged media item`() {
        val callback = mockk<CurrentMediaItemTagTracker.Callback>(relaxed = true)
        val mediaItem = FakeAssetLoader.MEDIA_1
        val expectedPillarboxData = PillarboxData(
            trackersData = MediaItemTrackerData.Builder()
                .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_1))
                .build()
        )

        player.setMediaItem(mediaItem)
        player.prepare()
        player.play()

        tagTracker.addCallback(callback)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem = player.currentMediaItem!!

        verifyOrder {
            callback.hashCode()
            callback.onTagChanged(mediaItem, null)
            callback.onTagChanged(currentMediaItem, expectedPillarboxData)
        }
        confirmVerified(callback)
    }

    @Test
    fun `player gets its media item replaced`() {
        val callback = mockk<CurrentMediaItemTagTracker.Callback>(relaxed = true)
        val mediaItem1 = FakeAssetLoader.MEDIA_1
        val mediaItem2 = FakeAssetLoader.MEDIA_2
        val expectedPillarboxData1 = PillarboxData(
            trackersData = MediaItemTrackerData.Builder()
                .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_1))
                .build()
        )
        val expectedPillarboxData2 = PillarboxData(
            trackersData = MediaItemTrackerData.Builder()
                .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_2))
                .build()
        )

        player.setMediaItem(mediaItem1)
        player.prepare()
        player.play()

        tagTracker.addCallback(callback)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem1 = player.currentMediaItem!!

        player.setMediaItem(mediaItem2)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem2 = player.currentMediaItem!!

        verifyOrder {
            callback.hashCode()
            callback.onTagChanged(mediaItem1, null)
            callback.onTagChanged(currentMediaItem1, expectedPillarboxData1)
            callback.onTagChanged(mediaItem2, null)
            callback.onTagChanged(currentMediaItem2, expectedPillarboxData2)
        }
        confirmVerified(callback)
    }

    @Test
    fun `player gets its media item updated`() {
        val callback = mockk<CurrentMediaItemTagTracker.Callback>(relaxed = true)
        val mediaItem1 = FakeAssetLoader.MEDIA_1
        val mediaItem2 = mediaItem1.buildUpon()
            .setMediaId(FakeAssetLoader.MEDIA_ID_2)
            .build()
        val expectedPillarboxData1 = PillarboxData(
            MediaItemTrackerData.Builder()
                .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_1))
                .build()
        )

        player.setMediaItem(mediaItem1)
        player.prepare()
        player.play()

        tagTracker.addCallback(callback)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem = player.currentMediaItem!!

        player.replaceMediaItem(player.currentMediaItemIndex, mediaItem2)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            callback.hashCode()
            callback.onTagChanged(mediaItem1, null)
            callback.onTagChanged(currentMediaItem, expectedPillarboxData1)
            callback.onTagChanged(mediaItem2, null)
        }
        confirmVerified(callback)
    }

    @Test
    fun `player gets its media item removed`() {
        val callback = mockk<CurrentMediaItemTagTracker.Callback>(relaxed = true)
        val mediaItem1 = FakeAssetLoader.MEDIA_1
        val expectedPillarboxData = PillarboxData(
            trackersData = MediaItemTrackerData.Builder()
                .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_1))
                .build()
        )

        player.setMediaItem(mediaItem1)
        player.prepare()
        player.play()

        tagTracker.addCallback(callback)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem = player.currentMediaItem!!

        player.removeMediaItem(0)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        verifyOrder {
            callback.hashCode()
            callback.onTagChanged(mediaItem1, null)
            callback.onTagChanged(currentMediaItem, expectedPillarboxData)
            callback.onTagChanged(null, null)
        }
        confirmVerified(callback)
    }

    @Test
    fun `player gets a new item added`() {
        val callback = mockk<CurrentMediaItemTagTracker.Callback>(relaxed = true)
        val mediaItem1 = FakeAssetLoader.MEDIA_1
        val mediaItem2 = FakeAssetLoader.MEDIA_2
        val expectedPillarboxData = PillarboxData(
            trackersData = MediaItemTrackerData.Builder()
                .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_1))
                .build()
        )

        player.setMediaItem(mediaItem1)
        player.prepare()
        player.play()

        tagTracker.addCallback(callback)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem = player.currentMediaItem!!

        player.addMediaItem(mediaItem2)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            callback.hashCode()
            callback.onTagChanged(mediaItem1, null)
            callback.onTagChanged(currentMediaItem, expectedPillarboxData)
        }
        confirmVerified(callback)
    }

    @Test
    fun `player transition to the next item`() {
        val callback = mockk<CurrentMediaItemTagTracker.Callback>(relaxed = true)
        val mediaItem1 = FakeAssetLoader.MEDIA_1
        val mediaItem2 = FakeAssetLoader.MEDIA_2
        val expectedPillarboxData1 = PillarboxData(
            trackersData = MediaItemTrackerData.Builder()
                .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_1))
                .build()
        )
        val expectedPillarboxData2 = PillarboxData(
            trackersData = MediaItemTrackerData.Builder()
                .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_2))
                .build()
        )

        player.addMediaItem(mediaItem1)
        player.addMediaItem(mediaItem2)
        player.prepare()
        player.play()

        tagTracker.addCallback(callback)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem1 = player.currentMediaItem!!

        player.seekToNextMediaItem()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem2 = player.currentMediaItem!!

        verifyOrder {
            callback.hashCode()
            callback.onTagChanged(mediaItem1, null)
            callback.onTagChanged(currentMediaItem1, expectedPillarboxData1)
            callback.onTagChanged(mediaItem2, null)
            callback.onTagChanged(currentMediaItem2, expectedPillarboxData2)
        }
        confirmVerified(callback)
    }

    @Test
    fun `playlist gets cleared`() {
        val callback = mockk<CurrentMediaItemTagTracker.Callback>(relaxed = true)
        val mediaItem1 = FakeAssetLoader.MEDIA_1
        val mediaItem2 = FakeAssetLoader.MEDIA_2
        val expectedPillarboxData = PillarboxData(
            MediaItemTrackerData.Builder()
                .putData(FakeMediaItemTracker::class.java, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_1))
                .build()
        )

        player.setMediaItems(listOf(mediaItem1, mediaItem2))
        player.prepare()
        player.play()

        tagTracker.addCallback(callback)

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        val currentMediaItem = player.currentMediaItem!!

        player.clearMediaItems()

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        verifyOrder {
            callback.hashCode()
            callback.onTagChanged(mediaItem1, null)
            callback.onTagChanged(currentMediaItem, expectedPillarboxData)
            callback.onTagChanged(null, null)
        }
        confirmVerified(callback)
    }
}
