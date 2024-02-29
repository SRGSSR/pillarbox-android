/*
 * Copyright (c) SRG SSR. All rights reserved.
 * License information is available from the LICENSE file.
 */
package ch.srgssr.pillarbox.player.tracker

import android.content.Context
import androidx.media3.common.MediaItem
import androidx.media3.common.MediaMetadata
import androidx.media3.common.Player
import androidx.media3.common.util.Assertions
import androidx.media3.datasource.DefaultHttpDataSource
import androidx.media3.exoplayer.DefaultLoadControl
import androidx.media3.test.utils.FakeClock
import androidx.media3.test.utils.robolectric.RobolectricUtil
import androidx.media3.test.utils.robolectric.TestPlayerRunHelper
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import ch.srgssr.pillarbox.player.PillarboxPlayer
import ch.srgssr.pillarbox.player.SeekIncrement
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerData
import ch.srgssr.pillarbox.player.extension.getMediaItemTrackerDataOrNull
import ch.srgssr.pillarbox.player.extension.setTrackerData
import io.mockk.clearAllMocks
import io.mockk.confirmVerified
import io.mockk.spyk
import io.mockk.verify
import io.mockk.verifyAll
import io.mockk.verifyOrder
import org.junit.After
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.util.concurrent.TimeoutException
import java.util.concurrent.atomic.AtomicReference
import kotlin.test.assertNotNull

@RunWith(AndroidJUnit4::class)
class MediaItemTrackerTest {

    private lateinit var player: PillarboxPlayer
    private lateinit var fakeMediaItemTracker: FakeMediaItemTracker
    private lateinit var fakeClock: FakeClock

    @Before
    fun createPlayer() {
        val context = ApplicationProvider.getApplicationContext<Context>()
        fakeMediaItemTracker = spyk(FakeMediaItemTracker())
        fakeClock = FakeClock(true)
        player = PillarboxPlayer(
            context = context,
            dataSourceFactory = DefaultHttpDataSource.Factory(),
            seekIncrement = SeekIncrement(),
            loadControl = DefaultLoadControl(),
            clock = fakeClock,
            mediaItemSource = FakeMediaItemSource(),
            mediaItemTrackerProvider = FakeTrackerProvider(fakeMediaItemTracker)
        )
    }

    @After
    fun releasePlayer() {
        clearAllMocks()
        player.release()
    }

