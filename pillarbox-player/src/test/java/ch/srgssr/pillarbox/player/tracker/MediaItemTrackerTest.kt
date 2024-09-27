/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.content.Context
import android.os.Looper
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.RobolectricUtil
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxExoPlayer
import ch.srgssr.pillarbox.player.SeekIncrement
import ch.srgssr.pillarbox.player.source.PillarboxMediaSourceFactory
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyAll
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Before
import org.junit.runner.RunWith
import org.robolectric.Shadows.shadowOf
import kotlin.coroutines.EmptyCoroutineContext
import kotlin.test.Test
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class MediaItemTrackerTest {

    private lateinit var player: PillarboxExoPlayer
    private lateinit var fakeMediaItemTracker: FakeMediaItemTracker
    private lateinit var fakeClock: FakeClock

    @Before
    fun createPlayer() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        fakeMediaItemTracker = spyk(FakeMediaItemTracker())
        fakeClock = FakeClock(true)
        player = PillarboxExoPlayer(
            context = context,
            seekIncrement = SeekIncrement(),
            loadControl = DefaultLoadControl(),
            clock = fakeClock,
            coroutineContext = EmptyCoroutineContext,
            mediaSourceFactory = PillarboxMediaSourceFactory(context).apply {
                addAssetLoader(FakeAssetLoader(context, fakeMediaItemTracker))
            },
        )
    }

    @After
    fun releasePlayer() {
        clearAllMocks()
        player.release()
        shadowOf(Looper.getMainLooper()).idle()
    }

    @Test
    fun `Player toggle tracking enabled call stop`() {
        val mediaItem = FakeAssetLoader.MEDIA_1
        val mediaId = mediaItem.mediaId
        player.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(FakeAssetLoader.NEAR_END_POSITION_MS)
        player.trackingEnabled = false

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any())
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `Player toggle tracking enabled true false call stop start`() {
        val mediaItem = FakeAssetLoader.MEDIA_1
        val mediaId = mediaItem.mediaId
        player.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(FakeAssetLoader.NEAR_END_POSITION_MS)
        player.trackingEnabled = false
        player.trackingEnabled = true

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any())
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
        }

        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `one MediaItem reach EoF`() {
        val mediaItem = FakeAssetLoader.MEDIA_1
        val mediaId = mediaItem.mediaId
        player.apply {
            setMediaItem(mediaItem)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(FakeAssetLoader.NEAR_END_POSITION_MS)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `one MediaItem reach stop`() {
        val mediaId = FakeAssetLoader.MEDIA_ID_1
        player.apply {
            setMediaItem(FakeAssetLoader.MEDIA_1)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.stop()

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any())
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `item seek to another item stop current tracker and start the other`() {
        val firstMediaId = FakeAssetLoader.MEDIA_ID_1
        val secondMediaId = FakeAssetLoader.MEDIA_ID_2
        player.apply {
            addMediaItem(FakeAssetLoader.MEDIA_1)
            addMediaItem(FakeAssetLoader.MEDIA_2)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekToDefaultPosition(1)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        RobolectricUtil.runMainLooperUntil {
            player.currentPosition >= 1_000
        }

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
            fakeMediaItemTracker.stop(any())
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(secondMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `item seek to another item without tracker stop current tracker`() {
        val firstMediaId = FakeAssetLoader.MEDIA_ID_1
        val secondMediaId = FakeAssetLoader.MEDIA_ID_NO_TRACKING_DATA
        player.apply {
            addMediaItem(FakeAssetLoader.MEDIA_1)
            addMediaItem(FakeAssetLoader.MEDIA_NO_TRACKING_DATA)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(1, FakeAssetLoader.NEAR_END_POSITION_MS)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
            fakeMediaItemTracker.stop(any())
        }
        verify(exactly = 0) {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(secondMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `remove current item call stop`() {
        val mediaId = FakeAssetLoader.MEDIA_ID_1
        player.apply {
            addMediaItem(FakeAssetLoader.MEDIA_1)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.removeMediaItem(0)

        verifyAll {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any())
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `remove current item start next item`() {
        val firstMediaId = FakeAssetLoader.MEDIA_ID_1
        val secondMediaId = FakeAssetLoader.MEDIA_ID_2
        player.apply {
            addMediaItem(FakeAssetLoader.MEDIA_1)
            addMediaItem(FakeAssetLoader.MEDIA_2)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.removeMediaItem(0)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        RobolectricUtil.runMainLooperUntil {
            player.currentPosition >= 1_000
        }

        verifyAll {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
            fakeMediaItemTracker.stop(any())
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(secondMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `replace current item by changing media meta does nothing`() {
        val firstMediaId = FakeAssetLoader.MEDIA_ID_1
        player.apply {
            addMediaItem(FakeAssetLoader.MEDIA_1)
            addMediaItem(FakeAssetLoader.MEDIA_2)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        // Wait for MediaItemSource to be loaded
        RobolectricUtil.runMainLooperUntil {
            player.getMediaItemTrackerDataOrNull() != null
        }
        val currentMediaItem = player.currentMediaItem!!
        val mediaUpdate = currentMediaItem.buildUpon()
            .setMediaMetadata(MediaMetadata.Builder().setTitle("New title").build())
            .build()
        player.replaceMediaItem(player.currentMediaItemIndex, mediaUpdate)

        verify(exactly = 1) {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `replace current item with tracker data or tag does nothing`() {
        val firstMediaId = FakeAssetLoader.MEDIA_ID_1
        player.apply {
            addMediaItem(FakeAssetLoader.MEDIA_1)
            addMediaItem(FakeAssetLoader.MEDIA_2)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        RobolectricUtil.runMainLooperUntil {
            player.getMediaItemTrackerDataOrNull() != null
        }
        val mediaItem = player.currentMediaItem
        assertNotNull(mediaItem)
        val mediaUpdate = mediaItem.buildUpon()
            .setTag(Any())
            .build()
        println("replace media item")
        player.replaceMediaItem(0, mediaUpdate)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        val waitToPosition = player.currentPosition + 3000
        RobolectricUtil.runMainLooperUntil {
            player.currentPosition >= waitToPosition
        }

        verifyAll {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `replace current item with different item stop current tracker`() {
        player.apply {
            setMediaItem(FakeAssetLoader.MEDIA_1)
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        RobolectricUtil.runMainLooperUntil {
            player.getMediaItemTrackerDataOrNull() != null
        }

        player.replaceMediaItem(0, FakeAssetLoader.MEDIA_2)
        TestPlayerRunHelper.runUntilTimelineChanged(player)
        RobolectricUtil.runMainLooperUntil {
            player.getMediaItemTrackerDataOrNull() != null
        }
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            fakeMediaItemTracker.start(player, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_1))
            fakeMediaItemTracker.stop(player)
            fakeMediaItemTracker.start(player, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_2))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `skip next stop current tracker`() {
        val firstMediaId = FakeAssetLoader.MEDIA_ID_1
        val secondMediaId = FakeAssetLoader.MEDIA_ID_2
        player.apply {
            addMediaItem(FakeAssetLoader.MEDIA_1)
            addMediaItem(FakeAssetLoader.MEDIA_2)
            prepare()
            play()
        }
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        RobolectricUtil.runMainLooperUntil {
            player.getMediaItemTrackerDataOrNull() != null
        }

        player.seekToNextMediaItem()

        TestPlayerRunHelper.runUntilTimelineChanged(player)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            fakeMediaItemTracker.start(player, FakeMediaItemTracker.Data(firstMediaId))
            fakeMediaItemTracker.stop(player)
            fakeMediaItemTracker.start(player, FakeMediaItemTracker.Data(secondMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `skip previous stop current tracker`() {
        player.apply {
            addMediaItem(FakeAssetLoader.MEDIA_1)
            addMediaItem(FakeAssetLoader.MEDIA_2)
            seekTo(1, 0)
            prepare()
            play()
        }
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        RobolectricUtil.runMainLooperUntil {
            player.getMediaItemTrackerDataOrNull() != null
        }

        player.seekToPreviousMediaItem()

        TestPlayerRunHelper.runUntilTimelineChanged(player)

        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            fakeMediaItemTracker.start(player, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_2))
            fakeMediaItemTracker.stop(player)
            fakeMediaItemTracker.start(player, FakeMediaItemTracker.Data(FakeAssetLoader.MEDIA_ID_1))
        }
        confirmVerified(fakeMediaItemTracker)
    }
}