    @Test
    fun `Player toggle tracking enabled call stop`() {
        val mediaId = FakeMediaItemSource.MEDIA_ID_1
        player.apply {
            setMediaItem(
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(FakeMediaItemSource.NEAR_END_POSITION_MS)
        player.trackingEnabled = false

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.Stop, player.currentPosition)
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `Player toggle tracking enabled true false call stop start`() {
        val mediaId = FakeMediaItemSource.MEDIA_ID_1
        player.apply {
            setMediaItem(
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(FakeMediaItemSource.NEAR_END_POSITION_MS)
        player.trackingEnabled = false
        player.trackingEnabled = true

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.Stop, player.currentPosition)
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
        }
        verify(exactly = 0) {
            fakeMediaItemTracker.update(any())
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `one MediaItem with mediaId set reach EoF`() {
        val mediaId = FakeMediaItemSource.MEDIA_ID_1
        player.apply {
            setMediaItem(
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(FakeMediaItemSource.NEAR_END_POSITION_MS)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.EoF, player.currentPosition)
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `one MediaItem with mediaId set reach stop`() {
        val mediaId = FakeMediaItemSource.MEDIA_ID_1
        player.apply {
            setMediaItem(
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.stop()

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.Stop, player.currentPosition)
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `one MediaItem with mediaId and url set reach eof`() {
        val mediaId = FakeMediaItemSource.MEDIA_ID_1
        player.apply {
            setMediaItem(
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .setUri(FakeMediaItemSource.URL_MEDIA_1)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(FakeMediaItemSource.NEAR_END_POSITION_MS)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.EoF, player.currentPosition)
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `one MediaItem with mediaId and url set reach eof then seek back`() {
        val mediaId = FakeMediaItemSource.MEDIA_ID_1
        player.apply {
            setMediaItem(
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .setUri(FakeMediaItemSource.URL_MEDIA_1)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(FakeMediaItemSource.NEAR_END_POSITION_MS)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)
        player.seekBack()
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.runUntilPendingCommandsAreFullyHandled(player)

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.EoF, player.duration)
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `one MediaItem with mediaId and url set reach stop`() {
        val mediaId = FakeMediaItemSource.MEDIA_ID_1
        player.apply {
            setMediaItem(
                MediaItem.Builder()
                    .setUri(FakeMediaItemSource.URL_MEDIA_1)
                    .setMediaId(mediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.stop()

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.Stop, player.currentPosition)
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `Playlist of different items with media id and url set transition`() {
        val firstMediaId = FakeMediaItemSource.MEDIA_ID_1
        val secondMediaId = FakeMediaItemSource.MEDIA_ID_2
        player.apply {
            addMediaItem(
                MediaItem.Builder()
                    .setUri(FakeMediaItemSource.URL_MEDIA_1)
                    .setMediaId(firstMediaId)
                    .build()
            )
            addMediaItem(
                MediaItem.Builder()
                    .setUri(FakeMediaItemSource.URL_MEDIA_2)
                    .setMediaId(secondMediaId)
                    .build()
            )
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
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.Stop, any())
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(secondMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `Playlist with items without tracking transition doesn't call start`() {
        val firstMediaId = FakeMediaItemSource.MEDIA_ID_1
        val secondMediaId = FakeMediaItemSource.MEDIA_ID_NO_TRACKING_DATA
        player.apply {
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(firstMediaId)
                    .build()
            )
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(secondMediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekTo(1, FakeMediaItemSource.NEAR_END_POSITION_MS)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_ENDED)

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.Stop, any())
        }
        verify(exactly = 0) {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(secondMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `Playlist of different items with media id set transition`() {
        val firstMediaId = FakeMediaItemSource.MEDIA_ID_1
        val secondMediaId = FakeMediaItemSource.MEDIA_ID_2
        player.apply {
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(firstMediaId)
                    .build()
            )
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(secondMediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.seekToDefaultPosition(1)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)

        verifyOrder {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.Stop, any())
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(secondMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `remove current item call stop`() {
        val mediaId = FakeMediaItemSource.MEDIA_ID_1
        player.apply {
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(mediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        player.removeMediaItem(0)

        verifyAll {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(mediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.Stop, any())
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `playlist remove current item start next item`() {
        val firstMediaId = FakeMediaItemSource.MEDIA_ID_1
        val secondMediaId = FakeMediaItemSource.MEDIA_ID_2
        player.apply {
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(firstMediaId)
                    .build()
            )
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(secondMediaId)
                    .build()
            )
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
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.Stop, any())
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(secondMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `playlist replace current item by changing media meta data only`() {
        val firstMediaId = FakeMediaItemSource.MEDIA_ID_1
        player.apply {
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(firstMediaId)
                    .build()
            )
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(FakeMediaItemSource.MEDIA_ID_2)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        // Wait for MediaItemSource to be loaded
        RobolectricUtil.runMainLooperUntil {
            player.currentMediaItem?.getMediaItemTrackerDataOrNull() != null
        }
        val currentMediaItem = player.currentMediaItem!!
        val mediaUpdate = currentMediaItem.buildUpon()
            .setMediaMetadata(MediaMetadata.Builder().setTitle("New title").build())
            .build()
        player.replaceMediaItem(player.currentMediaItemIndex, mediaUpdate)

        verify(exactly = 0) {
            fakeMediaItemTracker.update(any())
        }
        verify(exactly = 1) {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `playlist replace current item update current tracker with same data should not call update`() {
        val firstMediaId = FakeMediaItemSource.MEDIA_ID_1
        val secondMediaId = FakeMediaItemSource.MEDIA_ID_2
        player.apply {
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(firstMediaId)
                    .build()
            )
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(secondMediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        RobolectricUtil.runMainLooperUntil {
            player.currentMediaItem?.getMediaItemTrackerDataOrNull() != null
        }
        val mediaItem = player.currentMediaItem
        assertNotNull(mediaItem)
        val mediaUpdate = mediaItem.buildUpon()
            .setTrackerData(
                mediaItem.getMediaItemTrackerData().buildUpon().build()
            )
            .build()
        player.replaceMediaItem(0, mediaUpdate)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        val waitToPosition = player.currentPosition + 1000
        RobolectricUtil.runMainLooperUntil {
            player.currentPosition >= waitToPosition
        }

        verifyAll {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
        }
        verify(exactly = 0) {
            fakeMediaItemTracker.update(any())
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `playlist replace current item update current tracker with null data should not call update`() {
        val firstMediaId = FakeMediaItemSource.MEDIA_ID_1
        val secondMediaId = FakeMediaItemSource.MEDIA_ID_2
        player.apply {
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(firstMediaId)
                    .build()
            )
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(secondMediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        RobolectricUtil.runMainLooperUntil {
            player.currentMediaItem?.getMediaItemTrackerDataOrNull() != null
        }
        val mediaItem = player.currentMediaItem
        assertNotNull(mediaItem)
        val mediaUpdate = mediaItem.buildUpon()
            .setTrackerData(
                mediaItem.getMediaItemTrackerData().buildUpon()
                    .putData(FakeMediaItemTracker::class.java, null)
                    .build()
            )
            .build()
        player.replaceMediaItem(0, mediaUpdate)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        val waitToPosition = player.currentPosition + 1000
        RobolectricUtil.runMainLooperUntil {
            player.currentPosition >= waitToPosition
        }

        verifyAll {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
        }
        verify(exactly = 0) {
            fakeMediaItemTracker.update(any())
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `playlist replace current item update current tracker`() {
        val firstMediaId = FakeMediaItemSource.MEDIA_ID_1
        val secondMediaId = FakeMediaItemSource.MEDIA_ID_2
        player.apply {
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(firstMediaId)
                    .build()
            )
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(secondMediaId)
                    .build()
            )
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        RobolectricUtil.runMainLooperUntil {
            player.currentMediaItem?.getMediaItemTrackerDataOrNull() != null
        }
        val mediaItem = player.currentMediaItem
        assertNotNull(mediaItem)
        val mediaUpdate = mediaItem.buildUpon()
            .setTrackerData(
                mediaItem.getMediaItemTrackerData().buildUpon().putData(
                    FakeMediaItemTracker::class.java,
                    FakeMediaItemTracker.Data("New tracker data")
                ).build()
            )
            .build()
        player.replaceMediaItem(0, mediaUpdate)
        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        val waitToPosition = player.currentPosition + 1000
        RobolectricUtil.runMainLooperUntil {
            player.currentPosition >= waitToPosition
        }

        verifyAll {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
            fakeMediaItemTracker.update(FakeMediaItemTracker.Data("New tracker data"))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `playlist auto transition stop current tracker`() {
        val firstMediaId = FakeMediaItemSource.MEDIA_ID_1
        val secondMediaId = FakeMediaItemSource.MEDIA_ID_2
        player.apply {
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(firstMediaId)
                    .build()
            )
            addMediaItem(
                MediaItem.Builder()
                    .setMediaId(secondMediaId)
                    .build()
            )
            prepare()
            play()
            seekTo(FakeMediaItemSource.NEAR_END_POSITION_MS)
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        TestPlayerRunHelper.runUntilTimelineChanged(player)

        fakeClock.advanceTime(FakeMediaItemSource.NEAR_END_POSITION_MS)

        verifyOrder {
            fakeMediaItemTracker.start(player, FakeMediaItemTracker.Data(firstMediaId))
            fakeMediaItemTracker.stop(player, MediaItemTracker.StopReason.EoF, any())
            fakeMediaItemTracker.start(player, FakeMediaItemTracker.Data(secondMediaId))
        }
        confirmVerified(fakeMediaItemTracker)
    }

    @Test
    fun `playlist repeat current item reset current tracker`() {
        val firstMediaId = FakeMediaItemSource.MEDIA_ID_1
        player.apply {
            setMediaItem(
                MediaItem.Builder()
                    .setMediaId(firstMediaId)
                    .build(),
                FakeMediaItemSource.NEAR_END_POSITION_MS
            )
            player.repeatMode = Player.REPEAT_MODE_ONE
            prepare()
            play()
        }

        TestPlayerRunHelper.runUntilPlaybackState(player, Player.STATE_READY)
        runUntilMediaItemTransition(player)
        player.stop() // Stop player to stop the auto repeat mode

        // Wait on item transition
        // Stop otherwise goes crazy.

        verifyAll {
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.EoF, any())
            fakeMediaItemTracker.start(any(), FakeMediaItemTracker.Data(firstMediaId))
            // player.stop
            fakeMediaItemTracker.stop(any(), MediaItemTracker.StopReason.Stop, any())
        }
        confirmVerified(fakeMediaItemTracker)
    }

    companion object {
        @Throws(TimeoutException::class)
        private fun runUntilMediaItemTransition(player: Player): Pair<MediaItem?, Int> {
            val receivedEvent = AtomicReference<Pair<MediaItem?, Int>?>()
            val listener: Player.Listener = object : Player.Listener {
                override fun onMediaItemTransition(mediaItem: MediaItem?, reason: Int) {
                    receivedEvent.set(Pair(mediaItem, reason))
                }
            }
            player.addListener(listener)
            RobolectricUtil.runMainLooperUntil { receivedEvent.get() != null || player.playerError != null }
            player.removeListener(listener)
            if (player.playerError != null) {
                throw IllegalStateException(player.playerError)
            }
            return Assertions.checkNotNull(receivedEvent.get())
        }
    }
}
